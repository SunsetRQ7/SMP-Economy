package com.sunsetrq7.smpeconomy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

/**
 * Manages database schema creation and migration.
 * Automatically creates all necessary tables on first run.
 */
public class SchemaManager {
    
    private final SMP_Economy plugin;
    private final DatabaseManager databaseManager;
    
    // Current schema version
    private static final String CURRENT_SCHEMA_VERSION = "1.0.0";
    
    public SchemaManager(SMP_Economy plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }
    
    /**
     * Initializes the database schema by creating tables if they don't exist.
     */
    public void initializeSchema() {
        try {
            plugin.getLogger().info("Initializing database schema...");
            
            // Check if schema exists and create if needed
            if (!schemaExists()) {
                plugin.getLogger().info("No existing schema found. Creating new schema...");
                createSchema();
                plugin.getLogger().info("Database schema created successfully!");
            } else {
                plugin.getLogger().info("Existing schema found. Checking for updates...");
                updateSchema();
                plugin.getLogger().info("Database schema is up to date!");
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database schema", e);
            throw new RuntimeException("Schema initialization failed", e);
        }
    }
    
    /**
     * Checks if the schema already exists by looking for the players table.
     */
    private boolean schemaExists() throws SQLException {
        try (Connection connection = databaseManager.getConnection()) {
            if (databaseManager.isSQLite()) {
                // Check for SQLite
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT name FROM sqlite_master WHERE type='table' AND name='players'")) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next();
                    }
                }
            } else {
                // Check for MySQL
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'players'")) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next();
                    }
                }
            }
        }
    }
    
    /**
     * Creates the initial database schema with all required tables.
     */
    private void createSchema() throws SQLException {
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            
            try {
                // Create players table
                createPlayersTable(connection);
                
                // Create auctions table
                createAuctionsTable(connection);
                
                // Create transactions table
                createTransactionsTable(connection);
                
                // Create auction_bids table
                createAuctionBidsTable(connection);
                
                // Create player_settings table
                createPlayerSettingsTable(connection);
                
                // Create schema_migrations table
                createSchemaMigrationsTable(connection);
                
                // Insert initial migration record
                insertInitialMigration(connection);
                
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
    
    /**
     * Creates the players table to store player economy data.
     */
    private void createPlayersTable(Connection connection) throws SQLException {
        String sql;
        if (databaseManager.isSQLite()) {
            sql = """
                CREATE TABLE players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(16) NOT NULL,
                    balance DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
                    bank_balance DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
                    total_earned DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
                    total_spent DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        } else {
            sql = """
                CREATE TABLE players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(16) NOT NULL,
                    balance DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
                    bank_balance DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
                    total_earned DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
                    total_spent DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_username (username),
                    INDEX idx_balance (balance),
                    INDEX idx_created_at (created_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;
        }
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            plugin.getLogger().info("Created players table");
        }
    }
    
    /**
     * Creates the auctions table to store auction house data.
     */
    private void createAuctionsTable(Connection connection) throws SQLException {
        String sql;
        if (databaseManager.isSQLite()) {
            sql = """
                CREATE TABLE auctions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    seller_uuid VARCHAR(36) NOT NULL,
                    item_name TEXT NOT NULL,
                    item_data TEXT NOT NULL,
                    starting_bid DECIMAL(20, 2) NOT NULL,
                    buyout_price DECIMAL(20, 2),
                    current_bid DECIMAL(20, 2) DEFAULT 0.00,
                    highest_bidder_uuid VARCHAR(36),
                    duration_seconds INTEGER NOT NULL,
                    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    end_time TIMESTAMP NOT NULL,
                    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                    category VARCHAR(50),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (seller_uuid) REFERENCES players(uuid)
                )
                """;
        } else {
            sql = """
                CREATE TABLE auctions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    seller_uuid VARCHAR(36) NOT NULL,
                    item_name TEXT NOT NULL,
                    item_data LONGTEXT NOT NULL,
                    starting_bid DECIMAL(20, 2) NOT NULL,
                    buyout_price DECIMAL(20, 2),
                    current_bid DECIMAL(20, 2) DEFAULT 0.00,
                    highest_bidder_uuid VARCHAR(36),
                    duration_seconds INT NOT NULL,
                    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    end_time TIMESTAMP NOT NULL,
                    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                    category VARCHAR(50),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (seller_uuid) REFERENCES players(uuid),
                    INDEX idx_seller (seller_uuid),
                    INDEX idx_status (status),
                    INDEX idx_end_time (end_time),
                    INDEX idx_category (category)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;
        }
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            plugin.getLogger().info("Created auctions table");
        }
    }
    
    /**
     * Creates the transactions table to store all money movements.
     */
    private void createTransactionsTable(Connection connection) throws SQLException {
        String sql;
        if (databaseManager.isSQLite()) {
            sql = """
                CREATE TABLE transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    from_uuid VARCHAR(36),
                    to_uuid VARCHAR(36),
                    amount DECIMAL(20, 2) NOT NULL,
                    type VARCHAR(50) NOT NULL,
                    description TEXT,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (from_uuid) REFERENCES players(uuid),
                    FOREIGN KEY (to_uuid) REFERENCES players(uuid)
                )
                """;
        } else {
            sql = """
                CREATE TABLE transactions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    from_uuid VARCHAR(36),
                    to_uuid VARCHAR(36),
                    amount DECIMAL(20, 2) NOT NULL,
                    type VARCHAR(50) NOT NULL,
                    description TEXT,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (from_uuid) REFERENCES players(uuid),
                    FOREIGN KEY (to_uuid) REFERENCES players(uuid),
                    INDEX idx_from_uuid (from_uuid),
                    INDEX idx_to_uuid (to_uuid),
                    INDEX idx_timestamp (timestamp),
                    INDEX idx_type (type)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;
        }
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            plugin.getLogger().info("Created transactions table");
        }
    }
    
    /**
     * Creates the auction_bids table to store bid history.
     */
    private void createAuctionBidsTable(Connection connection) throws SQLException {
        String sql;
        if (databaseManager.isSQLite()) {
            sql = """
                CREATE TABLE auction_bids (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    auction_id INTEGER NOT NULL,
                    bidder_uuid VARCHAR(36) NOT NULL,
                    bid_amount DECIMAL(20, 2) NOT NULL,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
                    FOREIGN KEY (bidder_uuid) REFERENCES players(uuid)
                )
                """;
        } else {
            sql = """
                CREATE TABLE auction_bids (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    auction_id INT NOT NULL,
                    bidder_uuid VARCHAR(36) NOT NULL,
                    bid_amount DECIMAL(20, 2) NOT NULL,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
                    FOREIGN KEY (bidder_uuid) REFERENCES players(uuid),
                    INDEX idx_auction_id (auction_id),
                    INDEX idx_bidder_uuid (bidder_uuid),
                    INDEX idx_timestamp (timestamp)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;
        }
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            plugin.getLogger().info("Created auction_bids table");
        }
    }
    
    /**
     * Creates the player_settings table to store player preferences.
     */
    private void createPlayerSettingsTable(Connection connection) throws SQLException {
        String sql;
        if (databaseManager.isSQLite()) {
            sql = """
                CREATE TABLE player_settings (
                    uuid VARCHAR(36) PRIMARY KEY,
                    language VARCHAR(10) DEFAULT 'en_US',
                    notifications_enabled BOOLEAN DEFAULT TRUE,
                    sounds_enabled BOOLEAN DEFAULT TRUE,
                    particles_enabled BOOLEAN DEFAULT TRUE,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (uuid) REFERENCES players(uuid)
                )
                """;
        } else {
            sql = """
                CREATE TABLE player_settings (
                    uuid VARCHAR(36) PRIMARY KEY,
                    language VARCHAR(10) DEFAULT 'en_US',
                    notifications_enabled BOOLEAN DEFAULT TRUE,
                    sounds_enabled BOOLEAN DEFAULT TRUE,
                    particles_enabled BOOLEAN DEFAULT TRUE,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (uuid) REFERENCES players(uuid),
                    INDEX idx_language (language)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;
        }
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            plugin.getLogger().info("Created player_settings table");
        }
    }
    
    /**
     * Creates the schema_migrations table to track migration versions.
     */
    private void createSchemaMigrationsTable(Connection connection) throws SQLException {
        String sql;
        if (databaseManager.isSQLite()) {
            sql = """
                CREATE TABLE schema_migrations (
                    version VARCHAR(50) PRIMARY KEY,
                    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    description TEXT
                )
                """;
        } else {
            sql = """
                CREATE TABLE schema_migrations (
                    version VARCHAR(50) PRIMARY KEY,
                    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    description TEXT,
                    INDEX idx_applied_at (applied_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;
        }
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            plugin.getLogger().info("Created schema_migrations table");
        }
    }
    
    /**
     * Inserts the initial migration record.
     */
    private void insertInitialMigration(Connection connection) throws SQLException {
        String sql = databaseManager.isSQLite() ?
            "INSERT INTO schema_migrations (version, description) VALUES (?, ?)" :
            "INSERT INTO schema_migrations (version, description) VALUES (?, ?)";
            
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, CURRENT_SCHEMA_VERSION);
            stmt.setString(2, "Initial schema creation");
            stmt.executeUpdate();
            plugin.getLogger().info("Inserted initial migration record: " + CURRENT_SCHEMA_VERSION);
        }
    }
    
    /**
     * Updates the schema if needed (for future migrations).
     */
    private void updateSchema() throws SQLException {
        // Check current version and apply migrations if needed
        String currentVersion = getCurrentSchemaVersion();
        
        if (!CURRENT_SCHEMA_VERSION.equals(currentVersion)) {
            plugin.getLogger().info("Schema version mismatch. Current: " + currentVersion + ", Required: " + CURRENT_SCHEMA_VERSION);
            // In a real implementation, we would apply migration scripts here
            // For now, we just log that an update is needed
            plugin.getLogger().info("Schema update would be applied here if needed");
        }
    }
    
    /**
     * Gets the current schema version from the database.
     */
    private String getCurrentSchemaVersion() throws SQLException {
        try (Connection connection = databaseManager.getConnection()) {
            String sql = "SELECT version FROM schema_migrations ORDER BY applied_at DESC LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("version");
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if a table exists in the database.
     */
    public boolean tableExists(String tableName) throws SQLException {
        try (Connection connection = databaseManager.getConnection()) {
            if (databaseManager.isSQLite()) {
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT name FROM sqlite_master WHERE type='table' AND name=?")) {
                    stmt.setString(1, tableName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next();
                    }
                }
            } else {
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?")) {
                    stmt.setString(1, tableName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next();
                    }
                }
            }
        }
    }
}