package com.lyttledev.lyttleUtils;

import com.lyttledev.lyttleUtils.utils.Message;
import com.lyttledev.lyttleUtils.utils.Console;

import com.lyttledev.lyttleUtils.types.Configs;
import org.bukkit.plugin.java.JavaPlugin;

public final class LyttleUtils extends JavaPlugin {
    public Configs config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Setup config after creating the configs
        config = new Configs(this);
        // Migrate config
        migrateConfig();

        // Plugin startup logic
        Console.init(this);
        Message.init(this);
        saveDefaultConfig();

    }

    private void migrateConfig() {
        if (!config.general.contains("config_version")) {
            config.general.set("config_version", 0);
        }

        switch (config.general.get("config_version").toString()) {
            case "0":
                migrateConfig();
                break;
            default:
                break;
        }
    }
}