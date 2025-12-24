package com.sunsetrq7.smpeconomy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages all commands for the plugin.
 */
public class CommandManager implements CommandExecutor, TabCompleter {
    
    private final SMP_Economy plugin;
    
    public CommandManager(SMP_Economy plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Registers all commands.
     */
    public void registerCommands() {
        // Commands are registered in plugin.yml
        plugin.getCommand("eco").setExecutor(this);
        plugin.getCommand("ecoadmin").setExecutor(this);
        plugin.getCommand("bank").setExecutor(this);
        plugin.getCommand("ah").setExecutor(this);
        
        plugin.getLogger().info("Commands registered successfully.");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();
        
        switch (cmd) {
            case "eco":
                return handleEcoCommand(sender, args);
            case "ecoadmin":
                return handleEcoAdminCommand(sender, args);
            case "bank":
                return handleBankCommand(sender, args);
            case "ah":
                return handleAuctionCommand(sender, args);
            default:
                return false;
        }
    }
    
    /**
     * Handles the main economy command.
     */
    private boolean handleEcoCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open main economy GUI
            plugin.getGUIManager().openMainEconomyGUI(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "balance":
            case "bal":
                // Show player's balance
                double balance = plugin.getEconomyManager().getBalance(player);
                String message = plugin.getLanguageManager().getMessage(
                    getPlayerLanguage(player), 
                    "economy.balance", 
                    "balance", plugin.getEconomyManager().format(balance)
                );
                sender.sendMessage(message);
                break;
                
            case "top":
                // Show top balances
                List<EconomyManager.PlayerBalance> topBalances = plugin.getEconomyManager().getTopBalances(10);
                sender.sendMessage(plugin.getLanguageManager().getMessage(
                    getPlayerLanguage(player), 
                    "economy.top_header"
                ));
                
                for (int i = 0; i < topBalances.size(); i++) {
                    EconomyManager.PlayerBalance pb = topBalances.get(i);
                    sender.sendMessage((i + 1) + ". " + pb.getUsername() + " - " + 
                        plugin.getEconomyManager().format(pb.getBalance()));
                }
                break;
                
            case "pay":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getPlayerLanguage(player), 
                        "economy.pay_usage"
                    ));
                    return true;
                }
                
                Player receiver = plugin.getServer().getPlayer(args[1]);
                if (receiver == null) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getPlayerLanguage(player), 
                        "player_not_found"
                    ));
                    return true;
                }
                
                try {
                    double amount = Double.parseDouble(args[2]);
                    if (plugin.getTransferManager().transferMoney(player, receiver, amount)) {
                        sender.sendMessage(plugin.getLanguageManager().getMessage(
                            getPlayerLanguage(player), 
                            "economy.pay_success",
                            "amount", plugin.getEconomyManager().format(amount),
                            "receiver", receiver.getName()
                        ));
                    } else {
                        sender.sendMessage(plugin.getLanguageManager().getMessage(
                            getPlayerLanguage(player), 
                            "economy.pay_failed"
                        ));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getPlayerLanguage(player), 
                        "invalid_amount"
                    ));
                }
                break;
                
            default:
                sender.sendMessage(plugin.getLanguageManager().getMessage(
                    getPlayerLanguage(player), 
                    "economy.usage"
                ));
                break;
        }
        
        return true;
    }
    
    /**
     * Handles the admin economy command.
     */
    private boolean handleEcoAdminCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("smpeconomy.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(
                getDefaultLanguage(), 
                "no_permission"
            ));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(
                getDefaultLanguage(), 
                "admin.usage"
            ));
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "give":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getDefaultLanguage(), 
                        "admin.give_usage"
                    ));
                    return true;
                }
                
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getDefaultLanguage(), 
                        "player_not_found"
                    ));
                    return true;
                }
                
                try {
                    double amount = Double.parseDouble(args[2]);
                    plugin.getEconomyManager().addMoney(target, amount);
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getDefaultLanguage(), 
                        "admin.give_success",
                        "amount", plugin.getEconomyManager().format(amount),
                        "player", target.getName()
                    ));
                    
                    // Notify the player
                    target.sendMessage(plugin.getLanguageManager().getMessage(
                        getPlayerLanguage(target), 
                        "admin.money_given",
                        "amount", plugin.getEconomyManager().format(amount),
                        "sender", sender.getName()
                    ));
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getDefaultLanguage(), 
                        "invalid_amount"
                    ));
                }
                break;
                
            case "set":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getDefaultLanguage(), 
                        "admin.set_usage"
                    ));
                    return true;
                }
                
                Player targetSet = plugin.getServer().getPlayer(args[1]);
                if (targetSet == null) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getDefaultLanguage(), 
                        "player_not_found"
                    ));
                    return true;
                }
                
                try {
                    double amount = Double.parseDouble(args[2]);
                    plugin.getEconomyManager().setBalance(targetSet, amount);
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getDefaultLanguage(), 
                        "admin.set_success",
                        "amount", plugin.getEconomyManager().format(amount),
                        "player", targetSet.getName()
                    ));
                    
                    // Notify the player
                    targetSet.sendMessage(plugin.getLanguageManager().getMessage(
                        getPlayerLanguage(targetSet), 
                        "admin.balance_set",
                        "amount", plugin.getEconomyManager().format(amount),
                        "sender", sender.getName()
                    ));
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getDefaultLanguage(), 
                        "invalid_amount"
                    ));
                }
                break;
                
            case "reset":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getDefaultLanguage(), 
                        "admin.reset_usage"
                    ));
                    return true;
                }
                
                Player targetReset = plugin.getServer().getPlayer(args[1]);
                if (targetReset == null) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getDefaultLanguage(), 
                        "player_not_found"
                    ));
                    return true;
                }
                
                plugin.getEconomyManager().setBalance(targetReset, 
                    plugin.getConfigManager().getEconomyConfig().getDouble("starting_balance", 100.0));
                sender.sendMessage(plugin.getLanguageManager().getMessage(
                    getDefaultLanguage(), 
                    "admin.reset_success",
                    "player", targetReset.getName()
                ));
                
                // Notify the player
                targetReset.sendMessage(plugin.getLanguageManager().getMessage(
                    getPlayerLanguage(targetReset), 
                    "admin.balance_reset",
                    "sender", sender.getName()
                ));
                break;
                
            case "reload":
                plugin.getConfigManager().reloadConfigs();
                plugin.getLanguageManager().reloadLanguages();
                sender.sendMessage(plugin.getLanguageManager().getMessage(
                    getDefaultLanguage(), 
                    "admin.reload_success"
                ));
                break;
                
            default:
                sender.sendMessage(plugin.getLanguageManager().getMessage(
                    getDefaultLanguage(), 
                    "admin.usage"
                ));
                break;
        }
        
        return true;
    }
    
    /**
     * Handles the bank command.
     */
    private boolean handleBankCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open bank GUI
            plugin.getGUIManager().openBankGUI(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "balance":
                double bankBalance = plugin.getBankManager().getBankBalance(player);
                sender.sendMessage(plugin.getLanguageManager().getMessage(
                    getPlayerLanguage(player), 
                    "bank.balance",
                    "balance", plugin.getBankManager().format(bankBalance)
                ));
                break;
                
            case "deposit":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getPlayerLanguage(player), 
                        "bank.deposit_usage"
                    ));
                    return true;
                }
                
                if (args[1].equalsIgnoreCase("all")) {
                    // Deposit all money
                    double playerBalance = plugin.getEconomyManager().getBalance(player);
                    if (plugin.getBankManager().depositToBank(player, playerBalance)) {
                        sender.sendMessage(plugin.getLanguageManager().getMessage(
                            getPlayerLanguage(player), 
                            "bank.deposit_success",
                            "amount", plugin.getBankManager().format(playerBalance)
                        ));
                    } else {
                        sender.sendMessage(plugin.getLanguageManager().getMessage(
                            getPlayerLanguage(player), 
                            "bank.deposit_failed"
                        ));
                    }
                } else {
                    try {
                        double amount = Double.parseDouble(args[1]);
                        if (plugin.getBankManager().depositToBank(player, amount)) {
                            sender.sendMessage(plugin.getLanguageManager().getMessage(
                                getPlayerLanguage(player), 
                                "bank.deposit_success",
                                "amount", plugin.getBankManager().format(amount)
                            ));
                        } else {
                            sender.sendMessage(plugin.getLanguageManager().getMessage(
                                getPlayerLanguage(player), 
                                "bank.deposit_failed"
                            ));
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage(plugin.getLanguageManager().getMessage(
                            getPlayerLanguage(player), 
                            "invalid_amount"
                        ));
                    }
                }
                break;
                
            case "withdraw":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(
                        getPlayerLanguage(player), 
                        "bank.withdraw_usage"
                    ));
                    return true;
                }
                
                if (args[1].equalsIgnoreCase("all")) {
                    // Withdraw all money
                    double bankBalanceAmount = plugin.getBankManager().getBankBalance(player);
                    if (plugin.getBankManager().withdrawFromBank(player, bankBalanceAmount)) {
                        sender.sendMessage(plugin.getLanguageManager().getMessage(
                            getPlayerLanguage(player), 
                            "bank.withdraw_success",
                            "amount", plugin.getBankManager().format(bankBalanceAmount)
                        ));
                    } else {
                        sender.sendMessage(plugin.getLanguageManager().getMessage(
                            getPlayerLanguage(player), 
                            "bank.withdraw_failed"
                        ));
                    }
                } else {
                    try {
                        double amount = Double.parseDouble(args[1]);
                        if (plugin.getBankManager().withdrawFromBank(player, amount)) {
                            sender.sendMessage(plugin.getLanguageManager().getMessage(
                                getPlayerLanguage(player), 
                                "bank.withdraw_success",
                                "amount", plugin.getBankManager().format(amount)
                            ));
                        } else {
                            sender.sendMessage(plugin.getLanguageManager().getMessage(
                                getPlayerLanguage(player), 
                                "bank.withdraw_failed"
                            ));
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage(plugin.getLanguageManager().getMessage(
                            getPlayerLanguage(player), 
                            "invalid_amount"
                        ));
                    }
                }
                break;
                
            default:
                sender.sendMessage(plugin.getLanguageManager().getMessage(
                    getPlayerLanguage(player), 
                    "bank.usage"
                ));
                break;
        }
        
        return true;
    }
    
    /**
     * Handles the auction command.
     */
    private boolean handleAuctionCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open auction house GUI
            plugin.getGUIManager().openAuctionHouseGUI(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "my":
                // Open player's auctions GUI
                plugin.getGUIManager().openMyAuctionsGUI(player);
                break;
                
            case "create":
                // Open create auction GUI
                plugin.getGUIManager().openCreateAuctionGUI(player);
                break;
                
            case "bids":
                // Open bid history GUI
                plugin.getGUIManager().openBidHistoryGUI(player);
                break;
                
            default:
                sender.sendMessage(plugin.getLanguageManager().getMessage(
                    getPlayerLanguage(player), 
                    "auction.usage"
                ));
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase();
        
        switch (cmd) {
            case "eco":
                if (args.length == 1) {
                    return Arrays.asList("balance", "bal", "top", "pay")
                        .stream()
                        .filter(arg -> arg.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
                } else if (args.length == 2 && args[0].equalsIgnoreCase("pay")) {
                    return plugin.getServer().getOnlinePlayers().stream()
                        .map(player -> player.getName())
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
                break;
                
            case "ecoadmin":
                if (args.length == 1 && sender.hasPermission("smpeconomy.admin")) {
                    return Arrays.asList("give", "set", "reset", "reload")
                        .stream()
                        .filter(arg -> arg.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
                } else if (args.length == 2 && (args[0].equalsIgnoreCase("give") || 
                           args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset")) && 
                           sender.hasPermission("smpeconomy.admin")) {
                    return plugin.getServer().getOnlinePlayers().stream()
                        .map(player -> player.getName())
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
                break;
                
            case "bank":
                if (args.length == 1) {
                    return Arrays.asList("balance", "deposit", "withdraw")
                        .stream()
                        .filter(arg -> arg.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
                }
                break;
                
            case "ah":
                if (args.length == 1) {
                    return Arrays.asList("my", "create", "bids")
                        .stream()
                        .filter(arg -> arg.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
                }
                break;
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Gets the player's language.
     */
    private String getPlayerLanguage(Player player) {
        // In a real implementation, this would get the player's language from the database
        return plugin.getLanguageManager().getDefaultLanguage();
    }
    
    /**
     * Gets the default language.
     */
    private String getDefaultLanguage() {
        return plugin.getLanguageManager().getDefaultLanguage();
    }
}