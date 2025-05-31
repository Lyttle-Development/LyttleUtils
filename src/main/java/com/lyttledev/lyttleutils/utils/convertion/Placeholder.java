package com.lyttledev.lyttleutils.utils.convertion;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getServer;

public class Placeholder {
    private static boolean isLoaded() {
        return getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public static String parsePlaceholders(Player player, String text) {
        if (!isLoaded()) {
            // PlaceholderAPI is not loaded, return the original text
            return text;
        }

        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public static String parsePlaceholders(String text) {
        return parsePlaceholders(null, text);
    }

    public static String parsePlaceholder(Player player, String text) {
        return parsePlaceholders(player, text);
    }

    public static String parsePlaceholder(String text) {
        return parsePlaceholder(null, text);
    }
}
