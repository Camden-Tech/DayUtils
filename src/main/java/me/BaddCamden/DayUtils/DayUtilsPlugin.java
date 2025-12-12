package me.BaddCamden.DayUtils;

import java.util.Objects;
import me.BaddCamden.DayUtils.api.DayUtilsApi;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DayUtilsPlugin extends JavaPlugin {
    private DaySettings settings;
    private DayUtilsApiImpl api;
    private TimeController timeController;
    private boolean configModified;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        api = new DayUtilsApiImpl();
        api.updateConfiguredIntervals(settings.customDayTypes());
        registerListeners();
        registerCommands();
        initializeScheduler();
        getLogger().info("DayUtils enabled");
    }

    @Override
    public void onDisable() {
        if (timeController != null) {
            timeController.stop();
        }

        if (configModified) {
            saveConfig();
        }

        getLogger().info("DayUtils disabled");
    }

    public DaySettings getSettings() {
        return settings;
    }

    public DayUtilsApi getApi() {
        return api;
    }

    public void reloadDaySettings() {
        reloadConfig();
        loadSettings();
        api.updateConfiguredIntervals(settings.customDayTypes());
        if (timeController != null) {
            timeController.updateSettings(settings);
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
        timeController = new TimeController(this, settings, api);
        timeController.start();
    }
}
