package com.lyttledev.lyttleutils.utils.convertion;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Placeholder {
    private static boolean isNativeLoaded() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    /**
     * Parse placeholders by applying both native and proxy bridge in sequence.
     * - First: resolve with native PlaceholderAPI if present.
     * - Then: resolve with PAPIProxyBridge if available.
     *
     * @param player the player context (maybe null)
     * @param text   input text with placeholders
     * @return resolved text, or original on failure
     */
    public static String parsePlaceholders(Player player, String text) {
        String result = text;

        // Native resolution if available
        if (isNativeLoaded()) {
            result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
        }

        return result;
    }

    // Convenience overloads
    public static String parsePlaceholders(String text) {
        return parsePlaceholders(null, text);
    }

    public static String parsePlaceholder(Player player, String text) {
        return parsePlaceholders(player, text);
    }

    public static String parsePlaceholder(String text) {
        return parsePlaceholders(null, text);
    }
}
