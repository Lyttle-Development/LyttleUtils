package com.lyttledev.lyttleutils.utils.communication;

import com.lyttledev.lyttleutils.types.Message.ReplacementEntry;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import com.lyttledev.lyttleutils.types.YamlConfig;
import com.lyttledev.lyttleutils.utils.convertion.Placeholder;
import com.lyttledev.lyttleutils.utils.storage.GlobalConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Message class to handle sending messages to players and console.
 * It uses MiniMessage for formatting and supports message replacement.
 */
public class Message {
    private static final Pattern LEGACY_FORMATTING_CODE = Pattern.compile(
        "(?i)(?:[§&]#[0-9a-f]{6}|[§&]x(?:[§&][0-9a-f]){6}|[§&][0-9a-fk-or])"
    );

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

    private Component _deserializeMiniMessage(String message) {
        Matcher matcher = LEGACY_FORMATTING_CODE.matcher(message);
        StringBuilder normalized = new StringBuilder(message.length());
        int previousEnd = 0;
        while (matcher.find()) {
            normalized.append(message, previousEnd, matcher.start());
            normalized.append(_legacyFormattingCodeToMiniMessage(matcher.group()));
            previousEnd = matcher.end();
        }
        normalized.append(message, previousEnd, message.length());
        return miniMessage.deserialize(normalized.toString());
    }

    private String _legacyFormattingCodeToMiniMessage(String code) {
        if (code.length() == 8 && code.charAt(1) == '#') {
            return "<#" + code.substring(2) + ">";
        }

        if (code.length() == 14 && Character.toLowerCase(code.charAt(1)) == 'x') {
            StringBuilder hexColor = new StringBuilder(6);
            for (int i = 3; i < code.length(); i += 2) {
                hexColor.append(code.charAt(i));
            }
            return "<#" + hexColor + ">";
        }

        char legacyCode = Character.toLowerCase(code.charAt(code.length() - 1));
        String tag = switch (legacyCode) {
            case '0' -> "black";
            case '1' -> "dark_blue";
            case '2' -> "dark_green";
            case '3' -> "dark_aqua";
            case '4' -> "dark_red";
            case '5' -> "dark_purple";
            case '6' -> "gold";
            case '7' -> "gray";
            case '8' -> "dark_gray";
            case '9' -> "blue";
            case 'a' -> "green";
            case 'b' -> "aqua";
            case 'c' -> "red";
            case 'd' -> "light_purple";
            case 'e' -> "yellow";
            case 'f' -> "white";
            case 'k' -> "obfuscated";
            case 'l' -> "bold";
            case 'm' -> "strikethrough";
            case 'n' -> "underlined";
            case 'o' -> "italic";
            case 'r' -> "reset";
            default -> null;
        };
        if (tag == null) {
            return code;
        }
        return switch (legacyCode) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' ->
                "<reset><" + tag + ">";
            default -> "<" + tag + ">";
        };
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
        return _deserializeMiniMessage(message);
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

        return _deserializeMiniMessage(message);
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

        return _deserializeMiniMessage(messageString);
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

        return _deserializeMiniMessage(messageString);
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

        return _deserializeMiniMessage(messageString);
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
        // Convert Component to string with MiniMessage formatting
        String messageString = miniMessage.serialize(message);

        // Replace all \n with real newlines
        messageString = messageString.replace("\\n", "\n");
        // Remove all backslashes
        messageString = messageString.replace("\\", "");

        // Deserialize back to Component
        Component cleaned = _deserializeMiniMessage(messageString);

        // Support legacy color codes by serializing to legacy, then parsing back
        String legacySerialized = LegacyComponentSerializer.legacyAmpersand().serialize(cleaned);
        // Return as a Component (MiniMessage tags already handled)
        return LegacyComponentSerializer.legacyAmpersand().deserialize(legacySerialized);
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
     * @param messageKey   The message key
     */
    public void sendBroadcast(String messageKey) {
        Bukkit.broadcast(_cleanupMessage(_getMessageFromGlobalConfig(messageKey)));
    }

    /**
     * Send a message to all players on the server with a prefix.
     *
     * @param prefix  Whether to include the prefix or not
     * @param messageKey   The message key
     */
    public void sendBroadcast(boolean prefix, String messageKey) {
        Bukkit.broadcast(_cleanupMessage(_getPrefix(prefix), _getMessageFromGlobalConfig(messageKey)));
    }

    /**
     * Send a message to all players on the server.
     *
     * @param messageKey   The message key
     * @param replacements The replacements to be made in the message
     */
    public void sendBroadcast(String messageKey, Replacements replacements) {
        Component message = _cleanupMessage(_replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), replacements));
        sendBroadcastRaw(message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param messageKey   The message key
     * @param replacements The replacements to be made in the message
     */
    public void sendBroadcast(boolean prefix, String messageKey, Replacements replacements) {
        Component message = _cleanupMessage(_replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), replacements));
        sendBroadcastRaw(prefix, message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param messageKey The message key
     * @param player     The player to apply placeholders for
     */
    public void sendBroadcast(String messageKey, @Nullable Player player) {
        Component message = _cleanupMessage(_replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), player));
        sendBroadcastRaw(message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param messageKey The message key
     * @param player     The player to apply placeholders for
     */
    public void sendBroadcast(boolean prefix, String messageKey, @Nullable Player player) {
        Component message = _cleanupMessage(_replaceMessageStrings(_getMessageFromGlobalConfig(messageKey), player));
        sendBroadcastRaw(prefix, message);
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
        sendBroadcastRaw(message);
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
        sendBroadcastRaw(prefix, message);
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
        sendBroadcastRaw(message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param message      The message key
     * @param replacements The replacements to be made in the message
     */
    public void sendBroadcastRaw(boolean prefix, Component message, Replacements replacements) {
        message = _cleanupMessage(_replaceMessageStrings(message, replacements));
        sendBroadcastRaw(prefix, message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param message The message key
     * @param player  The player to apply placeholders for
     */
    public void sendBroadcastRaw(Component message, @Nullable Player player) {
        message = _cleanupMessage(_replaceMessageStrings(message, player));
        sendBroadcastRaw(message);
    }

    /**
     * Send a message to all players on the server.
     *
     * @param message The message key
     * @param player  The player to apply placeholders for
     */
    public void sendBroadcastRaw(boolean prefix, Component message, @Nullable Player player) {
        message = _cleanupMessage(_replaceMessageStrings(message, player));
        sendBroadcastRaw(prefix, message);
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
        sendBroadcastRaw(message);
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
        sendBroadcastRaw(prefix, message);
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
        return _cleanupMessage(_deserializeMiniMessage(message));
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
        return _cleanupMessage(_replaceMessageStrings(_deserializeMiniMessage(message), replacements));
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
        return _cleanupMessage(_replaceMessageStrings(_deserializeMiniMessage(message), player));
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
        return _cleanupMessage(_replaceMessageStrings(_deserializeMiniMessage(message), replacements, player));
    }
}
