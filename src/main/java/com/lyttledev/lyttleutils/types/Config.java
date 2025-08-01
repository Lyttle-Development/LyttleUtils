package com.lyttledev.lyttleutils.types;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Config utility class for managing plugin configuration files.
 * Supports robust access to all possible YAML types and ensures safe reading/writing.
 * Includes defensive checks, comments, and reload/save routines.
 */
public class Config {
    private final String pluginFolderPath;
    private final String configPath;
    private YamlConfiguration config = null;
    private final JavaPlugin plugin;

    /**
     * Initialize Config with plugin and config file path.
     *
     * @param plugin     The JavaPlugin instance.
     * @param configPath The config file name or relative path.
     */
    public Config(JavaPlugin plugin, String configPath) {
        this.pluginFolderPath = plugin.getDataFolder().getPath();
        this.configPath = configPath;
        this.plugin = plugin;
    }

    /**
     * Loads and returns the current config, ensuring it's never null after call.
     * If loading fails, logs error and returns null.
     *
     * @return YamlConfiguration instance or null if failed.
     */
    private YamlConfiguration getConfig() {
        if (this.config == null) {
            try {
                // read the config as a string
                String originalConfigString = Files.readString(Paths.get(this.pluginFolderPath, this.configPath));

                // clean the config string
                String configString = this.cleanConfig(originalConfigString);

                // load the config from the string
                this.config = new YamlConfiguration();
                this.config.loadFromString(configString);

                // Save the cleaned config back to the file if it has changed
                if (!configString.equals(originalConfigString)) {
                    this.saveConfig();
                }
            } catch (InvalidConfigurationException | IOException var3) {
                this.config = null;
            }
        }

        if (this.config == null) {
            this.plugin.getLogger().severe("Failed to load config " + this.configPath);
            return null;
        } else {
            return this.config;
        }
    }

    /**
     * Cleans config string for YAML parsing (removes type tags).
     *
     * @param configString The original string.
     * @return Cleaned string.
     */
    private String cleanConfig(String configString) {
        configString = configString.replaceAll("!!.+", "");
        return configString;
    }

    /**
     * Saves the current config state to disk.
     * Attempts to clean and rewrite YAML for safety.
     */
    private void saveConfig() {
        try {
            String originalConfigString = this.config.saveToString();

            try {
                String configString = this.cleanConfig(originalConfigString);
                this.config.loadFromString(configString);
                this.config.save(new File(this.pluginFolderPath, this.configPath));
            } catch (IOException var3) {
                // Saving failed silently; consider logging
            }

            this.config.loadFromString(originalConfigString);
        } catch (InvalidConfigurationException var4) {
            // Loading failed silently; consider logging
        }
    }

    /**
     * Force reload from file.
     */
    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(new File(this.pluginFolderPath, this.configPath));
    }

    /**
     * Generic getter, returns Object (use type-specific for safety).
     */
    public @Nullable Object get(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path) ? cfg.get(path) : null;
    }

    /**
     * Getters for all common Bukkit YAML config types
     **/

    public @Nullable String getString(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path) ? cfg.getString(path) : null;
    }

    public @Nullable Integer getInt(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path) ? cfg.getInt(path) : null;
    }

    public @Nullable Long getLong(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path) ? cfg.getLong(path) : null;
    }

    public @Nullable Double getDouble(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path) ? cfg.getDouble(path) : null;
    }

    public @Nullable Boolean getBoolean(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path) ? cfg.getBoolean(path) : null;
    }

    public @Nullable List<?> getList(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path) ? cfg.getList(path) : null;
    }

    public @Nullable List<String> getStringList(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path) ? cfg.getStringList(path) : null;
    }

    public @Nullable List<Integer> getIntegerList(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path) ? cfg.getIntegerList(path) : null;
    }

    public @Nullable List<Double> getDoubleList(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path) ? cfg.getDoubleList(path) : null;
    }

    public @Nullable List<Boolean> getBooleanList(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path) ? cfg.getBooleanList(path) : null;
    }

    public @Nullable Map<String, Object> getMap(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path) ? cfg.getConfigurationSection(path).getValues(false) : null;
    }

    public @Nullable Set<String> getKeySet(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path) ? cfg.getConfigurationSection(path).getKeys(false) : null;
    }

    /**
     * Get a nested ConfigurationSection at path.
     */
    public @Nullable ConfigurationSection getSection(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path) ? cfg.getConfigurationSection(path) : null;
    }

    /**
     * Set a value at path and persist to disk.
     *
     * @param path  The config path.
     * @param value The new value.
     */
    public void set(String path, @Nullable Object value) {
        YamlConfiguration cfg = this.getConfig();
        cfg.set(path, value);
        this.saveConfig();
    }

    /**
     * Remove a value at path (sets to null and saves).
     *
     * @param path The config path.
     * @return true if removed, false if not present.
     */
    public boolean remove(String path) {
        YamlConfiguration cfg = this.getConfig();
        if (cfg.contains(path)) {
            cfg.set(path, null);
            this.saveConfig();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check existence of key at path.
     */
    public boolean contains(String path) {
        YamlConfiguration cfg = this.getConfig();
        return cfg != null && cfg.contains(path);
    }

    /**
     * Check existence of key (case-insensitive).
     */
    public boolean containsLowercase(String path) {
        YamlConfiguration cfg = this.getConfig();
        if (cfg == null) return false;
        for (String key : cfg.getKeys(false)) {
            if (key.toLowerCase().equalsIgnoreCase(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all immediate child keys under path as an array.
     */
    public String[] getKeys(String path) {
        YamlConfiguration cfg = this.getConfig();
        if (cfg == null || !cfg.contains(path)) return null;
        Set<String> keys = cfg.getConfigurationSection(path).getKeys(false);
        return keys.toArray(new String[0]);
    }

    /**
     * Get all values at path as an Object array.
     *
     * @param path The config section path.
     * @return All values, or empty array if none.
     */
    public Object[] getAll(String path) {
        YamlConfiguration cfg = this.getConfig();
        if (cfg == null || !cfg.contains(path)) return new Object[0];
        return cfg.getConfigurationSection(path).getValues(false).values().toArray(new Object[0]);
    }

    /**
     * Remove all top-level keys from config and reloads.
     * Use with caution!
     */
    public void clear() {
        File configFile = new File(this.pluginFolderPath, this.configPath);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        for (String key : config.getKeys(false)) {
            config.set(key, null);
        }

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.reload();
    }
}