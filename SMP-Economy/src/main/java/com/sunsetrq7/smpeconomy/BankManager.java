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
 * Manages the banking system including deposits, withdrawals, and interest.
 */
public class BankManager {
    
    private final SMP_Economy plugin;
    private final DatabaseManager databaseManager;
    private final PlayerDataManager playerDataManager;
    private final EconomyManager economyManager;
    
    // Constants for banking configuration
    private static final double MAX_DAILY_DEPOSIT = 1_000_000.0; // 1 million
    private static final double MAX_WEEKLY_DEPOSIT = 10_000_000.0; // 10 million
    
    public BankManager(SMP_Economy plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.playerDataManager = plugin.getPlayerDataManager();
        this.economyManager = plugin.getEconomyManager();
    }
    
    /**
     * Gets a player's bank balance.
     */
    public double getBankBalance(Player player) {
        return getBankBalance(player.getUniqueId());
    }
    
    /**
     * Gets a player's bank balance by UUID.
     */
    public double getBankBalance(UUID playerUUID) {
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "SELECT bank_balance FROM players WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUUID.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("bank_balance");
                    } else {
                        // Player doesn't exist, create account and return 0
                        plugin.getEconomyManager().createPlayerAccount(playerUUID, null);
                        return 0.0;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get bank balance for player: " + playerUUID, e);
            return 0.0;
        }
    }
    
    /**
     * Sets a player's bank balance.
     */
    public boolean setBankBalance(Player player, double amount) {
        return setBankBalance(player.getUniqueId(), amount);
    }
    
    /**
     * Sets a player's bank balance by UUID.
     */
    public boolean setBankBalance(UUID playerUUID, double amount) {
        // Validate the amount
        if (amount < 0) {
            return false;
        }
        
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "UPDATE players SET bank_balance = ?, last_updated = CURRENT_TIMESTAMP WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setBigDecimal(1, BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP));
                stmt.setString(2, playerUUID.toString());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    // Update cache
                    playerDataManager.updatePlayerBankBalance(playerUUID, amount);
                    return true;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to set bank balance for player: " + playerUUID, e);
        }
        return false;
    }
    
    /**
     * Adds money to a player's bank balance.
     */
    public boolean addBankMoney(Player player, double amount) {
        return addBankMoney(player.getUniqueId(), amount);
    }
    
    /**
     * Adds money to a player's bank balance by UUID.
     */
    public boolean addBankMoney(UUID playerUUID, double amount) {
        if (amount <= 0) {
            return false;
        }
        
        double currentBalance = getBankBalance(playerUUID);
        double newBalance = currentBalance + amount;
        
        return setBankBalance(playerUUID, newBalance);
    }
    
    /**
     * Removes money from a player's bank balance.
     */
    public boolean removeBankMoney(Player player, double amount) {
        return removeBankMoney(player.getUniqueId(), amount);
    }
    
    /**
     * Removes money from a player's bank balance by UUID.
     */
    public boolean removeBankMoney(UUID playerUUID, double amount) {
        if (amount <= 0) {
            return false;
        }
        
        double currentBalance = getBankBalance(playerUUID);
        double newBalance = currentBalance - amount;
        
        if (newBalance < 0) {
            newBalance = 0;
        }
        
        return setBankBalance(playerUUID, newBalance);
    }
    
    /**
     * Checks if a player has enough money in their bank.
     */
    public boolean hasBank(Player player, double amount) {
        return hasBank(player.getUniqueId(), amount);
    }
    
    /**
     * Checks if a player has enough money in their bank by UUID.
     */
    public boolean hasBank(UUID playerUUID, double amount) {
        return getBankBalance(playerUUID) >= amount;
    }
    
    /**
     * Deposits money from player's balance to their bank.
     */
    public boolean depositToBank(Player player, double amount) {
        return depositToBank(player.getUniqueId(), amount);
    }
    
    /**
     * Deposits money from player's balance to their bank by UUID.
     */
    public boolean depositToBank(UUID playerUUID, double amount) {
        // Check if player has enough money
        if (!economyManager.has(playerUUID, amount)) {
            return false;
        }
        
        // Check deposit limits
        if (!isWithinDepositLimits(playerUUID, amount)) {
            return false;
        }
        
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            
            try {
                // Remove money from player's balance
                if (!economyManager.removeMoney(playerUUID, amount)) {
                    connection.rollback();
                    return false;
                }
                
                // Add money to player's bank
                if (!addBankMoney(playerUUID, amount)) {
                    connection.rollback();
                    // Restore player's balance
                    economyManager.addMoney(playerUUID, amount);
                    return false;
                }
                
                // Log the transaction
                logTransaction(connection, playerUUID, amount, "bank_deposit", 
                    "Deposit to bank from " + playerUUID);
                
                connection.commit();
                return true;
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to deposit to bank for player: " + playerUUID, e);
            return false;
        }
    }
    
    /**
     * Withdraws money from player's bank to their balance.
     */
    public boolean withdrawFromBank(Player player, double amount) {
        return withdrawFromBank(player.getUniqueId(), amount);
    }
    
    /**
     * Withdraws money from player's bank to their balance by UUID.
     */
    public boolean withdrawFromBank(UUID playerUUID, double amount) {
        // Check if player has enough money in bank
        if (!hasBank(playerUUID, amount)) {
            return false;
        }
        
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            
            try {
                // Remove money from player's bank
                if (!removeBankMoney(playerUUID, amount)) {
                    connection.rollback();
                    return false;
                }
                
                // Add money to player's balance
                if (!economyManager.addMoney(playerUUID, amount)) {
                    connection.rollback();
                    // Restore player's bank balance
                    addBankMoney(playerUUID, amount);
                    return false;
                }
                
                // Log the transaction
                logTransaction(connection, playerUUID, amount, "bank_withdrawal", 
                    "Withdrawal from bank to " + playerUUID);
                
                connection.commit();
                return true;
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to withdraw from bank for player: " + playerUUID, e);
            return false;
        }
    }
    
    /**
     * Checks if a deposit is within the limits.
     */
    private boolean isWithinDepositLimits(UUID playerUUID, double amount) {
        // Get the daily deposit limit from config
        double dailyLimit = plugin.getConfigManager().getBankConfig().getDouble("daily_deposit_limit", MAX_DAILY_DEPOSIT);
        
        // Check if the deposit amount exceeds the daily limit
        if (amount > dailyLimit) {
            return false;
        }
        
        // Additional checks can be implemented here (e.g., tracking actual daily deposits)
        return true;
    }
    
    /**
     * Calculates and applies interest to all player accounts.
     */
    public void calculateAndApplyInterest() {
        double interestRate = plugin.getConfigManager().getBankConfig().getDouble("interest_rate", 0.1); // Default 0.1%
        double minBalanceForInterest = plugin.getConfigManager().getBankConfig().getDouble("min_balance_for_interest", 1000.0);
        
        try (Connection connection = databaseManager.getConnection()) {
            // Get all players with bank balance above the minimum
            String sql = "SELECT uuid, bank_balance FROM players WHERE bank_balance >= ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setDouble(1, minBalanceForInterest);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String uuidStr = rs.getString("uuid");
                        UUID playerUUID = UUID.fromString(uuidStr);
                        double currentBalance = rs.getDouble("bank_balance");
                        
                        // Calculate interest
                        double interest = currentBalance * (interestRate / 100);
                        
                        // Apply interest
                        addBankMoney(playerUUID, interest);
                        
                        // Log the interest transaction
                        logTransaction(connection, playerUUID, interest, "bank_interest", 
                            "Interest earned: " + plugin.getEconomyManager().format(interest));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to calculate and apply bank interest", e);
        }
    }
    
    /**
     * Logs a banking transaction.
     */
    private void logTransaction(Connection connection, UUID playerUUID, double amount, 
                               String type, String description) throws SQLException {
        String sql = "INSERT INTO transactions (from_uuid, to_uuid, amount, type, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, playerUUID.toString()); // Same player for bank transactions
            stmt.setBigDecimal(3, BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP));
            stmt.setString(4, type);
            stmt.setString(5, description);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Gets the total money in all banks.
     */
    public double getTotalBankMoney() {
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "SELECT SUM(bank_balance) as total FROM players";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        double total = rs.getDouble("total");
                        return total > 0 ? total : 0;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get total bank money", e);
        }
        return 0.0;
    }
    
    /**
     * Formats a monetary amount with proper decimal places.
     */
    public String format(double amount) {
        return economyManager.format(amount);
    }
}