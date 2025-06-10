package com.lyttledev.lyttleutils.utils.convertion;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Placeholder {
    static net.william278.papiproxybridge.api.PlaceholderAPI papiproxybridgeApi;

    private static boolean isNativeLoaded() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    private static boolean isProxyBridgeAvailable() {
        boolean isAvailable = Bukkit.getPluginManager().getPlugin("PAPIProxyBridge") != null;

        if (isAvailable && papiproxybridgeApi == null) {
            try {
                papiproxybridgeApi = net.william278.papiproxybridge.api.PlaceholderAPI.createInstance();
            } catch (Exception e) {
                System.err.println("Failed to create PAPIProxyBridge instance: " + e.getMessage());
                isAvailable = false; // fallback to native if proxy bridge fails
            }
        }
        return isAvailable;
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

        // Proxy bridge resolution if available
        if (isProxyBridgeAvailable()) {
            try {
                UUID playerId = (player != null ? player.getUniqueId() : null);
                if (playerId != null) {
                    CompletableFuture<String> future = papiproxybridgeApi.formatPlaceholders(result, playerId);
                    // wait for up to 3 seconds
                    result = future.get(3, TimeUnit.SECONDS);
                } else {
                    // If player is null, log message
                    System.err.println("Player is null, cannot resolve placeholders with PAPIProxyBridge.");
                }
            } catch (Exception e) {
                System.err.println("Failed to resolve placeholders with PAPIProxyBridge: " + e.getMessage());
                // Fallback to original text if there's an error
                return text;
            }
        }

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
