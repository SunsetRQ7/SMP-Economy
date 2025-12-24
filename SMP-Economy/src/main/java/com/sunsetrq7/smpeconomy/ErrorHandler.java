package com.sunsetrq7.smpeconomy;

import java.util.logging.Level;

/**
 * Manages error handling and crash reporting for the plugin.
 */
public class ErrorHandler {
    
    private final SMP_Economy plugin;
    
    public ErrorHandler(SMP_Economy plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Logs an error with the plugin's logger.
     */
    public void logError(String message, Throwable throwable) {
        plugin.getLogger().log(Level.SEVERE, message, throwable);
    }
    
    /**
     * Logs a warning with the plugin's logger.
     */
    public void logWarning(String message) {
        plugin.getLogger().log(Level.WARNING, message);
    }
    
    /**
     * Logs an info message with the plugin's logger.
     */
    public void logInfo(String message) {
        plugin.getLogger().info(message);
    }
    
    /**
     * Handles a recoverable error by logging and optionally notifying admins.
     */
    public void handleRecoverableError(String context, String message, Throwable throwable) {
        String fullMessage = "Recoverable error in " + context + ": " + message;
        logError(fullMessage, throwable);
        
        // Optionally notify online admins
        notifyAdmins(fullMessage);
    }
    
    /**
     * Handles a critical error that may affect plugin functionality.
     */
    public void handleCriticalError(String context, String message, Throwable throwable) {
        String fullMessage = "CRITICAL ERROR in " + context + ": " + message;
        logError(fullMessage, throwable);
        
        // Notify online admins immediately
        notifyAdmins(fullMessage);
        
        // Potentially disable certain features or the entire plugin
        // depending on the severity of the error
    }
    
    /**
     * Handles a database error.
     */
    public void handleDatabaseError(String operation, Throwable throwable) {
        String message = "Database error during " + operation + ": " + throwable.getMessage();
        logError(message, throwable);
        
        // Notify admins about database issues
        notifyAdmins("Database error: " + message);
    }
    
    /**
     * Handles a configuration error.
     */
    public void handleConfigError(String configName, String message) {
        String fullMessage = "Configuration error in " + configName + ": " + message;
        logError(fullMessage, null);
        
        // Notify admins about config issues
        notifyAdmins(fullMessage);
    }
    
    /**
     * Handles a security-related error.
     */
    public void handleSecurityError(String context, String message) {
        String fullMessage = "Security issue in " + context + ": " + message;
        logError(fullMessage, null);
        
        // Always notify admins about security issues
        notifyAdmins(fullMessage);
    }
    
    /**
     * Notifies online admins about an issue.
     */
    private void notifyAdmins(String message) {
        plugin.getServer().getOnlinePlayers().stream()
            .filter(player -> player.hasPermission("smpeconomy.admin"))
            .forEach(player -> {
                player.sendMessage("§c[SMPEconomy] §4" + message);
            });
    }
    
    /**
     * Reports an exception with additional context.
     */
    public void reportException(Exception e, String context) {
        String message = "Exception in " + context + ": " + e.getMessage();
        logError(message, e);
        
        // Print stack trace for debugging
        if (plugin.getConfigManager().getMainConfig().getBoolean("debug", false)) {
            e.printStackTrace();
        }
    }
    
    /**
     * Handles a general exception with proper logging.
     */
    public void handleException(Exception e, String context, String userMessage) {
        // Log the full exception
        reportException(e, context);
        
        // Send a user-friendly message if provided
        if (userMessage != null) {
            notifyAdmins(userMessage);
        }
    }
}