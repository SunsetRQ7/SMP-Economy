package com.sunsetrq7.smpeconomy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages multi-language support for the plugin.
 */
public class LanguageManager {
    
    private final SMP_Economy plugin;
    private final Map<String, FileConfiguration> languageFiles;
    private String defaultLanguage;
    
    public LanguageManager(SMP_Economy plugin) {
        this.plugin = plugin;
        this.languageFiles = new HashMap<>();
        this.defaultLanguage = "en_US";
    }
    
    /**
     * Loads all language files.
     */
    public void loadLanguages() {
        plugin.getLogger().info("Loading language files...");
        
        // Create language directory if it doesn't exist
        File languageDir = new File(plugin.getDataFolder(), "language");
        if (!languageDir.exists()) {
            languageDir.mkdirs();
        }
        
        // Load default language first
        loadLanguageFile("en_US");
        
        // Load other languages
        loadLanguageFile("fa_IR"); // Persian
        loadLanguageFile("tr_TR"); // Turkish
        loadLanguageFile("ru_RU"); // Russian
        loadLanguageFile("es_ES"); // Spanish
        
        // Set default language from config
        defaultLanguage = plugin.getConfigManager().getMainConfig().getString("default_language", "en_US");
        
        plugin.getLogger().info("Loaded " + languageFiles.size() + " language files.");
    }
    
    /**
     * Loads a specific language file.
     */
    private void loadLanguageFile(String languageCode) {
        String fileName = "language/" + languageCode + ".yml";
        File languageFile = new File(plugin.getDataFolder(), fileName);
        
        // Copy default language file from resources if it doesn't exist
        if (!languageFile.exists()) {
            plugin.saveResource(fileName, false);
        }
        
        // Load the language file
        FileConfiguration config = YamlConfiguration.loadConfiguration(languageFile);
        
        // Also load from the jar if available
        InputStream inputStream = plugin.getResource(fileName);
        if (inputStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
            config.setDefaults(defaultConfig);
        }
        
        languageFiles.put(languageCode, config);
        
        plugin.getLogger().info("Loaded language file: " + languageCode);
    }
    
    /**
     * Gets a message in the specified language.
     */
    public String getMessage(String languageCode, String key) {
        FileConfiguration config = languageFiles.get(languageCode);
        
        // If the specific language doesn't exist, use default
        if (config == null) {
            config = languageFiles.get(defaultLanguage);
        }
        
        // If the key doesn't exist in the specific language, try default
        if (config == null || !config.contains(key)) {
            config = languageFiles.get(defaultLanguage);
        }
        
        // If it still doesn't exist, return the key itself
        if (config == null || !config.contains(key)) {
            plugin.getLogger().log(Level.WARNING, "Missing translation key: " + key + " for language: " + languageCode);
            return key;
        }
        
        String message = config.getString(key);
        
        // Replace color codes
        if (message != null) {
            message = formatColors(message);
        }
        
        return message;
    }
    
    /**
     * Gets a message in the player's language.
     */
    public String getMessageForPlayer(String playerLanguage, String key) {
        return getMessage(playerLanguage, key);
    }
    
    /**
     * Gets a message with placeholders replaced.
     */
    public String getMessage(String languageCode, String key, Object... placeholders) {
        String message = getMessage(languageCode, key);
        
        if (placeholders.length > 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                if (i + 1 < placeholders.length) {
                    String placeholder = "{" + placeholders[i] + "}";
                    String value = String.valueOf(placeholders[i + 1]);
                    message = message.replace(placeholder, value);
                }
            }
        }
        
        return message;
    }
    
    /**
     * Formats color codes in a message.
     */
    private String formatColors(String message) {
        if (message == null) {
            return null;
        }
        
        // Replace & with § for Minecraft color codes
        return message.replace("&", "§");
    }
    
    /**
     * Gets all available languages.
     */
    public java.util.Set<String> getAvailableLanguages() {
        return languageFiles.keySet();
    }
    
    /**
     * Gets the default language.
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }
    
    /**
     * Sets the default language.
     */
    public void setDefaultLanguage(String languageCode) {
        if (languageFiles.containsKey(languageCode)) {
            this.defaultLanguage = languageCode;
        }
    }
    
    /**
     * Reloads all language files.
     */
    public void reloadLanguages() {
        languageFiles.clear();
        loadLanguages();
    }
    
    /**
     * Saves a language file to disk.
     */
    public boolean saveLanguageFile(String languageCode) {
        FileConfiguration config = languageFiles.get(languageCode);
        if (config == null) {
            return false;
        }
        
        String fileName = "language/" + languageCode + ".yml";
        File languageFile = new File(plugin.getDataFolder(), fileName);
        
        try {
            config.save(languageFile);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save language file: " + languageCode, e);
            return false;
        }
    }
    
    /**
     * Adds or updates a translation key in a language file.
     */
    public void setTranslation(String languageCode, String key, String value) {
        FileConfiguration config = languageFiles.get(languageCode);
        if (config != null) {
            config.set(key, value);
        }
    }
    
    /**
     * Gets the language configuration for a specific language.
     */
    public FileConfiguration getLanguageConfig(String languageCode) {
        return languageFiles.get(languageCode);
    }
}