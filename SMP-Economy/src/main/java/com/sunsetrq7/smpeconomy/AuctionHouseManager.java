package com.sunsetrq7.smpeconomy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages the auction house system.
 */
public class AuctionHouseManager {
    
    private final SMP_Economy plugin;
    private final DatabaseManager databaseManager;
    private final EconomyManager economyManager;
    private final BankManager bankManager;
    
    public AuctionHouseManager(SMP_Economy plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.economyManager = plugin.getEconomyManager();
        this.bankManager = plugin.getBankManager();
    }
    
    /**
     * Creates a new auction.
     */
    public boolean createAuction(UUID sellerUUID, ItemStack item, double startingBid, Double buyoutPrice, 
                                int durationSeconds, String category) {
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "INSERT INTO auctions (seller_uuid, item_name, item_data, starting_bid, " +
                        "buyout_price, duration_seconds, end_time, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, sellerUUID.toString());
                stmt.setString(2, item.getType().name());
                stmt.setString(3, serializeItemStack(item));
                stmt.setDouble(4, startingBid);
                stmt.setObject(5, buyoutPrice); // Can be null
                stmt.setInt(6, durationSeconds);
                stmt.setTimestamp(7, new java.sql.Timestamp(System.currentTimeMillis() + (durationSeconds * 1000L)));
                stmt.setString(8, category);
                
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create auction for seller: " + sellerUUID, e);
            return false;
        }
    }
    
    /**
     * Gets all active auctions.
     */
    public List<Auction> getActiveAuctions() {
        List<Auction> auctions = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "SELECT * FROM auctions WHERE status = 'ACTIVE' AND end_time > CURRENT_TIMESTAMP ORDER BY end_time ASC";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        auctions.add(resultSetToAuction(rs));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get active auctions", e);
        }
        
        return auctions;
    }
    
    /**
     * Gets auctions by category.
     */
    public List<Auction> getAuctionsByCategory(String category) {
        List<Auction> auctions = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "SELECT * FROM auctions WHERE status = 'ACTIVE' AND category = ? AND end_time > CURRENT_TIMESTAMP ORDER BY end_time ASC";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, category);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        auctions.add(resultSetToAuction(rs));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get auctions by category: " + category, e);
        }
        
        return auctions;
    }
    
    /**
     * Gets auctions by seller.
     */
    public List<Auction> getAuctionsBySeller(UUID sellerUUID) {
        List<Auction> auctions = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "SELECT * FROM auctions WHERE seller_uuid = ? ORDER BY created_at DESC";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, sellerUUID.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        auctions.add(resultSetToAuction(rs));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get auctions for seller: " + sellerUUID, e);
        }
        
        return auctions;
    }
    
    /**
     * Gets an auction by ID.
     */
    public Auction getAuctionById(int auctionId) {
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "SELECT * FROM auctions WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, auctionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return resultSetToAuction(rs);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get auction by ID: " + auctionId, e);
        }
        
        return null;
    }
    
    /**
     * Places a bid on an auction.
     */
    public boolean placeBid(Player bidder, int auctionId, double bidAmount) {
        Auction auction = getAuctionById(auctionId);
        if (auction == null) {
            return false;
        }
        
        // Check if auction is still active
        if (!auction.getStatus().equals("ACTIVE") || auction.getEndTime().before(new java.util.Date())) {
            return false;
        }
        
        // Check if bid amount is sufficient
        double currentBid = auction.getCurrentBid() > auction.getStartingBid() ? auction.getCurrentBid() : auction.getStartingBid();
        double minimumBid = currentBid + plugin.getConfigManager().getAuctionConfig().getDouble("minimum_bid_increase", 1.0);
        
        if (bidAmount < minimumBid) {
            return false;
        }
        
        // Check if bidder has enough money
        if (!economyManager.has(bidder, bidAmount)) {
            return false;
        }
        
        // Check bid cooldown
        if (isBidCooldownActive(bidder.getUniqueId(), auctionId)) {
            return false;
        }
        
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            
            try {
                // Update the auction with the new bid
                String updateSql = "UPDATE auctions SET current_bid = ?, highest_bidder_uuid = ?, last_updated = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, bidAmount);
                    updateStmt.setString(2, bidder.getUniqueId().toString());
                    updateStmt.setInt(3, auctionId);
                    
                    if (updateStmt.executeUpdate() == 0) {
                        connection.rollback();
                        return false;
                    }
                }
                
                // Record the bid
                String bidSql = "INSERT INTO auction_bids (auction_id, bidder_uuid, bid_amount) VALUES (?, ?, ?)";
                try (PreparedStatement bidStmt = connection.prepareStatement(bidSql)) {
                    bidStmt.setInt(1, auctionId);
                    bidStmt.setString(2, bidder.getUniqueId().toString());
                    bidStmt.setDouble(3, bidAmount);
                    
                    if (bidStmt.executeUpdate() == 0) {
                        connection.rollback();
                        return false;
                    }
                }
                
                // If there was a previous highest bidder, refund their money
                if (auction.getHighestBidderUuid() != null) {
                    UUID previousBidder = auction.getHighestBidderUuid();
                    Player previousPlayer = Bukkit.getPlayer(previousBidder);
                    if (previousPlayer != null && previousPlayer.isOnline()) {
                        // Refund the previous bidder
                        economyManager.addMoney(previousPlayer, auction.getCurrentBid());
                    }
                }
                
                // Deduct the bid amount from the new bidder
                economyManager.removeMoney(bidder, bidAmount);
                
                connection.commit();
                return true;
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to place bid on auction: " + auctionId, e);
            return false;
        }
    }
    
    /**
     * Checks if a bid cooldown is active for a player on a specific auction.
     */
    private boolean isBidCooldownActive(UUID playerUUID, int auctionId) {
        // Check config for bid cooldown
        int cooldownSeconds = plugin.getConfigManager().getAuctionConfig().getInt("bid_cooldown_seconds", 5);
        long cooldownMillis = cooldownSeconds * 1000L;
        
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "SELECT timestamp FROM auction_bids WHERE bidder_uuid = ? AND auction_id = ? ORDER BY timestamp DESC LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUUID.toString());
                stmt.setInt(2, auctionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        long lastBidTime = rs.getTimestamp("timestamp").getTime();
                        return (System.currentTimeMillis() - lastBidTime) < cooldownMillis;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to check bid cooldown for player: " + playerUUID, e);
        }
        
        return false;
    }
    
    /**
     * Processes ended auctions and handles payouts.
     */
    public void processEndedAuctions() {
        List<Auction> endedAuctions = getEndedAuctions();
        
        for (Auction auction : endedAuctions) {
            processAuctionEnd(auction);
        }
    }
    
    /**
     * Gets all auctions that have ended.
     */
    private List<Auction> getEndedAuctions() {
        List<Auction> auctions = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "SELECT * FROM auctions WHERE status = 'ACTIVE' AND end_time <= CURRENT_TIMESTAMP";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        auctions.add(resultSetToAuction(rs));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get ended auctions", e);
        }
        
        return auctions;
    }
    
    /**
     * Processes the end of an auction.
     */
    private void processAuctionEnd(Auction auction) {
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            
            // Update auction status to ended
            String updateSql = "UPDATE auctions SET status = 'ENDED' WHERE id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                updateStmt.setInt(1, auction.getId());
                updateStmt.executeUpdate();
            }
            
            // If there was a winning bid, handle the transaction
            if (auction.getHighestBidderUuid() != null) {
                // Get the seller
                Player seller = Bukkit.getPlayer(auction.getHighestBidderUuid());
                
                // Calculate fee
                double feePercentage = plugin.getConfigManager().getAuctionConfig().getDouble("fee_percentage", 5.0);
                double feeAmount = auction.getCurrentBid() * (feePercentage / 100.0);
                double sellerAmount = auction.getCurrentBid() - feeAmount;
                
                // Pay the seller
                economyManager.addMoney(Bukkit.getOfflinePlayer(auction.getSellerUuid()), sellerAmount);
                
                // Add fee to server economy (or handle as configured)
                // This could be configurable to go to a server account or be destroyed
                
                // Send notifications to seller and winner
                Player winner = Bukkit.getPlayer(auction.getHighestBidderUuid());
                if (winner != null && winner.isOnline()) {
                    // Give the item to the winner
                    // This would typically be handled by the GUI system
                    plugin.getNotificationManager().sendNotification(winner, 
                        plugin.getLanguageManager().getMessage("en_US", "auction.won", 
                            "item", auction.getItemName(), 
                            "amount", economyManager.format(auction.getCurrentBid())));
                }
                
                Player originalSeller = Bukkit.getPlayer(auction.getSellerUuid());
                if (originalSeller != null && originalSeller.isOnline()) {
                    plugin.getNotificationManager().sendNotification(originalSeller, 
                        plugin.getLanguageManager().getMessage("en_US", "auction.sold", 
                            "item", auction.getItemName(), 
                            "amount", economyManager.format(sellerAmount)));
                }
            } else {
                // No bids - return item to seller (handled by GUI system)
                Player originalSeller = Bukkit.getPlayer(auction.getSellerUuid());
                if (originalSeller != null && originalSeller.isOnline()) {
                    plugin.getNotificationManager().sendNotification(originalSeller, 
                        plugin.getLanguageManager().getMessage("en_US", "auction.no_bids", 
                            "item", auction.getItemName()));
                }
            }
            
            connection.commit();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to process ended auction: " + auction.getId(), e);
        }
    }
    
    /**
     * Cancels an auction (only for the seller before any bids).
     */
    public boolean cancelAuction(Player player, int auctionId) {
        Auction auction = getAuctionById(auctionId);
        if (auction == null) {
            return false;
        }
        
        // Check if player is the seller
        if (!auction.getSellerUuid().equals(player.getUniqueId())) {
            return false;
        }
        
        // Check if auction has any bids
        if (auction.getCurrentBid() > auction.getStartingBid()) {
            // Auction has bids, cannot cancel
            return false;
        }
        
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "UPDATE auctions SET status = 'CANCELLED' WHERE id = ? AND seller_uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, auctionId);
                stmt.setString(2, player.getUniqueId().toString());
                
                boolean success = stmt.executeUpdate() > 0;
                if (success) {
                    // Return the item to the seller (handled by GUI system)
                    plugin.getNotificationManager().sendNotification(player, 
                        plugin.getLanguageManager().getMessage("en_US", "auction.cancelled"));
                }
                return success;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to cancel auction: " + auctionId, e);
            return false;
        }
    }
    
    /**
     * Serializes an ItemStack to a string.
     * In a real implementation, this would use proper serialization.
     */
    private String serializeItemStack(ItemStack item) {
        // This is a simplified serialization - in a real plugin you'd use proper NBT serialization
        return item.getType().name() + ":" + item.getAmount();
    }
    
    /**
     * Converts a ResultSet row to an Auction object.
     */
    private Auction resultSetToAuction(ResultSet rs) throws SQLException {
        return new Auction(
            rs.getInt("id"),
            UUID.fromString(rs.getString("seller_uuid")),
            rs.getString("item_name"),
            rs.getString("item_data"),
            rs.getDouble("starting_bid"),
            rs.getObject("buyout_price", Double.class),
            rs.getDouble("current_bid"),
            rs.getString("highest_bidder_uuid") != null ? UUID.fromString(rs.getString("highest_bidder_uuid")) : null,
            rs.getInt("duration_seconds"),
            rs.getTimestamp("start_time"),
            rs.getTimestamp("end_time"),
            rs.getString("status"),
            rs.getString("category"),
            rs.getTimestamp("created_at")
        );
    }
    
    /**
     * Inner class to represent an auction.
     */
    public static class Auction {
        private final int id;
        private final UUID sellerUuid;
        private final String itemName;
        private final String itemData;
        private final double startingBid;
        private final Double buyoutPrice;
        private final double currentBid;
        private final UUID highestBidderUuid;
        private final int durationSeconds;
        private final java.util.Date startTime;
        private final java.util.Date endTime;
        private final String status;
        private final String category;
        private final java.util.Date createdAt;
        
        public Auction(int id, UUID sellerUuid, String itemName, String itemData, double startingBid,
                      Double buyoutPrice, double currentBid, UUID highestBidderUuid, int durationSeconds,
                      java.sql.Timestamp startTime, java.sql.Timestamp endTime, String status, 
                      String category, java.sql.Timestamp createdAt) {
            this.id = id;
            this.sellerUuid = sellerUuid;
            this.itemName = itemName;
            this.itemData = itemData;
            this.startingBid = startingBid;
            this.buyoutPrice = buyoutPrice;
            this.currentBid = currentBid;
            this.highestBidderUuid = highestBidderUuid;
            this.durationSeconds = durationSeconds;
            this.startTime = startTime;
            this.endTime = endTime;
            this.status = status;
            this.category = category;
            this.createdAt = createdAt;
        }
        
        public int getId() { return id; }
        public UUID getSellerUuid() { return sellerUuid; }
        public String getItemName() { return itemName; }
        public String getItemData() { return itemData; }
        public double getStartingBid() { return startingBid; }
        public Double getBuyoutPrice() { return buyoutPrice; }
        public double getCurrentBid() { return currentBid; }
        public UUID getHighestBidderUuid() { return highestBidderUuid; }
        public int getDurationSeconds() { return durationSeconds; }
        public java.util.Date getStartTime() { return startTime; }
        public java.util.Date getEndTime() { return endTime; }
        public String getStatus() { return status; }
        public String getCategory() { return category; }
        public java.util.Date getCreatedAt() { return createdAt; }
    }
}