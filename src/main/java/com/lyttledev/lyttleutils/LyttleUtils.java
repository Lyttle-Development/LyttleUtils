package com.lyttledev.lyttleutils;

import com.lyttledev.lyttleutils.utils.communication.Console;
import org.bukkit.plugin.java.JavaPlugin;

public final class LyttleUtils extends JavaPlugin {
    Console console;

    @Override
    public void onEnable() {
        // Print the plugin version
        this.console = new Console(this);
        console.log("LyttleUtils " + getDescription().getVersion() + " is enabled!");
    }
}