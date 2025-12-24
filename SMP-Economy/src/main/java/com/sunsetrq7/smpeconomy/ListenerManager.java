package com.sunsetrq7.smpeconomy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

/**
 * Manages event listeners for the plugin.
 */
public class ListenerManager implements Listener {
    
    private final SMP_Economy plugin;
    
    public ListenerManager(SMP_Economy plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Registers all event listeners.
     */
    public void registerListeners() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);
        
        plugin.getLogger().info("Event listeners registered successfully.");
    }
    
    /**
     * Handles player join events.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Load player data
        plugin.getPlayerDataManager().loadPlayerData(player.getUniqueId(), player.getName());
        
        // Check for updates if player is OP
        if (player.isOp()) {
            plugin.getUpdateChecker().checkForUpdates(player);
        }
    }
    
    /**
     * Handles player quit events.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Save player data
        plugin.getPlayerDataManager().removePlayerFromCache(player.getUniqueId());
    }
    
    /**
     * Handles inventory click events for GUIs.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        
        // Check if this is one of our GUIs
        if (isEconomyGUI(inventory)) {
            event.setCancelled(true);
            
            // Handle GUI clicks based on the item clicked
            if (event.getCurrentItem() != null) {
                String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
                
                // Handle specific clicks based on item name
                if (itemName.contains("Bank")) {
                    plugin.getGUIManager().openBankGUI(player);
                } else if (itemName.contains("Auction")) {
                    plugin.getGUIManager().openAuctionHouseGUI(player);
                } else if (itemName.contains("Pay")) {
                    // Open pay GUI or send message
                    player.closeInventory();
                    player.sendMessage(plugin.getLanguageManager().getMessage(
                        getPlayerLanguage(player), 
                        "gui.click.pay_instruction"));
                } else if (itemName.contains("Deposit")) {
                    // Handle deposit action
                    player.closeInventory();
                    player.sendMessage(plugin.getLanguageManager().getMessage(
                        getPlayerLanguage(player), 
                        "gui.click.deposit_instruction"));
                } else if (itemName.contains("Withdraw")) {
                    // Handle withdraw action
                    player.closeInventory();
                    player.sendMessage(plugin.getLanguageManager().getMessage(
                        getPlayerLanguage(player), 
                        "gui.click.withdraw_instruction"));
                }
            }
        }
    }
    
    /**
     * Handles inventory close events.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Perform cleanup when GUI is closed
        if (isEconomyGUI(event.getInventory())) {
            // Any cleanup needed when GUI is closed
        }
    }
    
    /**
     * Checks if an inventory is one of our economy GUIs.
     */
    private boolean isEconomyGUI(Inventory inventory) {
        String title = inventory.getTitle();
        return title.contains("Economy") || 
               title.contains("Bank") || 
               title.contains("Auction") || 
               title.contains("Auction House") ||
               title.contains("My Auctions") ||
               title.contains("Create Auction");
    }
    
    /**
     * Gets the player's language.
     */
    private String getPlayerLanguage(Player player) {
        // In a real implementation, this would get the player's language from the database
        return plugin.getLanguageManager().getDefaultLanguage();
    }
}