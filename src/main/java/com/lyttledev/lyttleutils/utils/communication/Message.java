package com.lyttledev.lyttleutils.utils.communication;

import com.lyttledev.lyttleutils.types.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

/**
 * Message class to handle sending messages to players and console.
 * It uses MiniMessage for formatting and supports message replacement.
 */
public class Message {
    public static JavaPlugin plugin;
    public static Config messages;

    /**
     * Initialize the Message class with the plugin instance and messages configuration.
     *
     * @param plugin  The JavaPlugin instance
     * @param messages The Config instance for messages
     */
    public static void init(JavaPlugin plugin, Config messages) {
        Message.plugin = plugin;
        Message.messages = messages;
    }

    /**
     * Get the prefix from the messages configuration.
     *
     * @return The prefix string
     */
    private static String _getPrefix() {
        return _getConfigMessage("prefix");
    }

    /**
     * Get a message from the messages configuration.
     *
     * @param messageKey The key for the message
     * @return The message string
     */
    private static String _getConfigMessage(String messageKey) {
        @Nullable String message = (String) messages.get(messageKey);
        if (message == null) {
            Console.log("Message key " + messageKey + " not found in messages.yml");
            message = (String) messages.get("message_not_found");
        }

        if (message == null) {
            Console.log("Even the message_not_found not found in messages.yml...");
            message = "&cOh... I can't react to that. (Contact the Administrators)";
        }

        return message;
    }

    /**
     * Get a message from the messages configuration without the prefix.
     *
     * @param messageKey The key for the message
     * @return The message string
     */
    public static String getConfigMessage(String messageKey) {
        return _getConfigMessage(messageKey);
    }

    /**
     * Get a message from the messages configuration with the prefix.
     *
     * @param message The message string
     * @param replacements The replacements to be made in the message
     * @return The formatted message string
     */
    private static String _replaceMessageStrings(String message, String[][] replacements) {
        for (String[] replacement : replacements) {
            message = message.replace(replacement[0], replacement[1]);
        }
        return message;
    }

    /**
     * Send a message to a target (Player or ConsoleCommandSender).
     *
     * @param target The target to send the message to
     * @param messageKey The key for the message
     */
    public static void sendMessage(Object target, String messageKey) {
        Component msg = _getMessage(_getPrefix() + _getConfigMessage(messageKey));
        _sendMessage(target, msg);
    }

    /**
     * Send a message to a target (Player or ConsoleCommandSender) with replacements.
     *
     * @param target The target to send the message to
     * @param message The message string
     * @param replacements The replacements to be made in the message
     */
    public static void sendMessage(Object target, String message, String[][] replacements) {
        Component msg = _getMessage(_getPrefix() + _replaceMessageStrings(_getConfigMessage(message), replacements));
        _sendMessage(target, msg);
    }

    /**
     * Send a raw message to a target (Player or ConsoleCommandSender).
     *
     * @param target The target to send the message to
     * @param message The message string
     */
    public static void sendMessageRaw(Object target, String message) {
        Component msg = _getMessage(_getPrefix() + message);
        _sendMessage(target, msg);
    }

    /**
     * Send a raw message to a target (Player or ConsoleCommandSender) without the prefix.
     *
     * @param target The target to send the message to
     * @param message The message string
     */
    private static void _sendMessage(Object target, Component message) {
        if (target instanceof Player) {
            ((Player) target).sendMessage(message);
        }
        if (target instanceof ConsoleCommandSender) {
            ((ConsoleCommandSender) target).sendMessage(message);
        }
    }

    /**
     * Send a message to all players on the server.
     *
     * @param message The message string
     * @param replacements The replacements to be made in the message
     */
    public static void sendBroadcast(String message, String[][] replacements) {
        String msg = _replaceMessageStrings(_getConfigMessage(message), replacements);
        Bukkit.broadcast(_getMessage(msg));
    }

    /**
     * Send a message to all players on the server with a prefix.
     *
     * @param message The message string
     * @param replacements The replacements to be made in the message
     * @param prefix Whether to include the prefix or not
     */
    public static void sendBroadcast(String message, String[][] replacements, boolean prefix) {
        String msg = _replaceMessageStrings(_getConfigMessage(message), replacements);
        sendBroadcast(msg, prefix);
    }

    /**
     * Send a message to all players on the server with a prefix.
     *
     * @param message The message string
     * @param prefix Whether to include the prefix or not
     */
    public static void sendBroadcast(String message, boolean prefix) {
        if (prefix) {
            Bukkit.broadcast(_getMessage(_getPrefix() + message));
            return;
        }
        Bukkit.broadcast(_getMessage(message));
    }

    /**
     * Send a message to all players on the server without a prefix.
     *
     * @param message The message string
     */
    public static void sendBroadcast(String message) {
        Bukkit.broadcast(_getMessage(message));
    }

    /**
     * Get a message from the messages configuration with the prefix.
     *
     * @param message The message string
     * @return The formatted message string
     */
    private static Component _getMessage(String message) {
        // Replace all \n with real newlines
        message = message.replace("\\n", "\n");
        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component getMessage(String message, String[][] replacements) {
        return _getMessage(_replaceMessageStrings(_getConfigMessage(message), replacements));
    }
}
