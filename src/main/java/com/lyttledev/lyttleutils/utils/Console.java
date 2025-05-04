package com.lyttledev.lyttleutils.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Console {
    public static JavaPlugin plugin;

    public static void init(JavaPlugin plugin) {
        Console.plugin = plugin;
    }

    public static void run(String command) {
        if (command == null || command.isEmpty()) return;
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        Bukkit.getScheduler().callSyncMethod( plugin, () -> Bukkit.dispatchCommand( console, command ) );
    }

    public static void log(String message) {
        plugin.getLogger().info(message);
    }
}
