package com.sunsetrq7.smpeconomy;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import java.util.logging.Logger;

/**
 * Main class for SMP-Economy plugin.
 * This is the entry point for the plugin when loaded by the server.
 */
public class SMP_Economy extends JavaPlugin {
    
    private static SMP_Economy instance;
    
    // Manager instances
    private DatabaseManager databaseManager;
    private SchemaManager schemaManager;
    private EconomyManager economyManager;
    private PlayerDataManager playerDataManager;
    private AuctionHouseManager auctionHouseManager;
    private BankManager bankManager;
    private TransferManager transferManager;
    private CommandManager commandManager;
    private GUIManager guiManager;
    private ListenerManager listenerManager;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private PermissionManager permissionManager;
    private SecurityManager securityManager;
    private MetricsManager metricsManager;
    private UpdateManager updateManager;
    private VersionManager versionManager;
    private TaskManager taskManager;
    private BackupManager backupManager;
    private LoggerManager loggerManager;
    private ErrorHandler errorHandler;
    private MigrationManager migrationManager;
    private CacheManager cacheManager;
    private NotificationManager notificationManager;
    private SoundManager soundManager;
    private ParticleManager particleManager;
    private PlaceholderManager placeholderManager;
    private VaultIntegration vaultIntegration;
    private UpdateChecker updateChecker;
    private MetricsCollector metricsCollector;
    private DebugManager debugManager;
    private PluginMetrics pluginMetrics;
    
    private Logger logger;
    
    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        
        try {
            logger.info("Starting SMP-Economy v" + getDescription().getVersion());
            
            // Initialize configuration manager first
            configManager = new ConfigManager(this);
            configManager.loadConfigs();
            
            // Initialize language manager
            languageManager = new LanguageManager(this);
            languageManager.loadLanguages();
            
            // Initialize error handler
            errorHandler = new ErrorHandler(this);
            
            // Initialize logger manager
            loggerManager = new LoggerManager(this);
            
            // Initialize database manager
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
            
            // Initialize schema manager and create/update schema
            schemaManager = new SchemaManager(this);
            schemaManager.initializeSchema();
            
            // Initialize cache manager
            cacheManager = new CacheManager(this);
            
            // Initialize player data manager
            playerDataManager = new PlayerDataManager(this);
            playerDataManager.initialize();
            
            // Initialize economy manager
            economyManager = new EconomyManager(this);
            
            // Initialize bank manager
            bankManager = new BankManager(this);
            
            // Initialize transfer manager
            transferManager = new TransferManager(this);
            
            // Initialize auction house manager
            auctionHouseManager = new AuctionHouseManager(this);
            
            // Initialize permission manager
            permissionManager = new PermissionManager(this);
            permissionManager.setupDefaultPermissions();
            
            // Initialize security manager
            securityManager = new SecurityManager(this);
            
            // Initialize update manager
            updateManager = new UpdateManager(this);
            
            // Initialize update checker
            updateChecker = new UpdateChecker(this);
            
            // Initialize metrics collector
            metricsCollector = new MetricsCollector(this);
            
            // Initialize plugin metrics
            pluginMetrics = new PluginMetrics(this);
            
            // Initialize metrics manager (bStats)
            metricsManager = new MetricsManager(this);
            metricsManager.setupMetrics();
            
            // Initialize version manager
            versionManager = new VersionManager(this);
            
            // Initialize task manager
            taskManager = new TaskManager(this);
            
            // Initialize backup manager
            backupManager = new BackupManager(this);
            
            // Initialize GUI manager
            guiManager = new GUIManager(this);
            
            // Initialize sound manager
            soundManager = new SoundManager(this);
            
            // Initialize particle manager
            particleManager = new ParticleManager(this);
            
            // Initialize notification manager
            notificationManager = new NotificationManager(this);
            
            // Initialize migration manager
            migrationManager = new MigrationManager(this);
            
            // Initialize debug manager
            debugManager = new DebugManager(this);
            
            // Initialize Vault integration if available
            vaultIntegration = new VaultIntegration(this);
            vaultIntegration.setupEconomy();
            
            // Initialize PlaceholderAPI integration if available
            placeholderManager = new PlaceholderManager(this);
            placeholderManager.registerPlaceholders();
            
            // Initialize command manager
            commandManager = new CommandManager(this);
            commandManager.registerCommands();
            
            // Initialize listener manager
            listenerManager = new ListenerManager(this);
            listenerManager.registerListeners();
            
            // Schedule daily interest calculation
            taskManager.scheduleDailyInterest();
            
            // Schedule regular backup tasks
            taskManager.scheduleBackups();
            
            // Schedule cleanup tasks
            taskManager.scheduleCleanup();
            
            logger.info("SMP-Economy has been enabled successfully!");
            
            // Check for updates
            updateChecker.checkForUpdates();
            
        } catch (Exception e) {
            logger.severe("Failed to enable SMP-Economy: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        try {
            logger.info("Disabling SMP-Economy v" + getDescription().getVersion());
            
            // Cancel all scheduled tasks
            if (taskManager != null) {
                taskManager.shutdown();
            }
            
            // Save all player data
            if (playerDataManager != null) {
                playerDataManager.saveAllPlayers();
            }
            
            // Close database connections
            if (databaseManager != null) {
                databaseManager.closeConnections();
            }
            
            // Unregister Vault integration
            if (vaultIntegration != null) {
                vaultIntegration.shutdown();
            }
            
            // Unregister PlaceholderAPI integration
            if (placeholderManager != null) {
                placeholderManager.unregisterPlaceholders();
            }
            
            logger.info("SMP-Economy has been disabled successfully!");
            
        } catch (Exception e) {
            logger.severe("Error during SMP-Economy disable: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getter methods for accessing managers
    public static SMP_Economy getInstance() {
        return instance;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public SchemaManager getSchemaManager() {
        return schemaManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    public AuctionHouseManager getAuctionHouseManager() {
        return auctionHouseManager;
    }
    
    public BankManager getBankManager() {
        return bankManager;
    }
    
    public TransferManager getTransferManager() {
        return transferManager;
    }
    
    public CommandManager getCommandManager() {
        return commandManager;
    }
    
    public GUIManager getGUIManager() {
        return guiManager;
    }
    
    public ListenerManager getListenerManager() {
        return listenerManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
    
    public SecurityManager getSecurityManager() {
        return securityManager;
    }
    
    public MetricsManager getMetricsManager() {
        return metricsManager;
    }
    
    public UpdateManager getUpdateManager() {
        return updateManager;
    }
    
    public VersionManager getVersionManager() {
        return versionManager;
    }
    
    public TaskManager getTaskManager() {
        return taskManager;
    }
    
    public BackupManager getBackupManager() {
        return backupManager;
    }
    
    public LoggerManager getLoggerManager() {
        return loggerManager;
    }
    
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
    
    public MigrationManager getMigrationManager() {
        return migrationManager;
    }
    
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }
    
    public SoundManager getSoundManager() {
        return soundManager;
    }
    
    public ParticleManager getParticleManager() {
        return particleManager;
    }
    
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
    
    public VaultIntegration getVaultIntegration() {
        return vaultIntegration;
    }
    
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
    
    public MetricsCollector getMetricsCollector() {
        return metricsCollector;
    }
    
    public DebugManager getDebugManager() {
        return debugManager;
    }
    
    public PluginMetrics getPluginMetrics() {
        return pluginMetrics;
    }
}