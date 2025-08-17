package com.lyttledev.lyttleutils.utils.storage;

import com.lyttledev.lyttleutils.types.YamlConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * GlobalConfig manages a single YAML-based configuration file shared by all plugins
 * placing it under "plugins/LyttleDevelopment/global.yml". It uses the shared YamlConfig type
 * to load, save, and migrate configuration entries. Additionally, it watches the file
 * for external changes and reloads its cache automatically.
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
                    "enabled: false\n" +
                    "\n" +
                    "################\n" +
                    "# Message Prefix\n" +
                    "################\n" +
                    "prefix: <dark_aqua><bold>Lyttle<aqua><bold>Development<reset> <dark_gray>- <gray>\n" +
                    "\n" +
                    "##########################\n" +
                    "# Generic Error Messages #\n" +
                    "##########################\n" +
                    "no_permission: <red>You do not have permission to do that!\n" +
                    "player_not_found: <dark_red>Player not found\n" +
                    "must_be_player: <red>You must be a player to use this command!\n" +
                    "message_not_found: <red>Oh... I can't react to that. (Contact the Administrators)\n" +
                    "\n" +
                    "##########################\n" +
                    "\n" +
                    "# Used internally for configuration updates.\n" +
                    "# ⚠\uFE0F Do not change this value.\n" +
                    "config_version: 0";

    private final JavaPlugin plugin;
    private final YamlConfig config;
    private WatchService watchService;

    /**
     * Constructs (and if necessary, initializes) the global.yml under
     * plugins/LyttleDevelopment/global.yml. If the directory or file does not exist,
     * it will be created and populated with default content. Afterwards, any migration
     * based on config_version will run automatically. Also starts a file watcher so that
     * if global.yml is modified externally, the cached YamlConfig is reloaded.
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

        // Step 3: Instantiate the shared YamlConfig object, pointing it at "../LyttleDevelopment/global.yml"
        this.config = new YamlConfig(plugin, RELATIVE_CONFIG_PATH);

        // Step 4: Trigger loading (and saving) so that cleanConfig runs, then migrate if needed
        migrateIfNeeded();

        // Step 5: Start the file watcher on global.yml so that external changes reload the cache
        startWatcher(globalFolder.toPath(), "global.yml");
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
            // Example migration stubs:
            // case "0":
            //     // Perform migration from 0 → 1 here...
            //     config.set("config_version", 1);
            //     plugin.getLogger().info("Migrated global.yml from version 0 to 1");
            //     migrateIfNeeded();
            //     break;

            default:
                // Already up‐to‐date (no further migrations)
                break;
        }
    }

    /**
     * Starts a WatchService watching the given folder for modifications to the specified file name.
     * When an external modify event on that file occurs, the local YamlConfig cache is reloaded.
     *
     * @param folderPath the folder containing the file to watch
     * @param fileName   the exact file name to monitor (e.g. "global.yml")
     */
    private void startWatcher(Path folderPath, String fileName) {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            folderPath.register(watchService, ENTRY_MODIFY);

            Thread watcherThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == ENTRY_MODIFY) {
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path changed = ev.context();
                            if (changed.getFileName().toString().equals(fileName)) {
                                // Reload the underlying YamlConfig
                                config.reload();
                                plugin.getLogger().info("Detected external change in global.yml; reloaded cache.");
                            }
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            }, "GlobalConfig-Watcher");

            watcherThread.setDaemon(true);
            watcherThread.start();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start WatchService for global.yml: " + e.getMessage());
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
