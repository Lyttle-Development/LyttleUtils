package com.lyttledev.lyttleutils.utils.location;

import org.bukkit.Location;

/**
 * BlockLocation is a utility class for comparing two locations in a Minecraft world.
 * It provides a method to check if two locations are in the same block.
 */
public class BlockLocation {
    /**
     * Check if two locations are in the same block.
     * This method compares the world, block x, block y, and block z of the two locations.
     *
     * @param loc1 The first location to compare.
     * @param loc2 The second location to compare.
     * @return true if the two locations are in the same block, false otherwise.
     */
    public static boolean isSameBlockLocation(Location loc1, Location loc2) {
        return loc1.getWorld().equals(loc2.getWorld()) &&
                loc1.getBlockX() == loc2.getBlockX() &&
                loc1.getBlockY() == loc2.getBlockY() &&
                loc1.getBlockZ() == loc2.getBlockZ();
    }
}
