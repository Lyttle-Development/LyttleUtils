package com.lyttledev.lyttleutils;

// Export the classes to be used in the plugin
import com.lyttledev.lyttleutils.utils.communication.Console;

import org.bukkit.plugin.java.JavaPlugin;

public final class LyttleUtils extends JavaPlugin {
    @Override
    public void onEnable() {
        // Print the plugin version
        Console.init(this);
        Console.log("LyttleUtils " + getDescription().getVersion() + " is enabled!");
    }
}