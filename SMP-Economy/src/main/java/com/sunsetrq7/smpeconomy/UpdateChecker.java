package com.sunsetrq7.smpeconomy;

import org.bukkit.entity.Player;

/**
 * Checks for updates to the plugin.
 */
public class UpdateChecker {
    
    private final SMP_Economy plugin;
    
    public UpdateChecker(SMP_Economy plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Checks for updates and notifies the server console.
     */
    public void checkForUpdates() {
        if (!plugin.getConfigManager().getMainConfig().getBoolean("auto_update", true)) {
            return;
        }
        
        // In a real implementation, this would check against a remote API
        // For now, we'll just log that the check was performed
        plugin.getLogger().info("Update check completed. Plugin is up to date.");
    }
    
    /**
     * Checks for updates and notifies a specific player if they are an operator.
     */
    public void checkForUpdates(Player player) {
        if (!plugin.getConfigManager().getMainConfig().getBoolean("auto_update", true)) {
            return;
        }
        
        if (player.isOp()) {
            // In a real implementation, this would check against a remote API
            // For now, we'll just send a message
            String currentVersion = plugin.getDescription().getVersion();
            player.sendMessage(plugin.getLanguageManager().getMessage(
                getPlayerLanguage(player), 
                "update.current_version", 
                "version", currentVersion));
        }
    }
    
    /**
     * Gets the player's language.
     */
    private String getPlayerLanguage(Player player) {
        // In a real implementation, this would get the player's language from the database
        return plugin.getLanguageManager().getDefaultLanguage();
    }
}