package com.sunsetrq7.smpeconomy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.List;
import java.util.logging.Level;

/**
 * Integrates with Vault for economy compatibility.
 */
public class VaultIntegration implements Economy {
    
    private final SMP_Economy plugin;
    private EconomyResponse notImplementedResponse;
    
    public VaultIntegration(SMP_Economy plugin) {
        this.plugin = plugin;
        this.notImplementedResponse = new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Method not implemented in this context");
    }
    
    /**
     * Sets up the economy provider.
     */
    public void setupEconomy() {
        if (setupEconomyProvider()) {
            plugin.getLogger().info("Vault integration enabled.");
        } else {
            plugin.getLogger().warning("Vault not found. Economy integration disabled.");
        }
    }
    
    /**
     * Sets up the economy provider in Vault.
     */
    private boolean setupEconomyProvider() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            // Register this as the economy provider
            Bukkit.getServer().getServicesManager().register(Economy.class, this, plugin, org.bukkit.plugin.ServicePriority.Highest);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }
    
    @Override
    public String getName() {
        return "SMP-Economy";
    }
    
    @Override
    public boolean hasBankSupport() {
        return true;
    }
    
    @Override
    public int fractionalDigits() {
        return 2; // Two decimal places
    }
    
    @Override
    public String format(double amount) {
        return plugin.getEconomyManager().format(amount);
    }
    
    @Override
    public String currencyNamePlural() {
        return plugin.getConfigManager().getEconomyConfig().getString("currency_name_plural", "dollars");
    }
    
    @Override
    public String currencyNameSingular() {
        return plugin.getConfigManager().getEconomyConfig().getString("currency_name_singular", "dollar");
    }
    
    @Override
    public boolean hasAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return player.hasPlayedBefore() || player.isOnline();
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return player.hasPlayedBefore() || player.isOnline();
    }
    
    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }
    
    @Override
    public double getBalance(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return getBalance(player);
    }
    
    @Override
    public double getBalance(OfflinePlayer player) {
        return plugin.getEconomyManager().getBalance(player.getUniqueId());
    }
    
    @Override
    public double getBalance(String playerName, String worldName) {
        return getBalance(playerName);
    }
    
    @Override
    public double getBalance(OfflinePlayer player, String worldName) {
        return getBalance(player);
    }
    
    @Override
    public boolean has(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return has(player, amount);
    }
    
    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return plugin.getEconomyManager().has(player.getUniqueId(), amount);
    }
    
    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }
    
    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }
    
    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return withdrawPlayer(player, amount);
    }
    
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (plugin.getEconomyManager().removeMoney(player.getUniqueId(), amount)) {
            return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, 
                "Successfully withdrew " + format(amount));
        } else {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, 
                "Failed to withdraw " + format(amount));
        }
    }
    
    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }
    
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }
    
    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return depositPlayer(player, amount);
    }
    
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (plugin.getEconomyManager().addMoney(player.getUniqueId(), amount)) {
            return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, 
                "Successfully deposited " + format(amount));
        } else {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, 
                "Failed to deposit " + format(amount));
        }
    }
    
    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }
    
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }
    
    @Override
    public EconomyResponse createBank(String name, String player) {
        return notImplementedResponse;
    }
    
    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return notImplementedResponse;
    }
    
    @Override
    public EconomyResponse deleteBank(String name) {
        return notImplementedResponse;
    }
    
    @Override
    public EconomyResponse bankBalance(String name) {
        return notImplementedResponse;
    }
    
    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return notImplementedResponse;
    }
    
    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return notImplementedResponse;
    }
    
    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return notImplementedResponse;
    }
    
    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return notImplementedResponse;
    }
    
    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return notImplementedResponse;
    }
    
    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return notImplementedResponse;
    }
    
    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return notImplementedResponse;
    }
    
    @Override
    public List<String> getBanks() {
        return java.util.Collections.emptyList();
    }
    
    @Override
    public boolean createPlayerAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return createPlayerAccount(player);
    }
    
    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return plugin.getEconomyManager().createPlayerAccount(player.getUniqueId(), player.getName());
    }
    
    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }
    
    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }
    
    /**
     * Shuts down the Vault integration.
     */
    public void shutdown() {
        try {
            // Unregister this economy provider
            Bukkit.getServer().getServicesManager().unregister(Economy.class, this);
            plugin.getLogger().info("Vault integration disabled.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error shutting down Vault integration", e);
        }
    }
}