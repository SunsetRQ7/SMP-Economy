package com.sunsetrq7.smpeconomy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages data caching for improved performance.
 */
public class CacheManager {
    
    private final SMP_Economy plugin;
    
    // Player data cache
    private final Map<UUID, Object> playerDataCache;
    
    // Item cache
    private final Map<String, Object> itemCache;
    
    // General purpose cache
    private final Map<String, Object> generalCache;
    
    public CacheManager(SMP_Economy plugin) {
        this.plugin = plugin;
        this.playerDataCache = new ConcurrentHashMap<>();
        this.itemCache = new ConcurrentHashMap<>();
        this.generalCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Gets player data from cache.
     */
    public Object getPlayerData(UUID playerId) {
        return playerDataCache.get(playerId);
    }
    
    /**
     * Adds player data to cache.
     */
    public void setPlayerData(UUID playerId, Object data) {
        int maxCachedPlayers = plugin.getConfigManager().getPerformanceConfig().getInt("max_cached_players", 1000);
        
        if (playerDataCache.size() >= maxCachedPlayers) {
            // Remove oldest entries if cache is full
            playerDataCache.remove(playerDataCache.keySet().iterator().next());
        }
        
        playerDataCache.put(playerId, data);
    }
    
    /**
     * Removes player data from cache.
     */
    public void removePlayerData(UUID playerId) {
        playerDataCache.remove(playerId);
    }
    
    /**
     * Gets an item from cache.
     */
    public Object getItem(String itemId) {
        return itemCache.get(itemId);
    }
    
    /**
     * Adds an item to cache.
     */
    public void setItem(String itemId, Object item) {
        itemCache.put(itemId, item);
    }
    
    /**
     * Removes an item from cache.
     */
    public void removeItem(String itemId) {
        itemCache.remove(itemId);
    }
    
    /**
     * Gets data from general cache.
     */
    public Object get(String key) {
        return generalCache.get(key);
    }
    
    /**
     * Adds data to general cache.
     */
    public void set(String key, Object value) {
        generalCache.put(key, value);
    }
    
    /**
     * Removes data from general cache.
     */
    public void remove(String key) {
        generalCache.remove(key);
    }
    
    /**
     * Checks if a player's data is in cache.
     */
    public boolean isPlayerDataCached(UUID playerId) {
        return playerDataCache.containsKey(playerId);
    }
    
    /**
     * Checks if an item is in cache.
     */
    public boolean isItemCached(String itemId) {
        return itemCache.containsKey(itemId);
    }
    
    /**
     * Checks if a key is in general cache.
     */
    public boolean isCached(String key) {
        return generalCache.containsKey(key);
    }
    
    /**
     * Clears all caches.
     */
    public void clearAll() {
        playerDataCache.clear();
        itemCache.clear();
        generalCache.clear();
    }
    
    /**
     * Clears player data cache.
     */
    public void clearPlayerDataCache() {
        playerDataCache.clear();
    }
    
    /**
     * Clears item cache.
     */
    public void clearItemCache() {
        itemCache.clear();
    }
    
    /**
     * Clears general cache.
     */
    public void clearGeneralCache() {
        generalCache.clear();
    }
    
    /**
     * Gets the size of the player data cache.
     */
    public int getPlayerDataCacheSize() {
        return playerDataCache.size();
    }
    
    /**
     * Gets the size of the item cache.
     */
    public int getItemCacheSize() {
        return itemCache.size();
    }
    
    /**
     * Gets the size of the general cache.
     */
    public int getGeneralCacheSize() {
        return generalCache.size();
    }
    
    /**
     * Gets the total cache size.
     */
    public int getTotalCacheSize() {
        return getPlayerDataCacheSize() + getItemCacheSize() + getGeneralCacheSize();
    }
}