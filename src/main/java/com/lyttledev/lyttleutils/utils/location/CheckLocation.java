package com.lyttledev.lyttleutils.utils.location;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * CheckLocation is a utility class for checking the safety of a location in a Minecraft world.
 * It provides methods to determine if a location is safe, in the air, or in the ground.
 * It also provides methods to adjust the location to make it safe.
 */
public class CheckLocation {
    /**
     * Get a safe location.
     * This method checks if the location is safe (i.e., not in the air or in a block).
     * If the location is not safe, it will adjust the location to make it safe.
     *
     * @param location The location to check.
     */
    public static void getSafe(Location location) {
        if (isSafe(location)) {return;}
        Material block = location.getBlock().getType();
        if (block != Material.AIR) {inGround(location);}
        if (block == Material.AIR) {inAir(location);}
    }

    /**
     * Check if the location is safe.
     * A location is considered safe if it is not in the air and not in a block.
     *
     * @param location The location to check.
     * @return true if the location is safe, false otherwise.
     */
    public static boolean isSafe(Location location) {
        Block block = location.getBlock();
        Block ground = location.subtract(0, 1,0).getBlock();
        location.add(0, 1, 0);
        return block.getType() == Material.AIR && ground.getType() != Material.AIR;
    }

    /**
     * Check if the location is in the air.
     * A location is considered in the air if it is not in a block.
     *
     * @param location The location to check.
     * @return true if the location is in the air, false otherwise.
     */
    public static void inGround(Location location) {
        Location newLocation = location.add(0, 1, 0);
        getSafe(newLocation);
    }

    /**
     * Check if the location is in the ground.
     * A location is considered in the ground if it is in a block.
     *
     * @param location The location to check.
     * @return true if the location is in the ground, false otherwise.
     */
    public static void inAir(Location location) {
        Location newLocation = location.subtract(0, 1, 0);
        getSafe(newLocation);
    }

}
