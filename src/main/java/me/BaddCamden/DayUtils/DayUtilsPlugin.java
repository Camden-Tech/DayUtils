package me.BaddCamden.DayUtils;

import org.bukkit.plugin.java.JavaPlugin;

public class DayUtilsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("DayUtils enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("DayUtils disabled");
    }
}
