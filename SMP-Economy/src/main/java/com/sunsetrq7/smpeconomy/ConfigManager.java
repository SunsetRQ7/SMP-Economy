package com.sunsetrq7.smpeconomy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Manages configuration files for the plugin.
 */
public class ConfigManager {
    
    private final SMP_Economy plugin;
    
    // Configuration files
    private FileConfiguration mainConfig;
    private FileConfiguration databaseConfig;
    private FileConfiguration economyConfig;
    private FileConfiguration auctionConfig;
    private FileConfiguration bankConfig;
    private FileConfiguration securityConfig;
    private FileConfiguration performanceConfig;
    
    // Configuration files
    private File mainConfigFile;
    private File databaseConfigFile;
    private File economyConfigFile;
    private File auctionConfigFile;
    private File bankConfigFile;
    private File securityConfigFile;
    private File performanceConfigFile;
    
    public ConfigManager(SMP_Economy plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Loads all configuration files.
     */
    public void loadConfigs() {
        plugin.getLogger().info("Loading configuration files...");
        
        // Create config directory if it doesn't exist
        File configDir = new File(plugin.getDataFolder(), "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // Load main config
        loadMainConfig();
        
        // Load database config
        loadDatabaseConfig();
        
        // Load economy config
        loadEconomyConfig();
        
        // Load auction config
        loadAuctionConfig();
        
        // Load bank config
        loadBankConfig();
        
        // Load security config
        loadSecurityConfig();
        
        // Load performance config
        loadPerformanceConfig();
        
        plugin.getLogger().info("All configuration files loaded successfully.");
    }
    
    /**
     * Loads the main configuration file.
     */
    private void loadMainConfig() {
        mainConfigFile = new File(plugin.getDataFolder(), "config.yml");
        
        if (!mainConfigFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        mainConfig = YamlConfiguration.loadConfiguration(mainConfigFile);
        
        // Set default values
        setDefaultMainConfigValues();
        
        // Save the config with defaults if it's a new file
        saveMainConfig();
    }
    
    /**
     * Sets default values for the main config.
     */
    private void setDefaultMainConfigValues() {
        if (!mainConfig.contains("default_language")) {
            mainConfig.set("default_language", "en_US");
        }
        
        if (!mainConfig.contains("debug")) {
            mainConfig.set("debug", false);
        }
        
        if (!mainConfig.contains("auto_update")) {
            mainConfig.set("auto_update", true);
        }
        
        if (!mainConfig.contains("backup_enabled")) {
            mainConfig.set("backup_enabled", true);
        }
        
        if (!mainConfig.contains("backup_interval_hours")) {
            mainConfig.set("backup_interval_hours", 24);
        }
        
        if (!mainConfig.contains("starting_balance")) {
            mainConfig.set("starting_balance", 100.0);
        }
        
        if (!mainConfig.contains("max_balance")) {
            mainConfig.set("max_balance", 1000000000.0);
        }
        
        if (!mainConfig.contains("min_transaction")) {
            mainConfig.set("min_transaction", 0.01);
        }
    }
    
    /**
     * Loads the database configuration file.
     */
    private void loadDatabaseConfig() {
        databaseConfigFile = new File(plugin.getDataFolder(), "database.yml");
        
        if (!databaseConfigFile.exists()) {
            plugin.saveResource("database.yml", false);
        }
        
        databaseConfig = YamlConfiguration.loadConfiguration(databaseConfigFile);
        
        // Set default values
        setDefaultDatabaseConfigValues();
        
        // Save the config with defaults if it's a new file
        saveDatabaseConfig();
    }
    
    /**
     * Sets default values for the database config.
     */
    private void setDefaultDatabaseConfigValues() {
        if (!databaseConfig.contains("database.type")) {
            databaseConfig.set("database.type", "sqlite");
        }
        
        if (!databaseConfig.contains("database.path")) {
            databaseConfig.set("database.path", "smpeconomy.db");
        }
        
        if (!databaseConfig.contains("database.host")) {
            databaseConfig.set("database.host", "localhost");
        }
        
        if (!databaseConfig.contains("database.port")) {
            databaseConfig.set("database.port", 3306);
        }
        
        if (!databaseConfig.contains("database.database")) {
            databaseConfig.set("database.database", "smpeconomy");
        }
        
        if (!databaseConfig.contains("database.username")) {
            databaseConfig.set("database.username", "root");
        }
        
        if (!databaseConfig.contains("database.password")) {
            databaseConfig.set("database.password", "");
        }
        
        if (!databaseConfig.contains("database.ssl")) {
            databaseConfig.set("database.ssl", false);
        }
        
        if (!databaseConfig.contains("database.pool.max_size")) {
            databaseConfig.set("database.pool.max_size", 20);
        }
        
        if (!databaseConfig.contains("database.pool.min_idle")) {
            databaseConfig.set("database.pool.min_idle", 5);
        }
        
        if (!databaseConfig.contains("database.pool.connection_timeout")) {
            databaseConfig.set("database.pool.connection_timeout", 30000L);
        }
    }
    
    /**
     * Loads the economy configuration file.
     */
    private void loadEconomyConfig() {
        economyConfigFile = new File(plugin.getDataFolder(), "economy.yml");
        
        if (!economyConfigFile.exists()) {
            plugin.saveResource("economy.yml", false);
        }
        
        economyConfig = YamlConfiguration.loadConfiguration(economyConfigFile);
        
        // Set default values
        setDefaultEconomyConfigValues();
        
        // Save the config with defaults if it's a new file
        saveEconomyConfig();
    }
    
    /**
     * Sets default values for the economy config.
     */
    private void setDefaultEconomyConfigValues() {
        if (!economyConfig.contains("starting_balance")) {
            economyConfig.set("starting_balance", 100.0);
        }
        
        if (!economyConfig.contains("max_balance")) {
            economyConfig.set("max_balance", 1000000000.0);
        }
        
        if (!economyConfig.contains("min_transaction")) {
            economyConfig.set("min_transaction", 0.01);
        }
        
        if (!economyConfig.contains("transaction_fee")) {
            economyConfig.set("transaction_fee", 0.0);
        }
        
        if (!economyConfig.contains("enabled")) {
            economyConfig.set("enabled", true);
        }
        
        if (!economyConfig.contains("currency_name_singular")) {
            economyConfig.set("currency_name_singular", "dollar");
        }
        
        if (!economyConfig.contains("currency_name_plural")) {
            economyConfig.set("currency_name_plural", "dollars");
        }
        
        if (!economyConfig.contains("currency_symbol")) {
            economyConfig.set("currency_symbol", "$");
        }
    }
    
    /**
     * Loads the auction configuration file.
     */
    private void loadAuctionConfig() {
        auctionConfigFile = new File(plugin.getDataFolder(), "auction.yml");
        
        if (!auctionConfigFile.exists()) {
            plugin.saveResource("auction.yml", false);
        }
        
        auctionConfig = YamlConfiguration.loadConfiguration(auctionConfigFile);
        
        // Set default values
        setDefaultAuctionConfigValues();
        
        // Save the config with defaults if it's a new file
        saveAuctionConfig();
    }
    
    /**
     * Sets default values for the auction config.
     */
    private void setDefaultAuctionConfigValues() {
        if (!auctionConfig.contains("enabled")) {
            auctionConfig.set("enabled", true);
        }
        
        if (!auctionConfig.contains("fee_percentage")) {
            auctionConfig.set("fee_percentage", 5.0);
        }
        
        if (!auctionConfig.contains("minimum_bid_increase")) {
            auctionConfig.set("minimum_bid_increase", 1.0);
        }
        
        if (!auctionConfig.contains("auction_durations")) {
            auctionConfig.set("auction_durations", java.util.Arrays.asList(3600, 21600, 86400, 604800)); // 1h, 6h, 24h, 7d in seconds
        }
        
        if (!auctionConfig.contains("max_auctions_per_player")) {
            auctionConfig.set("max_auctions_per_player", 10);
        }
        
        if (!auctionConfig.contains("bid_cooldown_seconds")) {
            auctionConfig.set("bid_cooldown_seconds", 5);
        }
    }
    
    /**
     * Loads the bank configuration file.
     */
    private void loadBankConfig() {
        bankConfigFile = new File(plugin.getDataFolder(), "bank.yml");
        
        if (!bankConfigFile.exists()) {
            plugin.saveResource("bank.yml", false);
        }
        
        bankConfig = YamlConfiguration.loadConfiguration(bankConfigFile);
        
        // Set default values
        setDefaultBankConfigValues();
        
        // Save the config with defaults if it's a new file
        saveBankConfig();
    }
    
    /**
     * Sets default values for the bank config.
     */
    private void setDefaultBankConfigValues() {
        if (!bankConfig.contains("enabled")) {
            bankConfig.set("enabled", true);
        }
        
        if (!bankConfig.contains("daily_deposit_limit")) {
            bankConfig.set("daily_deposit_limit", 1000000.0);
        }
        
        if (!bankConfig.contains("weekly_deposit_limit")) {
            bankConfig.set("weekly_deposit_limit", 10000000.0);
        }
        
        if (!bankConfig.contains("daily_withdrawal_limit")) {
            bankConfig.set("daily_withdrawal_limit", 1000000.0);
        }
        
        if (!bankConfig.contains("interest_rate")) {
            bankConfig.set("interest_rate", 0.1);
        }
        
        if (!bankConfig.contains("min_balance_for_interest")) {
            bankConfig.set("min_balance_for_interest", 1000.0);
        }
        
        if (!bankConfig.contains("interest_calculation_interval_hours")) {
            bankConfig.set("interest_calculation_interval_hours", 24);
        }
    }
    
    /**
     * Loads the security configuration file.
     */
    private void loadSecurityConfig() {
        securityConfigFile = new File(plugin.getDataFolder(), "security.yml");
        
        if (!securityConfigFile.exists()) {
            plugin.saveResource("security.yml", false);
        }
        
        securityConfig = YamlConfiguration.loadConfiguration(securityConfigFile);
        
        // Set default values
        setDefaultSecurityConfigValues();
        
        // Save the config with defaults if it's a new file
        saveSecurityConfig();
    }
    
    /**
     * Sets default values for the security config.
     */
    private void setDefaultSecurityConfigValues() {
        if (!securityConfig.contains("transaction_cooldown_seconds")) {
            securityConfig.set("transaction_cooldown_seconds", 1);
        }
        
        if (!securityConfig.contains("max_transactions_per_minute")) {
            securityConfig.set("max_transactions_per_minute", 10);
        }
        
        if (!securityConfig.contains("enable_transaction_logging")) {
            securityConfig.set("enable_transaction_logging", true);
        }
        
        if (!securityConfig.contains("log_admin_commands")) {
            securityConfig.set("log_admin_commands", true);
        }
        
        if (!securityConfig.contains("enable_anti_exploit")) {
            securityConfig.set("enable_anti_exploit", true);
        }
    }
    
    /**
     * Loads the performance configuration file.
     */
    private void loadPerformanceConfig() {
        performanceConfigFile = new File(plugin.getDataFolder(), "performance.yml");
        
        if (!performanceConfigFile.exists()) {
            plugin.saveResource("performance.yml", false);
        }
        
        performanceConfig = YamlConfiguration.loadConfiguration(performanceConfigFile);
        
        // Set default values
        setDefaultPerformanceConfigValues();
        
        // Save the config with defaults if it's a new file
        savePerformanceConfig();
    }
    
    /**
     * Sets default values for the performance config.
     */
    private void setDefaultPerformanceConfigValues() {
        if (!performanceConfig.contains("cache_expiration_minutes")) {
            performanceConfig.set("cache_expiration_minutes", 5);
        }
        
        if (!performanceConfig.contains("max_cached_players")) {
            performanceConfig.set("max_cached_players", 1000);
        }
        
        if (!performanceConfig.contains("async_database_operations")) {
            performanceConfig.set("async_database_operations", true);
        }
        
        if (!performanceConfig.contains("enable_metrics")) {
            performanceConfig.set("enable_metrics", true);
        }
    }
    
    /**
     * Saves the main configuration file.
     */
    public void saveMainConfig() {
        try {
            mainConfig.save(mainConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save main config to " + mainConfigFile, e);
        }
    }
    
    /**
     * Saves the database configuration file.
     */
    public void saveDatabaseConfig() {
        try {
            databaseConfig.save(databaseConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save database config to " + databaseConfigFile, e);
        }
    }
    
    /**
     * Saves the economy configuration file.
     */
    public void saveEconomyConfig() {
        try {
            economyConfig.save(economyConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save economy config to " + economyConfigFile, e);
        }
    }
    
    /**
     * Saves the auction configuration file.
     */
    public void saveAuctionConfig() {
        try {
            auctionConfig.save(auctionConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save auction config to " + auctionConfigFile, e);
        }
    }
    
    /**
     * Saves the bank configuration file.
     */
    public void saveBankConfig() {
        try {
            bankConfig.save(bankConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save bank config to " + bankConfigFile, e);
        }
    }
    
    /**
     * Saves the security configuration file.
     */
    public void saveSecurityConfig() {
        try {
            securityConfig.save(securityConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save security config to " + securityConfigFile, e);
        }
    }
    
    /**
     * Saves the performance configuration file.
     */
    public void savePerformanceConfig() {
        try {
            performanceConfig.save(performanceConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save performance config to " + performanceConfigFile, e);
        }
    }
    
    /**
     * Reloads all configuration files.
     */
    public void reloadConfigs() {
        loadConfigs();
    }
    
    /**
     * Gets the main configuration.
     */
    public FileConfiguration getMainConfig() {
        return mainConfig;
    }
    
    /**
     * Gets the database configuration.
     */
    public FileConfiguration getDatabaseConfig() {
        return databaseConfig;
    }
    
    /**
     * Gets the economy configuration.
     */
    public FileConfiguration getEconomyConfig() {
        return economyConfig;
    }
    
    /**
     * Gets the auction configuration.
     */
    public FileConfiguration getAuctionConfig() {
        return auctionConfig;
    }
    
    /**
     * Gets the bank configuration.
     */
    public FileConfiguration getBankConfig() {
        return bankConfig;
    }
    
    /**
     * Gets the security configuration.
     */
    public FileConfiguration getSecurityConfig() {
        return securityConfig;
    }
    
    /**
     * Gets the performance configuration.
     */
    public FileConfiguration getPerformanceConfig() {
        return performanceConfig;
    }
}