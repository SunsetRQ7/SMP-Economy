package com.sunsetrq7.smpeconomy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all GUI systems for the plugin.
 */
public class GUIManager {
    
    private final SMP_Economy plugin;
    
    public GUIManager(SMP_Economy plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Opens the main economy GUI for a player.
     */
    public void openMainEconomyGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, 
            ChatColor.translateAlternateColorCodes('&', 
                plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.main.title")));
        
        // Add balance display item
        ItemStack balanceItem = createItem(Material.GOLD_INGOT, 
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.main.balance.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.main.balance.lore",
                "balance", plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player))));
        
        gui.setItem(10, balanceItem);
        
        // Add bank balance display item
        ItemStack bankItem = createItem(Material.BOOK, 
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.main.bank.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.main.bank.lore",
                "balance", plugin.getBankManager().format(plugin.getBankManager().getBankBalance(player))));
        
        gui.setItem(12, bankItem);
        
        // Add top balances display
        List<EconomyManager.PlayerBalance> topBalances = plugin.getEconomyManager().getTopBalances(5);
        for (int i = 0; i < Math.min(topBalances.size(), 5); i++) {
            EconomyManager.PlayerBalance pb = topBalances.get(i);
            ItemStack topItem = createItem(Material.PLAYER_HEAD,
                plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.main.top.name", 
                    "position", String.valueOf(i + 1), 
                    "player", pb.getUsername()),
                plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.main.top.lore", 
                    "balance", plugin.getEconomyManager().format(pb.getBalance())));
            
            // This would require setting the player head texture in a real implementation
            gui.setItem(20 + i, topItem);
        }
        
        // Add quick action buttons
        ItemStack payButton = createItem(Material.EMERALD,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.main.pay.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.main.pay.lore"));
        
        gui.setItem(30, payButton);
        
        ItemStack bankButton = createItem(Material.CHEST,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.main.bank.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.main.bank.lore"));
        
        gui.setItem(32, bankButton);
        
        ItemStack auctionButton = createItem(Material.ANVIL,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.main.auction.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.main.auction.lore"));
        
        gui.setItem(40, auctionButton);
        
        player.openInventory(gui);
    }
    
    /**
     * Opens the bank GUI for a player.
     */
    public void openBankGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, 
            ChatColor.translateAlternateColorCodes('&', 
                plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.title")));
        
        // Display current balances
        ItemStack balanceItem = createItem(Material.GOLD_INGOT, 
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.player_balance.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.player_balance.lore",
                "balance", plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player))));
        
        gui.setItem(10, balanceItem);
        
        ItemStack bankBalanceItem = createItem(Material.BOOK, 
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.bank_balance.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.bank_balance.lore",
                "balance", plugin.getBankManager().format(plugin.getBankManager().getBankBalance(player))));
        
        gui.setItem(12, bankBalanceItem);
        
        // Deposit and withdraw buttons
        ItemStack depositButton = createItem(Material.GREEN_STAINED_GLASS_PANE,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.deposit.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.deposit.lore"));
        
        gui.setItem(20, depositButton);
        
        ItemStack withdrawButton = createItem(Material.RED_STAINED_GLASS_PANE,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.withdraw.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.withdraw.lore"));
        
        gui.setItem(22, withdrawButton);
        
        // Quick deposit/withdraw buttons
        ItemStack quickDeposit100 = createItem(Material.PAPER,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.quick_deposit.name", "amount", "100"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.quick_deposit.lore"));
        
        gui.setItem(30, quickDeposit100);
        
        ItemStack quickDeposit1000 = createItem(Material.PAPER,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.quick_deposit.name", "amount", "1000"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.quick_deposit.lore"));
        
        gui.setItem(31, quickDeposit1000);
        
        ItemStack quickDeposit10000 = createItem(Material.PAPER,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.quick_deposit.name", "amount", "10000"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.quick_deposit.lore"));
        
        gui.setItem(32, quickDeposit10000);
        
        ItemStack quickWithdraw100 = createItem(Material.PAPER,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.quick_withdraw.name", "amount", "100"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.quick_withdraw.lore"));
        
        gui.setItem(40, quickWithdraw100);
        
        ItemStack quickWithdraw1000 = createItem(Material.PAPER,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.quick_withdraw.name", "amount", "1000"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.quick_withdraw.lore"));
        
        gui.setItem(41, quickWithdraw1000);
        
        ItemStack quickWithdraw10000 = createItem(Material.PAPER,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.quick_withdraw.name", "amount", "10000"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.bank.quick_withdraw.lore"));
        
        gui.setItem(42, quickWithdraw10000);
        
        player.openInventory(gui);
    }
    
    /**
     * Opens the auction house GUI for a player.
     */
    public void openAuctionHouseGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, 
            ChatColor.translateAlternateColorCodes('&', 
                plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.title")));
        
        // Display active auctions
        List<AuctionHouseManager.Auction> auctions = plugin.getAuctionHouseManager().getActiveAuctions();
        
        for (int i = 0; i < Math.min(auctions.size(), 45); i++) {
            AuctionHouseManager.Auction auction = auctions.get(i);
            
            ItemStack auctionItem = createItem(Material.valueOf(auction.getItemName()),
                plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.item.name",
                    "item", auction.getItemName(), 
                    "current_bid", plugin.getEconomyManager().format(auction.getCurrentBid())),
                plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.item.lore",
                    "starting_bid", plugin.getEconomyManager().format(auction.getStartingBid()),
                    "current_bid", plugin.getEconomyManager().format(auction.getCurrentBid()),
                    "seller", Bukkit.getOfflinePlayer(auction.getSellerUuid()).getName(),
                    "time_left", formatTimeLeft(auction.getEndTime())));
            
            gui.setItem(i, auctionItem);
        }
        
        // Add category filter buttons
        ItemStack allCategory = createItem(Material.COMPASS,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.category.all.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.category.all.lore"));
        
        gui.setItem(45, allCategory);
        
        ItemStack toolsCategory = createItem(Material.IRON_PICKAXE,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.category.tools.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.category.tools.lore"));
        
        gui.setItem(46, toolsCategory);
        
        ItemStack armorCategory = createItem(Material.IRON_CHESTPLATE,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.category.armor.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.category.armor.lore"));
        
        gui.setItem(47, armorCategory);
        
        ItemStack weaponsCategory = createItem(Material.IRON_SWORD,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.category.weapons.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.category.weapons.lore"));
        
        gui.setItem(48, weaponsCategory);
        
        ItemStack otherCategory = createItem(Material.CHEST,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.category.other.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.category.other.lore"));
        
        gui.setItem(49, otherCategory);
        
        // Search button
        ItemStack searchButton = createItem(Material.BOOK,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.search.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.search.lore"));
        
        gui.setItem(50, searchButton);
        
        // Create auction button
        ItemStack createButton = createItem(Material.ANVIL,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.lore"));
        
        gui.setItem(53, createButton);
        
        player.openInventory(gui);
    }
    
    /**
     * Opens the create auction GUI for a player.
     */
    public void openCreateAuctionGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, 
            ChatColor.translateAlternateColorCodes('&', 
                plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.title")));
        
        // Instructions
        ItemStack instructions = createItem(Material.BOOK,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.instructions.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.instructions.lore"));
        
        gui.setItem(4, instructions);
        
        // Item slot
        ItemStack itemSlot = createItem(Material.CHEST,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.item_slot.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.item_slot.lore"));
        
        gui.setItem(20, itemSlot);
        
        // Starting bid input
        ItemStack bidSlot = createItem(Material.GOLD_INGOT,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.bid_slot.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.bid_slot.lore"));
        
        gui.setItem(22, bidSlot);
        
        // Duration selection
        ItemStack durationSlot = createItem(Material.CLOCK,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.duration_slot.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.duration_slot.lore"));
        
        gui.setItem(24, durationSlot);
        
        // Confirm button
        ItemStack confirmButton = createItem(Material.LIME_STAINED_GLASS_PANE,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.confirm.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.confirm.lore"));
        
        gui.setItem(49, confirmButton);
        
        // Cancel button
        ItemStack cancelButton = createItem(Material.RED_STAINED_GLASS_PANE,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.cancel.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.create.cancel.lore"));
        
        gui.setItem(51, cancelButton);
        
        player.openInventory(gui);
    }
    
    /**
     * Opens the player's auctions GUI.
     */
    public void openMyAuctionsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, 
            ChatColor.translateAlternateColorCodes('&', 
                plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.my.title")));
        
        // Get player's auctions
        List<AuctionHouseManager.Auction> auctions = plugin.getAuctionHouseManager().getAuctionsBySeller(player.getUniqueId());
        
        for (int i = 0; i < Math.min(auctions.size(), 45); i++) {
            AuctionHouseManager.Auction auction = auctions.get(i);
            
            ItemStack auctionItem = createItem(Material.valueOf(auction.getItemName()),
                plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.my.item.name",
                    "item", auction.getItemName(), 
                    "status", auction.getStatus()),
                plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.my.item.lore",
                    "starting_bid", plugin.getEconomyManager().format(auction.getStartingBid()),
                    "current_bid", plugin.getEconomyManager().format(auction.getCurrentBid()),
                    "time_left", formatTimeLeft(auction.getEndTime()),
                    "status", auction.getStatus()));
            
            gui.setItem(i, auctionItem);
        }
        
        player.openInventory(gui);
    }
    
    /**
     * Opens the bid history GUI for a player.
     */
    public void openBidHistoryGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, 
            ChatColor.translateAlternateColorCodes('&', 
                plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.bids.title")));
        
        // In a real implementation, this would show the player's bid history
        ItemStack historyItem = createItem(Material.BOOK,
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.bids.history.name"),
            plugin.getLanguageManager().getMessage(getPlayerLanguage(player), "gui.auction.bids.history.lore"));
        
        gui.setItem(4, historyItem);
        
        player.openInventory(gui);
    }
    
    /**
     * Creates an item stack with name and lore.
     */
    private ItemStack createItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            
            // Split lore by newlines if it contains them
            List<String> loreList = new ArrayList<>();
            if (lore != null) {
                String[] lines = lore.split("\n");
                for (String line : lines) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
            
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Formats time left for an auction.
     */
    private String formatTimeLeft(java.util.Date endTime) {
        long timeLeft = endTime.getTime() - System.currentTimeMillis();
        long seconds = timeLeft / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
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