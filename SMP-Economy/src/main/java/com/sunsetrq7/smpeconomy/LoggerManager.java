package com.sunsetrq7.smpeconomy;

import java.util.logging.Level;

/**
 * Manages advanced logging for the plugin.
 */
public class LoggerManager {
    
    private final SMP_Economy plugin;
    
    public LoggerManager(SMP_Economy plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Logs a debug message if debug mode is enabled.
     */
    public void debug(String message) {
        if (plugin.getConfigManager().getMainConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
    
    /**
     * Logs a debug message with throwable if debug mode is enabled.
     */
    public void debug(String message, Throwable throwable) {
        if (plugin.getConfigManager().getMainConfig().getBoolean("debug", false)) {
            plugin.getLogger().log(Level.INFO, "[DEBUG] " + message, throwable);
        }
    }
    
    /**
     * Logs an info message.
     */
    public void info(String message) {
        plugin.getLogger().info(message);
    }
    
    /**
     * Logs a warning message.
     */
    public void warn(String message) {
        plugin.getLogger().warning(message);
    }
    
    /**
     * Logs an error message.
     */
    public void error(String message) {
        plugin.getLogger().severe(message);
    }
    
    /**
     * Logs an error message with throwable.
     */
    public void error(String message, Throwable throwable) {
        plugin.getLogger().log(Level.SEVERE, message, throwable);
    }
    
    /**
     * Logs a message with the specified level.
     */
    public void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }
    
    /**
     * Logs a message with the specified level and throwable.
     */
    public void log(Level level, String message, Throwable throwable) {
        plugin.getLogger().log(level, message, throwable);
    }
    
    /**
     * Logs an economy transaction.
     */
    public void logEconomyTransaction(String from, String to, double amount, String type) {
        if (plugin.getConfigManager().getSecurityConfig().getBoolean("enable_transaction_logging", true)) {
            info(String.format("ECONOMY TRANSACTION: %s -> %s, Amount: %.2f, Type: %s", from, to, amount, type));
        }
    }
    
    /**
     * Logs a bank transaction.
     */
    public void logBankTransaction(String player, String action, double amount) {
        if (plugin.getConfigManager().getSecurityConfig().getBoolean("enable_transaction_logging", true)) {
            info(String.format("BANK TRANSACTION: %s, Action: %s, Amount: %.2f", player, action, amount));
        }
    }
    
    /**
     * Logs an auction event.
     */
    public void logAuctionEvent(String event, String details) {
        if (plugin.getConfigManager().getSecurityConfig().getBoolean("enable_transaction_logging", true)) {
            info(String.format("AUCTION EVENT: %s, Details: %s", event, details));
        }
    }
    
    /**
     * Logs an admin action.
     */
    public void logAdminAction(String admin, String action, String target) {
        if (plugin.getConfigManager().getSecurityConfig().getBoolean("log_admin_commands", true)) {
            info(String.format("ADMIN ACTION: %s performed '%s' on %s", admin, action, target));
        }
    }
    
    /**
     * Logs a security event.
     */
    public void logSecurityEvent(String event, String details) {
        warn(String.format("SECURITY: %s - %s", event, details));
    }
    
    /**
     * Logs a performance metric.
     */
    public void logPerformance(String operation, long durationMs) {
        if (plugin.getConfigManager().getMainConfig().getBoolean("debug", false)) {
            debug(String.format("PERFORMANCE: %s took %d ms", operation, durationMs));
        }
    }
}