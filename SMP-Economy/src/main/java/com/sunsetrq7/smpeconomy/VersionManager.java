package com.sunsetrq7.smpeconomy;

import org.bukkit.Bukkit;

/**
 * Manages version compatibility and NMS abstraction.
 */
public class VersionManager {
    
    private final SMP_Economy plugin;
    private final String serverVersion;
    private final String nmsVersion;
    
    public VersionManager(SMP_Economy plugin) {
        this.plugin = plugin;
        this.serverVersion = Bukkit.getBukkitVersion();
        this.nmsVersion = getNMSVersion();
    }
    
    /**
     * Gets the NMS version string.
     */
    private String getNMSVersion() {
        String version = Bukkit.getServer().getClass().getPackage().getName();
        return version.substring(version.lastIndexOf('.') + 1);
    }
    
    /**
     * Gets the server version.
     */
    public String getServerVersion() {
        return serverVersion;
    }
    
    /**
     * Gets the NMS version.
     */
    public String getNMSVersion() {
        return nmsVersion;
    }
    
    /**
     * Checks if the server version is supported.
     */
    public boolean isVersionSupported() {
        // Check if the server version is in the supported range (1.21 - 1.21.10)
        return isVersionInRange("1.21", "1.21.10");
    }
    
    /**
     * Checks if the server version is within a specific range.
     */
    public boolean isVersionInRange(String minVersion, String maxVersion) {
        try {
            // Extract version numbers
            double currentVersion = extractVersionNumber(serverVersion);
            double minVersionNum = extractVersionNumber(minVersion);
            double maxVersionNum = extractVersionNumber(maxVersion);
            
            return currentVersion >= minVersionNum && currentVersion <= maxVersionNum;
        } catch (Exception e) {
            // If we can't parse the version, assume it's supported
            return true;
        }
    }
    
    /**
     * Extracts the version number from a version string.
     */
    private double extractVersionNumber(String versionString) {
        // Extract major.minor version (e.g., from "1.21.1-R0.1-SNAPSHOT" get 1.21)
        String[] parts = versionString.split("\\.");
        if (parts.length >= 2) {
            try {
                int major = Integer.parseInt(parts[0]);
                int minor = Integer.parseInt(parts[1]);
                return major + (minor / 100.0);
            } catch (NumberFormatException e) {
                // If parsing fails, return a default value
                return 1.21;
            }
        }
        return 1.21;
    }
    
    /**
     * Gets the compatibility status message.
     */
    public String getCompatibilityMessage() {
        if (isVersionSupported()) {
            return "Server version " + serverVersion + " is fully supported.";
        } else {
            return "Server version " + serverVersion + " may not be fully supported. Some features may not work correctly.";
        }
    }
    
    /**
     * Gets the version-specific implementation for a feature.
     */
    public Object getVersionSpecificImplementation(String feature) {
        // This would return version-specific implementations in a real plugin
        // For now, we'll just return a generic implementation
        return new Object();
    }
}