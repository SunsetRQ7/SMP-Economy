package com.sunsetrq7.smpeconomy;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Manages database connections using HikariCP connection pooling.
 * Supports both SQLite and MySQL/MariaDB databases.
 */
public class DatabaseManager {
    
    private final SMP_Economy plugin;
    private HikariDataSource dataSource;
    private boolean isSQLite;
    private String databaseType;
    
    public DatabaseManager(SMP_Economy plugin) {
        this.plugin = plugin;
        this.isSQLite = plugin.getConfigManager().getDatabaseConfig().getString("database.type", "sqlite").equalsIgnoreCase("sqlite");
        this.databaseType = plugin.getConfigManager().getDatabaseConfig().getString("database.type", "sqlite");
    }
    
    /**
     * Initializes the database connection pool.
     */
    public void initialize() {
        try {
            HikariConfig config = new HikariConfig();
            
            if (isSQLite) {
                setupSQLiteConfig(config);
            } else {
                setupMySQLConfig(config);
            }
            
            dataSource = new HikariDataSource(config);
            
            // Test the connection
            try (Connection connection = dataSource.getConnection()) {
                plugin.getLogger().info("Successfully connected to " + (isSQLite ? "SQLite" : "MySQL") + " database!");
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Sets up HikariCP configuration for SQLite.
     */
    private void setupSQLiteConfig(HikariConfig config) {
        String databasePath = plugin.getConfigManager().getDatabaseConfig().getString("database.path", "smpeconomy.db");
        
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder() + "/" + databasePath);
        
        // SQLite specific settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        
        // SQLite-specific connection properties
        config.addDataSourceProperty("cache", "shared");
        config.addDataSourceProperty("journal_mode", "wal");
        config.addDataSourceProperty("synchronous", "normal");
        config.addDataSourceProperty("foreign_keys", "true");
    }
    
    /**
     * Sets up HikariCP configuration for MySQL/MariaDB.
     */
    private void setupMySQLConfig(HikariConfig config) {
        String host = plugin.getConfigManager().getDatabaseConfig().getString("database.host", "localhost");
        int port = plugin.getConfigManager().getDatabaseConfig().getInt("database.port", 3306);
        String database = plugin.getConfigManager().getDatabaseConfig().getString("database.database", "smpeconomy");
        String username = plugin.getConfigManager().getDatabaseConfig().getString("database.username", "root");
        String password = plugin.getConfigManager().getDatabaseConfig().getString("database.password", "");
        boolean useSSL = plugin.getConfigManager().getDatabaseConfig().getBoolean("database.ssl", false);
        
        config.setDriverClassName(getDriverClassName());
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL + "&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8mb4");
        config.setUsername(username);
        config.setPassword(password);
        
        // MySQL specific settings
        config.setMaximumPoolSize(plugin.getConfigManager().getDatabaseConfig().getInt("database.pool.max_size", 20));
        config.setMinimumIdle(plugin.getConfigManager().getDatabaseConfig().getInt("database.pool.min_idle", 5));
        config.setConnectionTimeout(plugin.getConfigManager().getDatabaseConfig().getLong("database.pool.connection_timeout", 30000L));
        config.setIdleTimeout(plugin.getConfigManager().getDatabaseConfig().getLong("database.pool.idle_timeout", 600000L));
        config.setMaxLifetime(plugin.getConfigManager().getDatabaseConfig().getLong("database.pool.max_lifetime", 1800000L));
        config.setLeakDetectionThreshold(plugin.getConfigManager().getDatabaseConfig().getLong("database.pool.leak_detection_threshold", 60000L));
        
        // MySQL-specific connection properties
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        config.addDataSourceProperty("characterEncoding", "utf8mb4");
        config.addDataSourceProperty("charset", "utf8mb4");
    }
    
    /**
     * Gets the appropriate driver class name based on the database type.
     */
    private String getDriverClassName() {
        if (databaseType.equalsIgnoreCase("mariadb")) {
            return "org.mariadb.jdbc.Driver";
        } else {
            return "com.mysql.cj.jdbc.Driver";
        }
    }
    
    /**
     * Gets a connection from the pool.
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database connection pool not initialized");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Closes all database connections in the pool.
     */
    public void closeConnections() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connections closed.");
        }
    }
    
    /**
     * Checks if the database connection is valid.
     */
    public boolean isConnectionValid() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Gets the database type (sqlite or mysql).
     */
    public String getDatabaseType() {
        return databaseType;
    }
    
    /**
     * Checks if the database is SQLite.
     */
    public boolean isSQLite() {
        return isSQLite;
    }
    
    /**
     * Checks if the database is MySQL/MariaDB.
     */
    public boolean isMySQL() {
        return !isSQLite;
    }
}