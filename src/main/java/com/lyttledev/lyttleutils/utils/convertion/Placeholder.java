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
        return Bukkit.getPluginManager().getPlugin("PAPIProxyBridge") != null;
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
                if (papiproxybridgeApi == null) {
                    papiproxybridgeApi = net.william278.papiproxybridge.api.PlaceholderAPI.createInstance();
                }
                UUID playerId = (player != null ? player.getUniqueId() : null);
                CompletableFuture<String> future = papiproxybridgeApi.formatPlaceholders(result, playerId);
                // wait for up to 3 seconds
                result = future.get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                // on timeout or error, keep the current result
                System.err.println("Error resolving placeholders with PAPIProxyBridge: " + e.getMessage());
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
