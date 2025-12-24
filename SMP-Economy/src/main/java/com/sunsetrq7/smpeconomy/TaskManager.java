package com.sunsetrq7.smpeconomy;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages scheduled tasks for the plugin.
 */
public class TaskManager {
    
    private final SMP_Economy plugin;
    private final List<BukkitTask> scheduledTasks;
    
    public TaskManager(SMP_Economy plugin) {
        this.plugin = plugin;
        this.scheduledTasks = new ArrayList<>();
    }
    
    /**
     * Schedules daily interest calculation.
     */
    public void scheduleDailyInterest() {
        // Run every 24 hours (1728000 ticks = 24 hours)
        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plugin.getBankManager().calculateAndApplyInterest();
        }, 20 * 60 * 60 * 24, 20 * 60 * 60 * 24); // Every 24 hours
        
        scheduledTasks.add(task);
    }
    
    /**
     * Schedules regular backup tasks.
     */
    public void scheduleBackups() {
        int backupIntervalHours = plugin.getConfigManager().getMainConfig().getInt("backup_interval_hours", 24);
        
        if (plugin.getConfigManager().getMainConfig().getBoolean("backup_enabled", true)) {
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                plugin.getBackupManager().createBackup();
            }, 20 * 60 * 60 * backupIntervalHours, 20 * 60 * 60 * backupIntervalHours); // Every configured hours
            
            scheduledTasks.add(task);
        }
    }
    
    /**
     * Schedules cleanup tasks.
     */
    public void scheduleCleanup() {
        // Run cleanup every hour
        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            // Clean up expired auctions
            plugin.getAuctionHouseManager().processEndedAuctions();
            
            // Reset rate limiting counters
            plugin.getSecurityManager().resetTrackingData();
            
            // Clean up player data cache if needed
            // Additional cleanup tasks can be added here
        }, 20 * 60 * 60, 20 * 60 * 60); // Every hour
        
        scheduledTasks.add(task);
    }
    
    /**
     * Schedules metrics collection.
     */
    public void scheduleMetrics() {
        if (plugin.getConfigManager().getPerformanceConfig().getBoolean("enable_metrics", true)) {
            // Collect metrics every 30 minutes
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                plugin.getMetricsCollector().collectMetrics();
            }, 20 * 60 * 30, 20 * 60 * 30); // Every 30 minutes
            
            scheduledTasks.add(task);
        }
    }
    
    /**
     * Schedules update checks.
     */
    public void scheduleUpdateChecks() {
        if (plugin.getConfigManager().getMainConfig().getBoolean("auto_update", true)) {
            // Check for updates every 6 hours
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                plugin.getUpdateChecker().checkForUpdates();
            }, 20 * 60 * 60 * 6, 20 * 60 * 60 * 6); // Every 6 hours
            
            scheduledTasks.add(task);
        }
    }
    
    /**
     * Shuts down all scheduled tasks.
     */
    public void shutdown() {
        plugin.getLogger().info("Shutting down scheduled tasks...");
        
        for (BukkitTask task : scheduledTasks) {
            if (!task.isCancelled()) {
                task.cancel();
            }
        }
        
        scheduledTasks.clear();
        plugin.getLogger().info("All scheduled tasks have been cancelled.");
    }
}