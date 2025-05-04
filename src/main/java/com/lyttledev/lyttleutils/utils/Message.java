package com.lyttledev.lyttleutils.utils;

import com.lyttledev.lyttleutils.types.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

public class Message {
    public static JavaPlugin plugin;
    public static Config messages;
    static FileConfiguration config = plugin.getConfig();

    public static void init(JavaPlugin plugin, Config messages) {
        Message.plugin = plugin;
        Message.messages = messages;
    }

    private static String _getPrefix() {
        return _getConfigMessage("prefix");
    }

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

    public static String getConfigMessage(String messageKey) {
        return _getConfigMessage(messageKey);
    }

    private static String _replaceMessageStrings(String message, String[][] replacements) {
        for (String[] replacement : replacements) {
            message = message.replace(replacement[0], replacement[1]);
        }
        return message;
    }

    public static void sendMessage(Object target, String messageKey) {
        Component msg = _getMessage(_getPrefix() + _getConfigMessage(messageKey));
        _sendMessage(target, msg);
    }

    public static void sendMessage(Object target, String message, String[][] replacements) {
        Component msg = _getMessage(_getPrefix() + _replaceMessageStrings(_getConfigMessage(message), replacements));
        _sendMessage(target, msg);
    }

    public static void sendMessageRaw(Object target, String message) {
        Component msg = _getMessage(_getPrefix() + message);
        _sendMessage(target, msg);
    }

    private static void _sendMessage(Object target, Component message) {
        if (target instanceof Player) {
            ((Player) target).sendMessage(message);
        }
        if (target instanceof ConsoleCommandSender) {
            ((ConsoleCommandSender) target).sendMessage(message);
        }
    }

    public static void sendBroadcast(String message, String[][] replacements) {
        String msg = _replaceMessageStrings(_getConfigMessage(message), replacements);
        Bukkit.broadcast(_getMessage(msg));
    }

    public static void sendBroadcast(String message, String[][] replacements, boolean prefix) {
        String msg = _replaceMessageStrings(_getConfigMessage(message), replacements);
        sendBroadcast(msg, prefix);
    }

    public static void sendBroadcast(String message, boolean prefix) {
        if (prefix) {
            Bukkit.broadcast(_getMessage(_getPrefix() + message));
            return;
        }
        Bukkit.broadcast(_getMessage(message));
    }

    public static void sendBroadcast(String message) {
        Bukkit.broadcast(_getMessage(message));
    }

    private static Component _getMessage(String message) {
        // Replace all \n with real newlines
        message = message.replace("\\n", "\n");
        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component getMessage(String message, String[][] replacements) {
        return _getMessage(_replaceMessageStrings(_getConfigMessage(message), replacements));
    }
}
