package com.lyttledev.lyttleutils.utils.communication;

import com.lyttledev.lyttleutils.types.Config;
import com.lyttledev.lyttleutils.types.Message.ReplacementEntry;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import com.lyttledev.lyttleutils.utils.convertion.Placeholder;
import com.lyttledev.lyttleutils.utils.storage.GlobalConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

/**
 * Message class to handle sending messages to players and console.
 * It uses MiniMessage for formatting and supports message replacement.
 */
public class Message {
    private final JavaPlugin plugin;
    private final Console console;
    private final Config messages;
    private final GlobalConfig global;
    private MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Initialize the Message class with the plugin instance and messages configuration.
     *
     * @param plugin   The JavaPlugin instance
     * @param messages The Config instance for messages
     */
    public Message(JavaPlugin plugin, Config messages, GlobalConfig global) {
        this.plugin = plugin;
        this.messages = messages;
        this.global = global;
        this.console = new Console(plugin);
    }

    /**
     * Get the prefix from the messages configuration.
     *
     * @return The prefix string
     */
    private Component _getPrefix() {
        return _getMessageFromGlobalConfig("prefix");
    }

    /**
     * Get a message from the messages configuration.
     *
     * @param messageKey The key for the message
     * @return The message string
     */
    private Component _getMessageFromGlobalConfig(String messageKey) {
        // Check if the global config is enabled
        if (!global.get("enabled").equalsIgnoreCase("true")) {
            // If the global config is not enabled, fallback to messages.yml
            return _getMessageFromMessagesConfig(messageKey);
        }

        @Nullable String message = global.get(messageKey);

        if (message == null) {
            // If the message is not found in the global config, fallback to messages.yml
            return _getMessageFromMessagesConfig(messageKey);
        }

        // If the message is found in the global config, deserialize it using MiniMessage
        return miniMessage.deserialize(message);
    }

    private Component _getMessageFromMessagesConfig(String messageKey) {
        @Nullable String message = (String) messages.get(messageKey);
        if (message == null) {
            console.log("Message key " + messageKey + " not found in messages.yml");
            message = (String) messages.get("message_not_found");
        }

        if (message == null) {
            console.log("Even the message_not_found not found in messages.yml...");
            message = "&cOh... I can't react to that. (Contact the Administrators)";
        }

        return miniMessage.deserialize(message);
    }

    /**
     * Get a message from the messages configuration without the prefix.
     *
     * @param messageKey The key for the message
     * @return The message string
     */
    public Component getConfigMessage(String messageKey) {
        return _getMessageFromGlobalConfig(messageKey);
    }

    /**
     * Get a message from the messages configuration with the prefix.
     *
     * @param message      The message string
     * @param replacements The replacements to be made in the message
     * @param player       The player to apply placeholders for
     * @return The formatted message string
     */
    private Component _replaceMessageStrings(Component message, Replacements replacements, @Nullable Player player) {
        String messageString = miniMessage.serialize(message);
        for (ReplacementEntry replacement : replacements.getAll()) {
            messageString = messageString.replace((CharSequence) replacement.getKey(), replacement.getValue());
        }

        // Replace all PlaceholderAPI placeholders
        messageString = Placeholder.parsePlaceholders(player, messageString);

        return miniMessage.deserialize(messageString);
    }

    /**
     * Send a message to a target (Player or ConsoleCommandSender).
     *
     * @param target     The target to send the message to
     * @param messageKey The key for the message
     */
    public void sendMessage(Object target, String messageKey) {
        Component msg = _cleanupMessage(_getPrefix(), _getMessageFromGlobalConfig(messageKey));
        _sendMessage(target, msg);
    }

    /**
     * Send a message to a target (Player or ConsoleCommandSender) with replacements.
     *
     * @param target       The target to send the message to
     * @param message      The message string
     * @param replacements The replacements to be made in the message
     * @param player       The player to apply placeholders for
     */
    public void sendMessage(Object target, String message, Replacements replacements, @Nullable Player player) {
        Component msg = _cleanupMessage(_getPrefix(), _replaceMessageStrings(_getMessageFromGlobalConfig(message), replacements, player));
        _sendMessage(target, msg);
    }

    /**
     * Send a message to a target (Player or ConsoleCommandSender) with replacements.
     *
     * @param target       The target to send the message to
     * @param message      The message string
     * @param replacements The replacements to be made in the message
     */
    public void sendMessage(Object target, String message, Replacements replacements) {
        sendMessage(target, message, replacements, null);
    }

