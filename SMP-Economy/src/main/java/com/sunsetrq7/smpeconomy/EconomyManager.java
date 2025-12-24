package com.sunsetrq7.smpeconomy;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages the core economy functionality including player balances.
 */
public class EconomyManager {
    
    private final SMP_Economy plugin;
    private final DatabaseManager databaseManager;
    private final PlayerDataManager playerDataManager;
    private final TransactionLogger transactionLogger;
    
    // Constants for economy configuration
    private static final double MAX_BALANCE = 1_000_000_000.0; // 1 billion
    private static final double MIN_TRANSACTION = 0.01; // Minimum transaction amount
    
    public EconomyManager(SMP_Economy plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.playerDataManager = plugin.getPlayerDataManager();
        this.transactionLogger = new TransactionLogger(plugin);
    }
    
    /**
     * Gets a player's balance.
     */
    public double getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }
    
    /**
     * Gets a player's balance by UUID.
     */
    public double getBalance(UUID playerUUID) {
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "SELECT balance FROM players WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUUID.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("balance");
                    } else {
                        // Player doesn't exist, create with default balance
                        createPlayerAccount(playerUUID, null);
                        return plugin.getConfigManager().getEconomyConfig().getDouble("starting_balance", 100.0);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get balance for player: " + playerUUID, e);
            return 0.0;
        }
    }
    
    /**
     * Sets a player's balance.
     */
    public boolean setBalance(Player player, double amount) {
        return setBalance(player.getUniqueId(), amount);
    }
    
    /**
     * Sets a player's balance by UUID.
     */
    public boolean setBalance(UUID playerUUID, double amount) {
        // Validate the amount
        if (amount < 0 || amount > MAX_BALANCE) {
            return false;
        }
        
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "UPDATE players SET balance = ?, last_updated = CURRENT_TIMESTAMP WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setBigDecimal(1, BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP));
                stmt.setString(2, playerUUID.toString());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    // Update cache
                    playerDataManager.updatePlayerBalance(playerUUID, amount);
                    return true;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to set balance for player: " + playerUUID, e);
        }
        return false;
    }
    
    /**
     * Adds money to a player's balance.
     */
    public boolean addMoney(Player player, double amount) {
        return addMoney(player.getUniqueId(), amount);
    }
    
    /**
     * Adds money to a player's balance by UUID.
     */
    public boolean addMoney(UUID playerUUID, double amount) {
        if (amount <= 0) {
            return false;
        }
        
        double currentBalance = getBalance(playerUUID);
        double newBalance = currentBalance + amount;
        
        // Check if new balance exceeds maximum
        if (newBalance > MAX_BALANCE) {
            newBalance = MAX_BALANCE;
        }
        
        return setBalance(playerUUID, newBalance);
    }
    
    /**
     * Removes money from a player's balance.
     */
    public boolean removeMoney(Player player, double amount) {
        return removeMoney(player.getUniqueId(), amount);
    }
    
    /**
     * Removes money from a player's balance by UUID.
     */
    public boolean removeMoney(UUID playerUUID, double amount) {
        if (amount <= 0) {
            return false;
        }
        
        double currentBalance = getBalance(playerUUID);
        double newBalance = currentBalance - amount;
        
        if (newBalance < 0) {
            newBalance = 0;
        }
        
        return setBalance(playerUUID, newBalance);
    }
    
    /**
     * Checks if a player has enough money.
     */
    public boolean has(Player player, double amount) {
        return has(player.getUniqueId(), amount);
    }
    
    /**
     * Checks if a player has enough money by UUID.
     */
    public boolean has(UUID playerUUID, double amount) {
        return getBalance(playerUUID) >= amount;
    }
    
    /**
     * Formats a monetary amount with proper decimal places.
     */
    public String format(double amount) {
        return String.format("%.2f", amount);
    }
    
    /**
     * Transfers money from one player to another.
     */
    public boolean transferMoney(Player from, Player to, double amount) {
        return transferMoney(from.getUniqueId(), to.getUniqueId(), amount, "money_transfer");
    }
    
    /**
     * Transfers money from one player to another by UUID.
     */
    public boolean transferMoney(UUID fromUUID, UUID toUUID, double amount, String type) {
        if (amount < MIN_TRANSACTION) {
            return false;
        }
        
        if (!has(fromUUID, amount)) {
            return false;
        }
        
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            
            try {
                // Remove money from sender
                if (!removeMoney(fromUUID, amount)) {
                    connection.rollback();
                    return false;
                }
                
                // Add money to receiver
                if (!addMoney(toUUID, amount)) {
                    connection.rollback();
                    // Restore sender's money
                    addMoney(fromUUID, amount);
                    return false;
                }
                
                // Log the transaction
                transactionLogger.logTransaction(connection, fromUUID, toUUID, amount, type, 
                    "Transfer from " + fromUUID + " to " + toUUID);
                
                connection.commit();
                return true;
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to transfer money from " + fromUUID + " to " + toUUID, e);
            return false;
        }
    }
    
    /**
     * Creates a new player account if it doesn't exist.
     */
    public boolean createPlayerAccount(UUID playerUUID, String username) {
        try (Connection connection = databaseManager.getConnection()) {
            // Check if player already exists
            String checkSql = "SELECT uuid FROM players WHERE uuid = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setString(1, playerUUID.toString());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        return true; // Player already exists
                    }
                }
            }
            
            // Create new player account
            double startingBalance = plugin.getConfigManager().getEconomyConfig().getDouble("starting_balance", 100.0);
            String insertSql = "INSERT INTO players (uuid, username, balance, bank_balance) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                insertStmt.setString(1, playerUUID.toString());
                insertStmt.setString(2, username != null ? username : "Unknown");
                insertStmt.setBigDecimal(3, BigDecimal.valueOf(startingBalance).setScale(2, RoundingMode.HALF_UP));
                insertStmt.setBigDecimal(4, BigDecimal.valueOf(0.0).setScale(2, RoundingMode.HALF_UP));
                
                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    // Create default settings for the player
                    createPlayerSettings(connection, playerUUID);
                    return true;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create player account: " + playerUUID, e);
        }
        return false;
    }
    
    /**
     * Creates default settings for a new player.
     */
    private void createPlayerSettings(Connection connection, UUID playerUUID) throws SQLException {
        String sql = "INSERT INTO player_settings (uuid, language) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, plugin.getConfigManager().getMainConfig().getString("default_language", "en_US"));
            stmt.executeUpdate();
        }
    }
    
    /**
     * Gets the top balances for leaderboard.
     */
    public java.util.List<PlayerBalance> getTopBalances(int limit) {
        java.util.List<PlayerBalance> topBalances = new java.util.ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "SELECT uuid, username, balance FROM players ORDER BY balance DESC LIMIT ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        String username = rs.getString("username");
                        double balance = rs.getDouble("balance");
                        topBalances.add(new PlayerBalance(uuid, username, balance));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get top balances", e);
        }
        
        return topBalances;
    }
    
    /**
     * Gets the total money in circulation.
     */
    public double getTotalMoney() {
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "SELECT SUM(balance) as total FROM players";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        double total = rs.getDouble("total");
                        return total > 0 ? total : 0;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get total money", e);
        }
        return 0.0;
    }
    
    /**
     * Inner class to represent player balance data.
     */
    public static class PlayerBalance {
        private final UUID uuid;
        private final String username;
        private final double balance;
        
        public PlayerBalance(UUID uuid, String username, double balance) {
            this.uuid = uuid;
            this.username = username;
            this.balance = balance;
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public String getUsername() {
            return username;
        }
        
        public double getBalance() {
            return balance;
        }
    }
    
    /**
     * Private class for handling transaction logging.
     */
    private class TransactionLogger {
        private final SMP_Economy plugin;
        
        public TransactionLogger(SMP_Economy plugin) {
            this.plugin = plugin;
        }
        
        public void logTransaction(Connection connection, UUID fromUUID, UUID toUUID, double amount, 
                                 String type, String description) throws SQLException {
            String sql = "INSERT INTO transactions (from_uuid, to_uuid, amount, type, description) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, fromUUID != null ? fromUUID.toString() : null);
                stmt.setString(2, toUUID != null ? toUUID.toString() : null);
                stmt.setBigDecimal(3, BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP));
                stmt.setString(4, type);
                stmt.setString(5, description);
                stmt.executeUpdate();
            }
        }
    }
}