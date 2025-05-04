package com.lyttledev.lyttleutils.utils.entity;

import net.kyori.adventure.text.TextComponent;

/**
 * Player is a utility class for handling player-related operations in a Minecraft server.
 * It provides methods to retrieve the display name of a player.
 */
public class Player {
    /**
     * Get the display name of a player.
     * This method retrieves the display name of a player as a string.
     *
     * @param player The player whose display name to retrieve.
     * @return The display name of the player.
     */
    public static String getDisplayName(org.bukkit.entity.Player player) { return ((TextComponent) player.displayName()).content(); }
}
