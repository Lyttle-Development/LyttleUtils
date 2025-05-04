package com.lyttledev.lyttleUtils;

import com.lyttledev.lyttleUtils.utils.Console;
import org.bukkit.plugin.java.JavaPlugin;

public final class LyttleUtils extends JavaPlugin {
    @Override
    public void onEnable() {
        // Print the plugin version
        Console.log("LyttleUtils " + getDescription().getVersion() + " is enabled!");
    }
}