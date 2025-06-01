package com.lyttledev.lyttleutils.utils.storage;

import com.lyttledev.lyttleutils.types.Config;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * GlobalConfig manages a single YAML-based configuration file shared by all plugins
 * placing it under "plugins/LyttleDevelopment/global.yml". It uses the shared Config type
 * to load, save, and migrate configuration entries.
 * <p>
 * Usage in any plugin:
 * <pre>
 *     public class MyPlugin extends JavaPlugin {
 *         private GlobalConfig globalConfig;
 *
 *         @Override
 *         public void onEnable() {
 *             this.globalConfig = new GlobalConfig(this);
 *             // Now you can call: globalConfig.get("some.key");
 *         }
 *     }
 * </pre>
 */
public class GlobalConfig {
    // Relative path FROM the plugin's data folder to the global.yml in plugins/LyttleDevelopment
    private static final String RELATIVE_CONFIG_PATH = "../LyttleDevelopment/global.yml";
    // Default contents for a fresh global.yml
    private static final String DEFAULT_CONFIG_CONTENT =
            "# Global configuration for all LyttleDevelopment plugins\n" +
                    "config_version: 0\n" +
                    "\n" +
                    "# You can add other default keys here:\n" +
                    "# example:\n" +
                    "# some_setting: \"default value\"\n";

    private final JavaPlugin plugin;
    private final Config config;

    /**
     * Constructs (and if necessary, initializes) the global.yml under
     * plugins/LyttleDevelopment/global.yml. If the directory or file does not exist,
     * it will be created and populated with default content. Afterwards, any migration
     * based on config_version will run automatically.
     *
     * @param plugin any JavaPlugin instance (typically the plugin that is calling this)
     */
    public GlobalConfig(JavaPlugin plugin) {
        this.plugin = plugin;

        // Step 1: Ensure the "plugins/LyttleDevelopment" directory exists
        File pluginsDir = plugin.getDataFolder().getParentFile(); // this points at "plugins/"
        File globalFolder = new File(pluginsDir, "LyttleDevelopment");
        if (!globalFolder.exists()) {
            boolean created = globalFolder.mkdirs();
            if (!created) {
                plugin.getLogger().severe("Could not create LyttleDevelopment directory at " + globalFolder.getPath());
            }
        }

        // Step 2: Ensure the global.yml file exists; if not, write the DEFAULT_CONFIG_CONTENT to it
        File globalFile = new File(globalFolder, "global.yml");
        if (!globalFile.exists()) {
            try (FileWriter writer = new FileWriter(globalFile)) {
                writer.write(DEFAULT_CONFIG_CONTENT);
                writer.flush();
                plugin.getLogger().info("Created new global config at " + globalFile.getPath());
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create global.yml: " + e.getMessage());
            }
        }

        // Step 3: Instantiate the shared Config object, pointing it at "../LyttleDevelopment/global.yml"
        this.config = new Config(plugin, RELATIVE_CONFIG_PATH);

        // Step 4: Trigger loading (and saving) so that cleanConfig runs, then migrate if needed
        migrateIfNeeded();
    }

    /**
     * Checks the current config_version in the "general" section (top‐level), and applies
     * migrations if needed. Each migration case should update config_version and then re‐invoke
     * migrateIfNeeded() to ensure cascading upgrades.
     */
    private void migrateIfNeeded() {
        // Ensure there's a top‐level "config_version" key
        if (!config.contains("config_version")) {
            config.set("config_version", 0);
        }

        switch (config.get("config_version").toString()) {
//            case "0":
//                // Migrate config entries.
//
//                // Update config version.
//                config.set("config_version", 1);
//
//                // Recheck if the config is fully migrated.
//                migrateIfNeeded();
//                break;

            default:
                // Already up‐to‐date (no further migrations)
                break;
        }
    }

    /**
     * Retrieves the value at the given path from global.yml as a String.
     * Returns null if the key does not exist.
     *
     * @param path the YAML path (e.g. "some_section.some_key")
     * @return the String value, or null if absent
     */
    public String get(String path) {
        Object value = config.get(path);
        return (value != null) ? value.toString() : null;
    }

    /**
     * Sets (or updates) the value at the given path in global.yml.
     * After setting, it is saved to disk immediately.
     *
     * @param path  the YAML path (e.g. "some_section.some_key")
     * @param value the new value. If null, the key is removed.
     */
    public void set(String path, Object value) {
        if (value == null) {
            config.remove(path);
        } else {
            config.set(path, value);
        }
    }

    /**
     * Checks whether a given path exists in the global config.
     *
     * @param path the YAML path
     * @return true if present, false otherwise
     */
    public boolean contains(String path) {
        return config.contains(path);
    }
}
