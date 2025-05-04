package com.lyttledev.lyttleutils.utils.communication;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Console class to run commands and log messages to the console
 * @author LyttleDev
 * @version 1.0
 */
public class Console {
    public static JavaPlugin plugin;

    /*
     * Initialize the Console class
     * @param plugin The plugin instance
     */
    public static void init(JavaPlugin plugin) {
        Console.plugin = plugin;
    }

    /*
     * Run a command as the console
     * @param command The command to run
     */
    public static void run(String command) {
        if (command == null || command.isEmpty()) return;
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        Bukkit.getScheduler().callSyncMethod( plugin, () -> Bukkit.dispatchCommand( console, command ) );
    }

    /*
     * Run a command as a player
     * @param player The player to run the command as
     * @param command The command to run
     */
    public static void run(Player player, String command) {
        if (command == null || command.isEmpty()) return;
        Bukkit.getScheduler().callSyncMethod(plugin, () -> Bukkit.dispatchCommand(player, command));
    }

    /*
     * Send a message to the console
     * @param message The message to send
     */
    public static void log(String message) {
        plugin.getLogger().info(message);
    }
}
