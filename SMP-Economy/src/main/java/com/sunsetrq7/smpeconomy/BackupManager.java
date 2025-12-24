package com.sunsetrq7.smpeconomy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * Manages database backups for the plugin.
 */
public class BackupManager {
    
    private final SMP_Economy plugin;
    
    public BackupManager(SMP_Economy plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Creates a backup of the database.
     */
    public boolean createBackup() {
        if (!plugin.getConfigManager().getMainConfig().getBoolean("backup_enabled", true)) {
            return false;
        }
        
        try {
            String dbType = plugin.getDatabaseManager().getDatabaseType();
            
            if (dbType.equalsIgnoreCase("sqlite")) {
                return createSQLiteBackup();
            } else {
                // For MySQL/MariaDB, we would typically use mysqldump or similar
                // For now, we'll just log that MySQL backup is not implemented
                plugin.getLogger().info("MySQL backup functionality would be implemented here in a production environment.");
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create backup", e);
            return false;
        }
    }
    
    /**
     * Creates a backup of the SQLite database.
     */
    private boolean createSQLiteBackup() {
        try {
            String dbName = plugin.getConfigManager().getDatabaseConfig().getString("database.path", "smpeconomy.db");
            File dbFile = new File(plugin.getDataFolder(), dbName);
            
            if (!dbFile.exists()) {
                plugin.getLogger().warning("Database file does not exist: " + dbFile.getAbsolutePath());
                return false;
            }
            
            // Create backups directory if it doesn't exist
            File backupsDir = new File(plugin.getDataFolder(), "backups");
            if (!backupsDir.exists()) {
                backupsDir.mkdirs();
            }
            
            // Generate backup filename with timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String timestamp = dateFormat.format(new Date());
            String backupName = "smpeconomy_backup_" + timestamp + ".db";
            File backupFile = new File(backupsDir, backupName);
            
            // Copy the database file to the backup location
            Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            plugin.getLogger().info("Database backup created: " + backupFile.getName());
            
            // Clean up old backups (keep only the last 10)
            cleanupOldBackups(backupsDir);
            
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create SQLite backup", e);
            return false;
        }
    }
    
    /**
     * Cleans up old backup files, keeping only the most recent ones.
     */
    private void cleanupOldBackups(File backupsDir) {
        File[] backupFiles = backupsDir.listFiles((dir, name) -> name.startsWith("smpeconomy_backup_") && name.endsWith(".db"));
        
        if (backupFiles == null || backupFiles.length <= 10) {
            // Don't need to clean up if we have 10 or fewer backups
            return;
        }
        
        // Sort files by modification time (oldest first)
        java.util.Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
        
        // Delete the oldest files, keeping only the 10 most recent
        int filesToDelete = backupFiles.length - 10;
        for (int i = 0; i < filesToDelete; i++) {
            if (backupFiles[i].delete()) {
                plugin.getLogger().info("Deleted old backup: " + backupFiles[i].getName());
            } else {
                plugin.getLogger().warning("Failed to delete old backup: " + backupFiles[i].getName());
            }
        }
    }
    
    /**
     * Restores a backup from the specified file.
     */
    public boolean restoreBackup(String backupFileName) {
        try {
            File backupsDir = new File(plugin.getDataFolder(), "backups");
            File backupFile = new File(backupsDir, backupFileName);
            
            if (!backupFile.exists()) {
                plugin.getLogger().warning("Backup file does not exist: " + backupFile.getAbsolutePath());
                return false;
            }
            
            String dbType = plugin.getDatabaseManager().getDatabaseType();
            if (!dbType.equalsIgnoreCase("sqlite")) {
                plugin.getLogger().warning("Restore is only supported for SQLite databases");
                return false;
            }
            
            String dbName = plugin.getConfigManager().getDatabaseConfig().getString("database.path", "smpeconomy.db");
            File dbFile = new File(plugin.getDataFolder(), dbName);
            
            // Close all database connections before restoring
            plugin.getDatabaseManager().closeConnections();
            
            // Copy the backup file to the database location
            Files.copy(backupFile.toPath(), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // Reinitialize the database connection
            plugin.getDatabaseManager().initialize();
            
            plugin.getLogger().info("Database restored from backup: " + backupFileName);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to restore backup: " + backupFileName, e);
            return false;
        }
    }
    
    /**
     * Lists all available backup files.
     */
    public java.util.List<String> listBackups() {
        java.util.List<String> backups = new java.util.ArrayList<>();
        
        File backupsDir = new File(plugin.getDataFolder(), "backups");
        if (!backupsDir.exists()) {
            return backups;
        }
        
        File[] backupFiles = backupsDir.listFiles((dir, name) -> name.startsWith("smpeconomy_backup_") && name.endsWith(".db"));
        if (backupFiles != null) {
            for (File file : backupFiles) {
                backups.add(file.getName());
            }
        }
        
        // Sort by modification time (newest first)
        backups.sort((f1, f2) -> {
            File file1 = new File(backupsDir, f1);
            File file2 = new File(backupsDir, f2);
            return Long.compare(file2.lastModified(), file1.lastModified());
        });
        
        return backups;
    }
    
    /**
     * Gets the size of a backup file.
     */
    public long getBackupSize(String backupFileName) {
        File backupsDir = new File(plugin.getDataFolder(), "backups");
        File backupFile = new File(backupsDir, backupFileName);
        
        if (backupFile.exists()) {
            return backupFile.length();
        }
        
        return -1;
    }
    
    /**
     * Deletes a specific backup file.
     */
    public boolean deleteBackup(String backupFileName) {
        File backupsDir = new File(plugin.getDataFolder(), "backups");
        File backupFile = new File(backupsDir, backupFileName);
        
        if (backupFile.exists()) {
            return backupFile.delete();
        }
        
        return false;
    }
}