package com.lyttledev.lyttleutils.utils.gameplay;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class ActionBar {

    private static final HashMap<Player, BukkitTask> activeActionBars = new HashMap<>();

    public static void setActionBar(boolean active, Player player, Component message, JavaPlugin plugin) {
        BukkitTask oldActionBarTask = activeActionBars.get(player);
        // If there is an old action bar task, cancel it before overwriting it
        if (oldActionBarTask != null) {
            oldActionBarTask.cancel();
            activeActionBars.remove(player);
        }

        if (active) {
            BukkitTask newActionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                player.sendActionBar(message);
            }, 0, 40);
            activeActionBars.put(player, newActionBarTask);
        }
    }
}
