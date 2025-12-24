package com.sunsetrq7.smpeconomy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages player data loading, saving, and caching.
 */
public class PlayerDataManager {
    
    private final SMP_Economy plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, PlayerData> playerCache;
    private final Map<UUID, Long> lastSaveTime;
    
    // Cache expiration time (5 minutes in milliseconds)
    private static final long CACHE_EXPIRATION = 5 * 60 * 1000;
    
    public PlayerDataManager(SMP_Economy plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.playerCache = new ConcurrentHashMap<>();
        this.lastSaveTime = new ConcurrentHashMap<>();
    }
    
    /**
     * Initializes the player data manager.
     */
    public void initialize() {
        // Preload online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player.getUniqueId(), player.getName());
        }
        
        plugin.getLogger().info("Player data manager initialized. " + playerCache.size() + " players loaded into cache.");
    }
    
    /**
     * Loads player data from the database.
     */
    public PlayerData loadPlayerData(UUID playerUUID, String username) {
        // Check if player data is already in cache
        PlayerData cachedData = playerCache.get(playerUUID);
        if (cachedData != null) {
            // Check if cache is still valid
            Long lastSave = lastSaveTime.get(playerUUID);
            if (lastSave != null && System.currentTimeMillis() - lastSave < CACHE_EXPIRATION) {
                return cachedData;
            }
        }
        
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "SELECT uuid, username, balance, bank_balance, total_earned, total_spent, created_at, last_seen FROM players WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUUID.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        PlayerData data = new PlayerData(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("username"),
                            rs.getDouble("balance"),
                            rs.getDouble("bank_balance"),
                            rs.getDouble("total_earned"),
                            rs.getDouble("total_spent"),
                            rs.getTimestamp("created_at"),
                            rs.getTimestamp("last_seen")
                        );
                        
                        // Update cache
                        playerCache.put(playerUUID, data);
                        lastSaveTime.put(playerUUID, System.currentTimeMillis());
                        
                        return data;
                    } else {
                        // Player doesn't exist, create with default values
                        return createNewPlayer(playerUUID, username);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data: " + playerUUID, e);
            return createNewPlayer(playerUUID, username);
        }
    }
    
    /**
     * Creates a new player with default values.
     */
    private PlayerData createNewPlayer(UUID playerUUID, String username) {
        double startingBalance = plugin.getConfigManager().getEconomyConfig().getDouble("starting_balance", 100.0);
        
        PlayerData data = new PlayerData(
            playerUUID,
            username != null ? username : "Unknown",
            startingBalance,
            0.0, // Bank balance starts at 0
            0.0, // Total earned starts at 0
            0.0, // Total spent starts at 0
            new java.sql.Timestamp(System.currentTimeMillis()), // Created at
            new java.sql.Timestamp(System.currentTimeMillis()) // Last seen
        );
        
        // Save to database
        savePlayerData(data);
        
        // Update cache
        playerCache.put(playerUUID, data);
        lastSaveTime.put(playerUUID, System.currentTimeMillis());
        
        return data;
    }
    
    /**
     * Saves player data to the database.
     */
    public boolean savePlayerData(PlayerData data) {
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "UPDATE players SET username = ?, balance = ?, bank_balance = ?, " +
                        "total_earned = ?, total_spent = ?, last_seen = CURRENT_TIMESTAMP, " +
                        "last_updated = CURRENT_TIMESTAMP WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, data.getUsername());
                stmt.setDouble(2, data.getBalance());
                stmt.setDouble(3, data.getBankBalance());
                stmt.setDouble(4, data.getTotalEarned());
                stmt.setDouble(5, data.getTotalSpent());
                stmt.setString(6, data.getUuid().toString());
                
                boolean success = stmt.executeUpdate() > 0;
                if (success) {
                    // Update cache timestamp
                    lastSaveTime.put(data.getUuid(), System.currentTimeMillis());
                }
                return success;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data: " + data.getUuid(), e);
            return false;
        }
    }
    
    /**
     * Saves all player data to the database.
     */
    public void saveAllPlayers() {
        plugin.getLogger().info("Saving all player data to database...");
        
        int savedCount = 0;
        for (Map.Entry<UUID, PlayerData> entry : playerCache.entrySet()) {
            if (savePlayerData(entry.getValue())) {
                savedCount++;
            }
        }
        
        plugin.getLogger().info("Saved " + savedCount + " player records to database.");
    }
    
    /**
     * Updates a player's balance in the cache.
     */
    public void updatePlayerBalance(UUID playerUUID, double newBalance) {
        PlayerData data = playerCache.get(playerUUID);
        if (data != null) {
            // Update the balance in the cached data
            PlayerData updatedData = new PlayerData(
                data.getUuid(),
                data.getUsername(),
                newBalance,
                data.getBankBalance(),
                data.getTotalEarned(),
                data.getTotalSpent(),
                data.getCreatedAt(),
                data.getLastSeen()
            );
            
            playerCache.put(playerUUID, updatedData);
            lastSaveTime.put(playerUUID, System.currentTimeMillis());
        }
    }
    
    /**
     * Updates a player's bank balance in the cache.
     */
    public void updatePlayerBankBalance(UUID playerUUID, double newBankBalance) {
        PlayerData data = playerCache.get(playerUUID);
        if (data != null) {
            // Update the bank balance in the cached data
            PlayerData updatedData = new PlayerData(
                data.getUuid(),
                data.getUsername(),
                data.getBalance(),
                newBankBalance,
                data.getTotalEarned(),
                data.getTotalSpent(),
                data.getCreatedAt(),
                data.getLastSeen()
            );
            
            playerCache.put(playerUUID, updatedData);
            lastSaveTime.put(playerUUID, System.currentTimeMillis());
        }
    }
    
    /**
     * Gets player data from cache (does not load from database).
     */
    public PlayerData getPlayerDataFromCache(UUID playerUUID) {
        return playerCache.get(playerUUID);
    }
    
    /**
     * Removes a player from the cache (typically when they log out).
     */
    public void removePlayerFromCache(UUID playerUUID) {
        playerCache.remove(playerUUID);
        lastSaveTime.remove(playerUUID);
    }
    
    /**
     * Gets the size of the player cache.
     */
    public int getCacheSize() {
        return playerCache.size();
    }
    
    /**
     * Clears the player cache.
     */
    public void clearCache() {
        playerCache.clear();
        lastSaveTime.clear();
    }
    
    /**
     * Inner class to represent player data.
     */
    public static class PlayerData {
        private final UUID uuid;
        private final String username;
        private final double balance;
        private final double bankBalance;
        private final double totalEarned;
        private final double totalSpent;
        private final java.sql.Timestamp createdAt;
        private final java.sql.Timestamp lastSeen;
        
        public PlayerData(UUID uuid, String username, double balance, double bankBalance, 
                         double totalEarned, double totalSpent, java.sql.Timestamp createdAt, 
                         java.sql.Timestamp lastSeen) {
            this.uuid = uuid;
            this.username = username;
            this.balance = balance;
            this.bankBalance = bankBalance;
            this.totalEarned = totalEarned;
            this.totalSpent = totalSpent;
            this.createdAt = createdAt;
            this.lastSeen = lastSeen;
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
        
        public double getBankBalance() {
            return bankBalance;
        }
        
        public double getTotalEarned() {
            return totalEarned;
        }
        
        public double getTotalSpent() {
            return totalSpent;
        }
        
        public java.sql.Timestamp getCreatedAt() {
            return createdAt;
        }
        
        public java.sql.Timestamp getLastSeen() {
            return lastSeen;
        }
        
        /**
         * Creates a new PlayerData instance with an updated balance.
         */
        public PlayerData withBalance(double newBalance) {
            return new PlayerData(uuid, username, newBalance, bankBalance, totalEarned, totalSpent, createdAt, lastSeen);
        }
        
        /**
         * Creates a new PlayerData instance with an updated bank balance.
         */
        public PlayerData withBankBalance(double newBankBalance) {
            return new PlayerData(uuid, username, balance, newBankBalance, totalEarned, totalSpent, createdAt, lastSeen);
        }
        
        /**
         * Creates a new PlayerData instance with updated earned amount.
         */
        public PlayerData withTotalEarned(double newTotalEarned) {
            return new PlayerData(uuid, username, balance, bankBalance, newTotalEarned, totalSpent, createdAt, lastSeen);
        }
        
        /**
         * Creates a new PlayerData instance with updated spent amount.
         */
        public PlayerData withTotalSpent(double newTotalSpent) {
            return new PlayerData(uuid, username, balance, bankBalance, totalEarned, newTotalSpent, createdAt, lastSeen);
        }
    }
}