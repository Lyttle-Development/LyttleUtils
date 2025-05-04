package com.lyttledev.lyttleUtils.utils;

import com.lyttledev.lyttleUtils.LyttleUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class Console {
    public static LyttleUtils plugin;

    public static void init(LyttleUtils plugin) {
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
