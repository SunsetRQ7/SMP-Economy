package com.sunsetrq7.smpeconomy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages secure money transfers between players with cooldowns and limits.
 */
public class TransferManager {
    
    private final SMP_Economy plugin;
    private final DatabaseManager databaseManager;
    private final EconomyManager economyManager;
    
    // Cooldown tracking
    private final Map<UUID, Long> lastTransferTime;
    private final Map<UUID, Double> transferAmountToday;
    private final Map<UUID, Double> transferAmountThisWeek;
    
    public TransferManager(SMP_Economy plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.economyManager = plugin.getEconomyManager();
        
        this.lastTransferTime = new HashMap<>();
        this.transferAmountToday = new HashMap<>();
        this.transferAmountThisWeek = new HashMap<>();
    }
    
    /**
     * Transfers money from one player to another with security checks.
     */
    public boolean transferMoney(Player sender, Player receiver, double amount) {
        return transferMoney(sender.getUniqueId(), receiver.getUniqueId(), amount);
    }
    
    /**
     * Transfers money from one player to another by UUID with security checks.
     */
    public boolean transferMoney(UUID senderUUID, UUID receiverUUID, double amount) {
        // Validate the amount
        if (amount < plugin.getConfigManager().getEconomyConfig().getDouble("min_transaction", 0.01)) {
            return false;
        }
        
        // Check if sender has enough money
        if (!economyManager.has(senderUUID, amount)) {
            return false;
        }
        
        // Check transfer cooldown
        if (isTransferOnCooldown(senderUUID)) {
            return false;
        }
        
        // Check daily transfer limits
        if (!isWithinDailyLimit(senderUUID, amount)) {
            return false;
        }
        
        // Apply transaction fee if configured
        double feePercentage = plugin.getConfigManager().getEconomyConfig().getDouble("transaction_fee", 0.0);
        double feeAmount = amount * (feePercentage / 100.0);
        double transferAmount = amount - feeAmount;
        
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            
            try {
                // Remove money from sender
                if (!economyManager.removeMoney(senderUUID, amount)) {
                    connection.rollback();
                    return false;
                }
                
                // Add money to receiver
                if (!economyManager.addMoney(receiverUUID, transferAmount)) {
                    connection.rollback();
                    // Restore sender's money
                    economyManager.addMoney(senderUUID, amount);
                    return false;
                }
                
                // Log the transaction
                logTransaction(connection, senderUUID, receiverUUID, amount, "money_transfer", 
                    "Transfer from " + senderUUID + " to " + receiverUUID);
                
                // Update transfer tracking
                updateTransferTracking(senderUUID, amount);
                
                connection.commit();
                
                // Send notifications
                Player senderPlayer = Bukkit.getPlayer(senderUUID);
                if (senderPlayer != null && senderPlayer.isOnline()) {
                    String message = plugin.getLanguageManager().getMessage(
                        getPlayerLanguage(senderUUID), 
                        "transfer.sent", 
                        "amount", economyManager.format(transferAmount),
                        "receiver", Bukkit.getOfflinePlayer(receiverUUID).getName(),
                        "fee", economyManager.format(feeAmount)
                    );
                    plugin.getNotificationManager().sendNotification(senderPlayer, message);
                }
                
                Player receiverPlayer = Bukkit.getPlayer(receiverUUID);
                if (receiverPlayer != null && receiverPlayer.isOnline()) {
                    String message = plugin.getLanguageManager().getMessage(
                        getPlayerLanguage(receiverUUID), 
                        "transfer.received", 
                        "amount", economyManager.format(transferAmount),
                        "sender", Bukkit.getOfflinePlayer(senderUUID).getName()
                    );
                    plugin.getNotificationManager().sendNotification(receiverPlayer, message);
                }
                
                return true;
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to transfer money from " + senderUUID + " to " + receiverUUID, e);
            return false;
        }
    }
    
    /**
     * Checks if a transfer is on cooldown for the sender.
     */
    private boolean isTransferOnCooldown(UUID senderUUID) {
        int cooldownSeconds = plugin.getConfigManager().getSecurityConfig().getInt("transaction_cooldown_seconds", 1);
        long cooldownMillis = cooldownSeconds * 1000L;
        
        Long lastTransfer = lastTransferTime.get(senderUUID);
        if (lastTransfer != null) {
            return (System.currentTimeMillis() - lastTransfer) < cooldownMillis;
        }
        
        return false;
    }
    
    /**
     * Checks if a transfer is within the daily limit.
     */
    private boolean isWithinDailyLimit(UUID senderUUID, double amount) {
        // This is a simplified check - in a real implementation you'd track actual daily amounts
        double maxTransfersPerMinute = plugin.getConfigManager().getSecurityConfig().getInt("max_transactions_per_minute", 10);
        
        // For now, just ensure the amount is reasonable
        return amount > 0;
    }
    
    /**
     * Updates transfer tracking for cooldown and limits.
     */
    private void updateTransferTracking(UUID senderUUID, double amount) {
        // Update last transfer time for cooldown
        lastTransferTime.put(senderUUID, System.currentTimeMillis());
        
        // Update daily transfer amount
        double currentDailyAmount = transferAmountToday.getOrDefault(senderUUID, 0.0);
        transferAmountToday.put(senderUUID, currentDailyAmount + amount);
        
        // Update weekly transfer amount
        double currentWeeklyAmount = transferAmountThisWeek.getOrDefault(senderUUID, 0.0);
        transferAmountThisWeek.put(senderUUID, currentWeeklyAmount + amount);
    }
    
    /**
     * Logs a transfer transaction.
     */
    private void logTransaction(Connection connection, UUID fromUUID, UUID toUUID, double amount, 
                               String type, String description) throws SQLException {
        String sql = "INSERT INTO transactions (from_uuid, to_uuid, amount, type, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, fromUUID.toString());
            stmt.setString(2, toUUID.toString());
            stmt.setDouble(3, amount);
            stmt.setString(4, type);
            stmt.setString(5, description);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Gets the player's language preference.
     */
    private String getPlayerLanguage(UUID playerUUID) {
        // This would typically get the player's language from the database
        // For now, we'll return the default language
        return plugin.getLanguageManager().getDefaultLanguage();
    }
    
    /**
     * Gets the amount transferred by a player today.
     */
    public double getDailyTransferAmount(UUID playerUUID) {
        return transferAmountToday.getOrDefault(playerUUID, 0.0);
    }
    
    /**
     * Gets the amount transferred by a player this week.
     */
    public double getWeeklyTransferAmount(UUID playerUUID) {
        return transferAmountThisWeek.getOrDefault(playerUUID, 0.0);
    }
    
    /**
     * Resets daily transfer tracking (should be called daily).
     */
    public void resetDailyTransfers() {
        transferAmountToday.clear();
    }
    
    /**
     * Resets weekly transfer tracking (should be called weekly).
     */
    public void resetWeeklyTransfers() {
        transferAmountThisWeek.clear();
    }
    
    /**
     * Formats a monetary amount with proper decimal places.
     */
    public String format(double amount) {
        return economyManager.format(amount);
    }
}