    /**
     * Send a raw message to a target (Player or ConsoleCommandSender).
     *
     * @param target  The target to send the message to
     * @param message The message string
     */
    public void sendMessageRaw(Object target, Component message) {
        Component msg = _cleanupMessage(_getPrefix(), message);
        _sendMessage(target, msg);
    }

    /**
     * Send a raw message to a target (Player or ConsoleCommandSender) without the prefix.
     *
     * @param target  The target to send the message to
     * @param message The message string
     */
    private void _sendMessage(Object target, Component message) {
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
     * @param messageKey   The message key
     * @param replacements The replacements to be made in the message
     * @param player       The player to apply placeholders for
     */
    public void sendBroadcast(String messageKey, Replacements replacements, @Nullable Player player) {
        Component msg = _replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), replacements, player);
        Bukkit.broadcast(_cleanupMessage(msg));
    }

    /**
     * Send a message to all players on the server.
     *
     * @param messageKey   The message key
     * @param replacements The replacements to be made in the message
     */
    public void sendBroadcast(String messageKey, Replacements replacements) {
        sendBroadcast(messageKey, replacements, null);
    }

    /**
     * Send a message to all players on the server with a prefix.
     *
     * @param messageKey   The message key
     * @param replacements The replacements to be made in the message
     * @param player       The player to apply placeholders for
     * @param prefix       Whether to include the prefix or not
     */
    public void sendBroadcast(String messageKey, Replacements replacements, boolean prefix, @Nullable Player player) {
        Component message = _replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), replacements, player);
        sendBroadcast(message, prefix);
    }


    /**
     * Send a message to all players on the server with a prefix.
     *
     * @param messageKey   The message key
     * @param replacements The replacements to be made in the message
     * @param prefix       Whether to include the prefix or not
     */
    public void sendBroadcast(String messageKey, Replacements replacements, boolean prefix) {
        sendBroadcast(messageKey, replacements, prefix, null);
    }

    /**
     * Send a message to all players on the server with a prefix.
     *
     * @param message The message string
     * @param prefix  Whether to include the prefix or not
     */
    public void sendBroadcast(Component message, boolean prefix) {
        if (prefix) {
            Bukkit.broadcast(_cleanupMessage(_getPrefix(), message));
            return;
        }
        Bukkit.broadcast(_cleanupMessage(message));
    }

    /**
     * Send a message to all players on the server without a prefix.
     *
     * @param message The message string
     */
    public void sendBroadcast(Component message) {
        Bukkit.broadcast(_cleanupMessage(message));
    }

    /**
     * Get a message from the messages configuration with the prefix.
     *
     * @param message The message string
     * @return The formatted message string
     */
    private Component _cleanupMessage(Component message) {
        String messageString = miniMessage.serialize(message);

        // Replace all \n with real newlines
        messageString = messageString.replace("\\n", "\n");
        // Remove all backslashes
        messageString = messageString.replace("\\", "");

        return miniMessage.deserialize(messageString);
    }

    /**
     * Get a message from the messages configuration with the prefix.
     *
     * @param components The message string
     * @return The formatted message string
     */
    private Component _cleanupMessage(Component... components) {
        Component combined = _combineComponents(components);
        return _cleanupMessage(combined);
    }

    private Component _combineComponents(Component... components) {
        Component combined = Component.empty();
        for (Component component : components) {
            combined = combined.append(component);
        }
        return combined;
    }

    /**
     * Get a message from the messages configuration without the prefix.
     *
     * @param messageKey The message key
     * @return The formatted message string
     */
    public Component getMessage(String messageKey) {
        return _cleanupMessage(_getMessageFromGlobalConfig(messageKey));
    }

    /**
     * Get a message from the messages configuration with replacements.
     *
     * @param messageKey   The message key
     * @param replacements The replacements to be made in the message
     * @param player       The player to apply placeholders for
     * @return The formatted message string
     */
    public Component getMessage(String messageKey, Replacements replacements, @Nullable Player player) {
        return _cleanupMessage(_replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), replacements, player));
    }

    /**
     * Get a message from the messages configuration with replacements.
     *
     * @param messageKey   The message key
     * @param replacements The replacements to be made in the message
     * @return The formatted message string
     */
    public Component getMessage(String messageKey, Replacements replacements) {
        return getMessage(messageKey, replacements, null);
    }
}
