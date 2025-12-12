package me.BaddCamden.DayUtils;

import java.util.Objects;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DayUtilsPlugin extends JavaPlugin {
    private DaySettings settings;
    private DayScheduler scheduler;
    private boolean configModified;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        registerListeners();
        registerCommands();
        initializeScheduler();
        getLogger().info("DayUtils enabled");
    }

    @Override
    public void onDisable() {
        if (scheduler != null) {
            scheduler.stop();
        }

        if (configModified) {
            saveConfig();
        }

        getLogger().info("DayUtils disabled");
    }

    public DaySettings getSettings() {
        return settings;
    }

    public void reloadDaySettings() {
        reloadConfig();
        loadSettings();
        if (scheduler != null) {
            scheduler.updateSettings(settings);
        }
    }

    private void loadSettings() {
        DaySettingsLoader loader = new DaySettingsLoader(this);
        DaySettingsLoader.LoadResult result = loader.load();
        this.settings = result.settings();
        configModified = configModified || result.modified();
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new DayCycleListener(this::getSettings), this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("dayutils"), "dayutils command must be defined in plugin.yml")
                .setExecutor(new DayUtilsCommand(this));
    }

    private void initializeScheduler() {
        scheduler = new DayScheduler(this, settings);
        scheduler.start();
    }
}
