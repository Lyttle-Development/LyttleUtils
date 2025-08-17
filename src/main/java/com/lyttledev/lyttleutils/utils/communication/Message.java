package com.lyttledev.lyttleutils.utils.communication;

import com.lyttledev.lyttleutils.types.Message.ReplacementEntry;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import com.lyttledev.lyttleutils.types.YamlConfig;
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
    private final YamlConfig messages;
    private final GlobalConfig global;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Initialize the Message class with the plugin instance and messages configuration.
     *
     * @param plugin   The JavaPlugin instance
     * @param messages The YamlConfig instance for messages
     */
    public Message(JavaPlugin plugin, YamlConfig messages, GlobalConfig global) {
        this.plugin = plugin;
        this.messages = messages;
        this.global = global;
        this.console = new Console(plugin);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTIL: Get Prefix
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get the prefix from the messages configuration.
     *
     * @return The prefix string
     */
    private Component _getPrefix() {
        return _getMessageFromGlobalConfig("prefix");
    }

    /**
     * Get the prefix from the messages configuration based on the enabled state.
     *
     * @param enabled
     * @return
     */
    private Component _getPrefix(Boolean enabled) {
        return enabled ? _getMessageFromGlobalConfig("prefix") : Component.empty();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTIL: Get Message from Global YamlConfig
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTIL: Replace Message Strings
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
     * Get a message from the messages configuration with the prefix.
     *
     * @param message      The message string
     * @param replacements The replacements to be made in the message
     * @return The formatted message string
     */
    private Component _replaceMessageStrings(Component message, Replacements replacements) {
        String messageString = miniMessage.serialize(message);
        for (ReplacementEntry replacement : replacements.getAll()) {
            messageString = messageString.replace((CharSequence) replacement.getKey(), replacement.getValue());
        }

        // Replace all PlaceholderAPI placeholders
        messageString = Placeholder.parsePlaceholders(null, messageString);

        return miniMessage.deserialize(messageString);
    }

    /**
     * Get a message from the messages configuration with the prefix.
     *
     * @param message The message string
     * @param player  The player to apply placeholders for
     * @return The formatted message string
     */
    private Component _replaceMessageStrings(Component message, @Nullable Player player) {
        String messageString = miniMessage.serialize(message);

        // Replace all PlaceholderAPI placeholders
        messageString = Placeholder.parsePlaceholders(player, messageString);

        return miniMessage.deserialize(messageString);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTIL: Send Message
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTIL: Combine Components

    /// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Component _combineComponents(Component... components) {
        Component combined = Component.empty();
        for (Component component : components) {
            combined = combined.append(component);
        }
        return combined;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTIL: Cleanup Message
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Send Message
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
     * Send a message to a target (Player or ConsoleCommandSender).
     *
     * @param target     The target to send the message to
     * @param messageKey The key for the message
     */
    public void sendMessage(boolean prefix, Object target, String messageKey) {
        Component msg = _cleanupMessage(_getPrefix(prefix), _getMessageFromGlobalConfig(messageKey));
        _sendMessage(target, msg);
    }

    /**
     * Send a message to a target (Player or ConsoleCommandSender) with replacements.
     *
     * @param target       The target to send the message to
     * @param messageKey   The message string
     * @param replacements The replacements to be made in the message
     */
    public void sendMessage(Object target, String messageKey, Replacements replacements) {
        Component msg = _cleanupMessage(_getPrefix(), _replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), replacements));
        _sendMessage(target, msg);
    }

    /**
     * Send a message to a target (Player or ConsoleCommandSender) with replacements.
     *
     * @param target       The target to send the message to
     * @param messageKey   The message string
     * @param replacements The replacements to be made in the message
     */
    public void sendMessage(boolean prefix, Object target, String messageKey, Replacements replacements) {
        Component msg = _cleanupMessage(_getPrefix(prefix), _replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), replacements));
        _sendMessage(target, msg);
    }

    /**
     * Send a message to a target (Player or ConsoleCommandSender) with replacements.
     *
     * @param target     The target to send the message to
     * @param messageKey The message string
     * @param player     The player to apply placeholders for
     */
    public void sendMessage(Object target, String messageKey, @Nullable Player player) {
        Component msg = _cleanupMessage(_getPrefix(), _replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), player));
        _sendMessage(target, msg);
    }

    /**
     * Send a message to a target (Player or ConsoleCommandSender) with replacements.
     *
     * @param target     The target to send the message to
     * @param messageKey The message string
     * @param player     The player to apply placeholders for
     */
    public void sendMessage(boolean prefix, Object target, String messageKey, @Nullable Player player) {
        Component msg = _cleanupMessage(_getPrefix(prefix), _replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), player));
        _sendMessage(target, msg);
    }

    /**
     * Send a message to a target (Player or ConsoleCommandSender) with replacements.
     *
     * @param target     The target to send the message to
     * @param messageKey The message string
     * @param player     The player to apply placeholders for
     */
    public void sendMessage(Object target, String messageKey, Replacements replacements, @Nullable Player player) {
        Component msg = _cleanupMessage(_getPrefix(), _replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), replacements, player));
        _sendMessage(target, msg);
    }

    /**
     * Send a message to a target (Player or ConsoleCommandSender) with replacements.
     *
     * @param target     The target to send the message to
     * @param messageKey The message string
     * @param player     The player to apply placeholders for
     */
    public void sendMessage(boolean prefix, Object target, String messageKey, Replacements replacements, @Nullable Player player) {
        Component msg = _cleanupMessage(_getPrefix(prefix), _replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), replacements, player));
        _sendMessage(target, msg);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Send Raw Message
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
     * Send a raw message to a target (Player or ConsoleCommandSender).
     *
     * @param target  The target to send the message to
     * @param message The message string
     */
    public void sendMessageRaw(boolean prefix, Object target, Component message) {
        Component msg = _cleanupMessage(_getPrefix(prefix), message);
        _sendMessage(target, msg);
    }

    /**
     * Send a raw message to a target (Player or ConsoleCommandSender).
     *
     * @param target  The target to send the message to
     * @param message The message string
     */
    public void sendMessageRaw(Object target, Component message, Replacements replacements) {
        Component msg = _cleanupMessage(_getPrefix(), _replaceMessageStrings(message, replacements));
        _sendMessage(target, msg);
    }

    /**
     * Send a raw message to a target (Player or ConsoleCommandSender).
     *
     * @param target  The target to send the message to
     * @param message The message string
     */
    public void sendMessageRaw(boolean prefix, Object target, Component message, Replacements replacements) {
        Component msg = _cleanupMessage(_getPrefix(prefix), _replaceMessageStrings(message, replacements));
        _sendMessage(target, msg);
    }

    /**
     * Send a raw message to a target (Player or ConsoleCommandSender).
     *
     * @param target  The target to send the message to
     * @param message The message string
     */
    public void sendMessageRaw(Object target, Component message, @Nullable Player player) {
        Component msg = _cleanupMessage(_getPrefix(), _replaceMessageStrings(message, player));
        _sendMessage(target, msg);
    }

    /**
     * Send a raw message to a target (Player or ConsoleCommandSender).
     *
     * @param target  The target to send the message to
     * @param message The message string
     */
    public void sendMessageRaw(boolean prefix, Object target, Component message, @Nullable Player player) {
        Component msg = _cleanupMessage(_getPrefix(prefix), _replaceMessageStrings(message, player));
        _sendMessage(target, msg);
    }

    /**
     * Send a raw message to a target (Player or ConsoleCommandSender).
     *
     * @param target  The target to send the message to
     * @param message The message string
     */
    public void sendMessageRaw(Object target, Component message, Replacements replacements, @Nullable Player player) {
        Component msg = _cleanupMessage(_getPrefix(), _replaceMessageStrings(message, replacements, player));
        _sendMessage(target, msg);
    }

    /**
     * Send a raw message to a target (Player or ConsoleCommandSender).
     *
     * @param target  The target to send the message to
     * @param message The message string
     */
    public void sendMessageRaw(boolean prefix, Object target, Component message, Replacements replacements, @Nullable Player player) {
        Component msg = _cleanupMessage(_getPrefix(prefix), _replaceMessageStrings(message, replacements, player));
        _sendMessage(target, msg);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Send Broadcast
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Send a message to all players on the server without a prefix.
     *
     * @param message The message string
     */
    public void sendBroadcast(Component message) {
        Bukkit.broadcast(_cleanupMessage(message));
    }

    /**
     * Send a message to all players on the server with a prefix.
     *
     * @param prefix  Whether to include the prefix or not
     * @param message The message string
     */
    public void sendBroadcast(boolean prefix, Component message) {
        Bukkit.broadcast(_cleanupMessage(_getPrefix(prefix), message));
    }

    /**
     * Send a message to all players on the server.
     *
     * @param messageKey   The message key
     * @param replacements The replacements to be made in the message
     */
    public void sendBroadcast(String messageKey, Replacements replacements) {
        Component message = _cleanupMessage(_replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), replacements));
        sendBroadcast(message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param messageKey   The message key
     * @param replacements The replacements to be made in the message
     */
    public void sendBroadcast(boolean prefix, String messageKey, Replacements replacements) {
        Component message = _cleanupMessage(_replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), replacements));
        sendBroadcast(prefix, message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param messageKey The message key
     * @param player     The player to apply placeholders for
     */
    public void sendBroadcast(String messageKey, @Nullable Player player) {
        Component message = _cleanupMessage(_replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), player));
        sendBroadcast(message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param messageKey The message key
     * @param player     The player to apply placeholders for
     */
    public void sendBroadcast(boolean prefix, String messageKey, @Nullable Player player) {
        Component message = _cleanupMessage(_replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), player));
        sendBroadcast(prefix, message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param messageKey   The message key
     * @param replacements The replacements to be made in the message
     * @param player       The player to apply placeholders for
     */
    public void sendBroadcast(String messageKey, Replacements replacements, @Nullable Player player) {
        Component message = _cleanupMessage(_replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), replacements, player));
        sendBroadcast(message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param messageKey   The message key
     * @param replacements The replacements to be made in the message
     * @param player       The player to apply placeholders for
     */
    public void sendBroadcast(boolean prefix, String messageKey, Replacements replacements, @Nullable Player player) {
        Component message = _cleanupMessage(_replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), replacements, player));
        sendBroadcast(prefix, message);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Send Raw Broadcast
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Send a message to all players on the server without a prefix.
     *
     * @param message The message string
     */
    public void sendBroadcastRaw(Component message) {
        Bukkit.broadcast(_cleanupMessage(message));
    }

    /**
     * Send a message to all players on the server with a prefix.
     *
     * @param prefix  Whether to include the prefix or not
     * @param message The message string
     */
    public void sendBroadcastRaw(boolean prefix, Component message) {
        Bukkit.broadcast(_cleanupMessage(_getPrefix(prefix), message));
    }

    /**
     * Send a message to all players on the server.
     *
     * @param message      The message key
     * @param replacements The replacements to be made in the message
     */
    public void sendBroadcastRaw(Component message, Replacements replacements) {
        message = _cleanupMessage(_replaceMessageStrings(message, replacements));
        sendBroadcast(message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param message      The message key
     * @param replacements The replacements to be made in the message
     */
    public void sendBroadcastRaw(boolean prefix, Component message, Replacements replacements) {
        message = _cleanupMessage(_replaceMessageStrings(message, replacements));
        sendBroadcast(prefix, message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param message The message key
     * @param player  The player to apply placeholders for
     */
    public void sendBroadcastRaw(Component message, @Nullable Player player) {
        message = _cleanupMessage(_replaceMessageStrings(message, player));
        sendBroadcast(message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param message The message key
     * @param player  The player to apply placeholders for
     */
    public void sendBroadcastRaw(boolean prefix, Component message, @Nullable Player player) {
        message = _cleanupMessage(_replaceMessageStrings(message, player));
        sendBroadcast(prefix, message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param message      The message key
     * @param replacements The replacements to be made in the message
     * @param player       The player to apply placeholders for
     */
    public void sendBroadcastRaw(Component message, Replacements replacements, @Nullable Player player) {
        message = _cleanupMessage(_replaceMessageStrings(message, replacements, player));
        sendBroadcast(message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param message      The message
     * @param replacements The replacements to be made in the message
     * @param player       The player to apply placeholders for
     */
    public void sendBroadcastRaw(boolean prefix, Component message, Replacements replacements, @Nullable Player player) {
        message = _cleanupMessage(_replaceMessageStrings(message, replacements, player));
        sendBroadcast(prefix, message);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Get YamlConfig Message
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
     * @return The formatted message string
     */
    public Component getMessage(String messageKey, Replacements replacements) {
        return _cleanupMessage(_replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), replacements));
    }

    /**
     * Get a message from the messages configuration with replacements.
     *
     * @param messageKey The message key
     * @param player     The replacements to be made in the message
     * @return The formatted message string
     */
    public Component getMessage(String messageKey, @Nullable Player player) {
        return _cleanupMessage(_replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), player));
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


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Get YamlConfig Message (RAW)
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get a message from the messages configuration without the prefix.
     *
     * @param message The key for the message
     * @return The message string
     */
    public Component getMessageRaw(Component message) {
        return _cleanupMessage(message);
    }

    /**
     * Get a message from the messages configuration without the prefix.
     *
     * @param message The key for the message
     * @return The message string
     */
    public Component getMessageRaw(String message) {
        return _cleanupMessage(miniMessage.deserialize(message));
    }

    /**
     * Get a message from the messages configuration with replacements.
     *
     * @param message      The key for the message
     * @param replacements The replacements to be made in the message
     * @return The message string
     */
    public Component getMessageRaw(Component message, Replacements replacements) {
        return _cleanupMessage(_replaceMessageStrings(message, replacements));
    }

    /**
     * Get a message from the messages configuration with replacements.
     *
     * @param message      The key for the message
     * @param replacements The replacements to be made in the message
     * @return The message string
     */
    public Component getMessageRaw(String message, Replacements replacements) {
        return _cleanupMessage(_replaceMessageStrings(miniMessage.deserialize(message), replacements));
    }

    /**
     * Get a message from the messages configuration with replacements.
     *
     * @param message The key for the message
     * @param player  The player to apply placeholders for
     * @return The message string
     */
    public Component getMessageRaw(Component message, @Nullable Player player) {
        return _cleanupMessage(_replaceMessageStrings(message, player));
    }

    /**
     * Get a message from the messages configuration with replacements.
     *
     * @param message The key for the message
     * @param player  The player to apply placeholders for
     * @return The message string
     */
    public Component getMessageRaw(String message, @Nullable Player player) {
        return _cleanupMessage(_replaceMessageStrings(miniMessage.deserialize(message), player));
    }

    /**
     * Get a message from the messages configuration with replacements.
     *
     * @param message      The key for the message
     * @param replacements The replacements to be made in the message
     * @param player       The player to apply placeholders for
     * @return The message string
     */
    public Component getMessageRaw(Component message, Replacements replacements, @Nullable Player player) {
        return _cleanupMessage(_replaceMessageStrings(message, replacements, player));
    }

    /**
     * Get a message from the messages configuration with replacements.
     *
     * @param message      The key for the message
     * @param replacements The replacements to be made in the message
     * @param player       The player to apply placeholders for
     * @return The message string
     */
    public Component getMessageRaw(String message, Replacements replacements, @Nullable Player player) {
        return _cleanupMessage(_replaceMessageStrings(miniMessage.deserialize(message), replacements, player));
    }
}
