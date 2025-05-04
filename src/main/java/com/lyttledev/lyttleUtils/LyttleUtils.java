package com.lyttledev.lyttleUtils;

// Export the classes to be used in the plugin
import com.lyttledev.lyttleUtils.utils.Console;
import com.lyttledev.lyttleUtils.types.Config;
import com.lyttledev.lyttleUtils.utils.Message;

import org.bukkit.plugin.java.JavaPlugin;

public final class LyttleUtils extends JavaPlugin {
    @Override
    public void onEnable() {
        // Print the plugin version
        Console.log("LyttleUtils " + getDescription().getVersion() + " is enabled!");
    }
}