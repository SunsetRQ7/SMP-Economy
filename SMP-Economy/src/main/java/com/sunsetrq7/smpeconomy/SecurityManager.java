package com.sunsetrq7.smpeconomy;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages security features and anti-exploit protection.
 */
public class SecurityManager {
    
    private final SMP_Economy plugin;
    
    // Rate limiting
    private final Map<UUID, Integer> commandUsageCount;
    private final Map<UUID, Long> commandUsageTime;
    
    // Transaction tracking
    private final Map<UUID, Double> transactionAmountThisMinute;
    private final Map<UUID, Integer> transactionCountThisMinute;
    
    public SecurityManager(SMP_Economy plugin) {
        this.plugin = plugin;
        this.commandUsageCount = new HashMap<>();
        this.commandUsageTime = new HashMap<>();
        this.transactionAmountThisMinute = new HashMap<>();
        this.transactionCountThisMinute = new HashMap<>();
    }
    
    /**
     * Checks if a player can execute an economy command based on rate limits.
     */
    public boolean canExecuteCommand(Player player) {
        if (!plugin.getConfigManager().getSecurityConfig().getBoolean("enable_anti_exploit", true)) {
            return true;
        }
        
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long timeWindow = 60000; // 1 minute
        
        // Check if we need to reset the counters
        Long lastTime = commandUsageTime.get(playerId);
        if (lastTime == null || (currentTime - lastTime) > timeWindow) {
            commandUsageCount.put(playerId, 1);
            commandUsageTime.put(playerId, currentTime);
            return true;
        }
        
        // Increment count and check limits
        int currentCount = commandUsageCount.getOrDefault(playerId, 0) + 1;
        commandUsageCount.put(playerId, currentCount);
        
        int maxCommandsPerMinute = plugin.getConfigManager().getSecurityConfig().getInt("max_transactions_per_minute", 10);
        
        return currentCount <= maxCommandsPerMinute;
    }
    
    /**
     * Validates a transaction amount.
     */
    public boolean isValidTransactionAmount(double amount) {
        double minTransaction = plugin.getConfigManager().getEconomyConfig().getDouble("min_transaction", 0.01);
        double maxBalance = plugin.getConfigManager().getEconomyConfig().getDouble("max_balance", 1000000000.0);
        
        return amount >= minTransaction && amount <= maxBalance;
    }
    
    /**
     * Checks if a transaction is allowed based on rate limits.
     */
    public boolean isTransactionAllowed(Player player, double amount) {
        if (!plugin.getConfigManager().getSecurityConfig().getBoolean("enable_anti_exploit", true)) {
            return true;
        }
        
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long timeWindow = 60000; // 1 minute
        
        // Check transaction count
        Long lastTransactionTime = transactionCountThisMinute.get(playerId);
        if (lastTransactionTime == null || (currentTime - lastTransactionTime) > timeWindow) {
            transactionCountThisMinute.put(playerId, 1);
            transactionAmountThisMinute.put(playerId, amount);
            return true;
        }
        
        // Increment counters
        int currentCount = transactionCountThisMinute.getOrDefault(playerId, 0) + 1;
        double currentAmount = transactionAmountThisMinute.getOrDefault(playerId, 0.0) + amount;
        
        transactionCountThisMinute.put(playerId, currentCount);
        transactionAmountThisMinute.put(playerId, currentAmount);
        
        int maxTransactionsPerMinute = plugin.getConfigManager().getSecurityConfig().getInt("max_transactions_per_minute", 10);
        double maxAmountPerMinute = plugin.getConfigManager().getSecurityConfig().getDouble("max_amount_per_minute", 1000000.0);
        
        return currentCount <= maxTransactionsPerMinute && currentAmount <= maxAmountPerMinute;
    }
    
    /**
     * Validates user input to prevent injection attacks.
     */
    public boolean isValidInput(String input) {
        if (input == null) {
            return false;
        }
        
        // Check for SQL injection patterns
        String lowerInput = input.toLowerCase();
        String[] sqlPatterns = {"'", "\"", ";", "--", "/*", "*/", "xp_", "exec", "select", "insert", "update", "delete", "drop", "create", "alter", "union", "script"};
        
        for (String pattern : sqlPatterns) {
            if (lowerInput.contains(pattern)) {
                return false;
            }
        }
        
        // Check for command injection patterns
        String[] cmdPatterns = {"|", "&", "$", "`", "\\", "(", ")"};
        for (String pattern : cmdPatterns) {
            if (input.contains(pattern)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Logs a security violation.
     */
    public void logSecurityViolation(Player player, String violationType, String details) {
        if (plugin.getConfigManager().getSecurityConfig().getBoolean("log_admin_commands", true)) {
            plugin.getLogger().log(Level.WARNING, 
                "Security violation by " + player.getName() + " (" + player.getUniqueId() + "): " + 
                violationType + " - " + details);
            
            // Optionally notify admins
            notifyAdmins(player, violationType, details);
        }
    }
    
    /**
     * Notifies admins about a security violation.
     */
    private void notifyAdmins(Player player, String violationType, String details) {
        plugin.getServer().getOnlinePlayers().stream()
            .filter(p -> p.hasPermission("smpeconomy.admin"))
            .forEach(admin -> {
                admin.sendMessage(plugin.getLanguageManager().getMessage(
                    getPlayerLanguage(admin), 
                    "security.violation_notify",
                    "player", player.getName(),
                    "violation", violationType,
                    "details", details));
            });
    }
    
    /**
     * Checks if a player has permission to perform an action.
     */
    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }
    
    /**
     * Gets the player's language.
     */
    private String getPlayerLanguage(Player player) {
        // In a real implementation, this would get the player's language from the database
        return plugin.getLanguageManager().getDefaultLanguage();
    }
    
    /**
     * Resets security tracking data periodically.
     */
    public void resetTrackingData() {
        long currentTime = System.currentTimeMillis();
        long timeWindow = 60000; // 1 minute
        
        // Clean up old entries
        commandUsageTime.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > timeWindow);
        
        transactionCountThisMinute.entrySet().removeIf(entry -> 
            (currentTime - entry.getKey().getLeastSignificantBits()) > timeWindow); // This is a simplified approach
    }
}