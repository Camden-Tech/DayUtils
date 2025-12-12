package me.BaddCamden.DayUtils;

import java.util.Objects;
import me.BaddCamden.DayUtils.api.DayInfoService;
import me.BaddCamden.DayUtils.api.DayUtilsApi;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DayUtilsPlugin extends JavaPlugin {
    private DaySettings settings;
    private DayUtilsApiImpl api;
    private TimeController timeController;
    private DayInfoService dayInfoService;
    private boolean configModified;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        api = new DayUtilsApiImpl();
        api.updateConfiguredIntervals(settings.customDayTypes());
        initializeScheduler();
        initializeDayInfoService();
        registerListeners();
        registerCommands();
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

    public DayInfoService getDayInfoService() {
        return dayInfoService;
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
        pluginManager.registerEvents(new DayCycleListener(this::getSettings, dayInfoService), this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("dayutils"), "dayutils command must be defined in plugin.yml")
                .setExecutor(new DayUtilsCommand(this, dayInfoService));
    }

    private void initializeScheduler() {
        timeController = new TimeController(this, settings, api);
        timeController.start();
    }

    private void initializeDayInfoService() {
        dayInfoService = new DayInfoServiceImpl(timeController.getCustomDayScheduler(), this::getSettings);
        api.setDayInfoService(dayInfoService);
    }
}
