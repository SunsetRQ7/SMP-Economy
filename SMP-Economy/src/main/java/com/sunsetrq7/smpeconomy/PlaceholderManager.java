package com.sunsetrq7.smpeconomy;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

/**
 * Manages PlaceholderAPI integration for the plugin.
 */
public class PlaceholderManager extends PlaceholderExpansion {
    
    private final SMP_Economy plugin;
    
    public PlaceholderManager(SMP_Economy plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getIdentifier() {
        return "smpeconomy";
    }
    
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true; // This is required for the expansion to be persistent
    }
    
    @Override
    public String onPlaceholderRequest(OfflinePlayer player, String identifier) {
        if (player == null) {
            return null;
        }
        
        switch (identifier.toLowerCase()) {
            case "balance":
                // Return the player's balance
                return plugin.getEconomyManager().format(
                    plugin.getEconomyManager().getBalance(player.getUniqueId()));
                
            case "bank_balance":
                // Return the player's bank balance
                return plugin.getBankManager().format(
                    plugin.getBankManager().getBankBalance(player.getUniqueId()));
                
            case "total_wealth":
                // Return the sum of balance and bank balance
                double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
                double bankBalance = plugin.getBankManager().getBankBalance(player.getUniqueId());
                return plugin.getEconomyManager().format(balance + bankBalance);
                
            case "rank":
                // Return the player's economy rank (position in top balances)
                return getEconomyRank(player);
                
            case "total_money":
                // Return the total money in circulation
                return plugin.getEconomyManager().format(plugin.getEconomyManager().getTotalMoney());
                
            case "total_bank_money":
                // Return the total money in all banks
                return plugin.getBankManager().format(plugin.getBankManager().getTotalBankMoney());
                
            default:
                return null; // Placeholder is unknown
        }
    }
    
    /**
     * Gets the player's economy rank (position in top balances).
     */
    private String getEconomyRank(OfflinePlayer player) {
        double playerBalance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        int rank = 1;
        
        for (EconomyManager.PlayerBalance pb : plugin.getEconomyManager().getTopBalances(1000)) {
            if (pb.getBalance() > playerBalance) {
                rank++;
            } else if (pb.getUuid().equals(player.getUniqueId())) {
                break; // Found the player
            }
        }
        
        return String.valueOf(rank);
    }
    
    /**
     * Registers the placeholder expansion.
     */
    public void registerPlaceholders() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            if (register()) {
                plugin.getLogger().info("PlaceholderAPI integration enabled.");
            } else {
                plugin.getLogger().warning("Failed to register PlaceholderAPI expansion.");
            }
        } else {
            plugin.getLogger().info("PlaceholderAPI not found. Placeholder integration disabled.");
        }
    }
    
    /**
     * Unregisters the placeholder expansion.
     */
    public void unregisterPlaceholders() {
        // PlaceholderAPI doesn't have an unregister method, so we just log
        plugin.getLogger().info("PlaceholderAPI integration disabled.");
    }
    
    /**
     * Expands a placeholder string with player context.
     */
    public String expandPlaceholders(OfflinePlayer player, String text) {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }
}