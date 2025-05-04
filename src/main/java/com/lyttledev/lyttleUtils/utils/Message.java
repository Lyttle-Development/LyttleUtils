package com.lyttledev.lyttleUtils.utils;

import com.lyttledev.lyttleUtils.LyttleUtils;
import org.bukkit.configuration.file.FileConfiguration;

public class Message {
    public static LyttleUtils plugin;
    static FileConfiguration config = plugin.getConfig();

    public static void init(LyttleUtils plugin) {
        Message.plugin = plugin;
    }
}
