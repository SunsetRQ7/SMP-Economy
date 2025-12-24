package com.sunsetrq7.smpeconomy;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Manages in-game notifications for the plugin.
 */
public class NotificationManager {
    
    private final SMP_Economy plugin;
    
    public NotificationManager(SMP_Economy plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Sends a notification to a player.
     */
    public void sendNotification(Player player, String message) {
        // Apply color codes
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);
        player.sendMessage(formattedMessage);
    }
    
    /**
     * Sends a notification to a player in a specific language.
     */
    public void sendNotification(Player player, String message, String language) {
        String localizedMessage = plugin.getLanguageManager().getMessage(language, message);
        sendNotification(player, localizedMessage);
    }
    
    /**
     * Sends a notification to a player with placeholders.
     */
    public void sendNotification(Player player, String message, Object... placeholders) {
        String formattedMessage = plugin.getLanguageManager().getMessage(getPlayerLanguage(player), message, placeholders);
        sendNotification(player, formattedMessage);
    }
    
    /**
     * Sends a notification to all online players.
     */
    public void broadcastNotification(String message) {
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);
        plugin.getServer().broadcastMessage(formattedMessage);
    }
    
    /**
     * Sends a notification to all online players in a specific language.
     */
    public void broadcastNotification(String message, String language) {
        String localizedMessage = plugin.getLanguageManager().getMessage(language, message);
        broadcastNotification(localizedMessage);
    }
    
    /**
     * Sends a notification to players with a specific permission.
     */
    public void notifyPermissionHolders(String permission, String message) {
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);
        plugin.getServer().getOnlinePlayers().stream()
            .filter(player -> player.hasPermission(permission))
            .forEach(player -> player.sendMessage(formattedMessage));
    }
    
    /**
     * Gets the player's language.
     */
    private String getPlayerLanguage(Player player) {
        // In a real implementation, this would get the player's language from the database
        return plugin.getLanguageManager().getDefaultLanguage();
    }
    
    /**
     * Sends a success notification to a player.
     */
    public void sendSuccess(Player player, String message) {
        String successMessage = "&a" + message; // Green color
        sendNotification(player, successMessage);
    }
    
    /**
     * Sends an error notification to a player.
     */
    public void sendError(Player player, String message) {
        String errorMessage = "&c" + message; // Red color
        sendNotification(player, errorMessage);
    }
    
    /**
     * Sends a warning notification to a player.
     */
    public void sendWarning(Player player, String message) {
        String warningMessage = "&e" + message; // Yellow color
        sendNotification(player, warningMessage);
    }
    
    /**
     * Sends an info notification to a player.
     */
    public void sendInfo(Player player, String message) {
        String infoMessage = "&b" + message; // Blue color
        sendNotification(player, infoMessage);
    }
}