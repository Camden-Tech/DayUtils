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
        DaySettingsLoader.LoadResult loadResult = loadSettings();
        api = new DayUtilsApiImpl();
        applySettings(loadResult.settings());
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
        DaySettingsLoader.LoadResult loadResult = loadSettings();
        applySettings(loadResult.settings());
    }

    private DaySettingsLoader.LoadResult loadSettings() {
        DaySettingsLoader loader = new DaySettingsLoader(this);
        DaySettingsLoader.LoadResult result = loader.load();
        configModified = configModified || result.modified();
        return result;
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new DayCycleListener(this::getSettings, dayInfoService), this);
    }

    private void registerCommands() {
        DayUtilsCommand commandHandler = new DayUtilsCommand(this, dayInfoService);
        var dayUtilsCommand =
                Objects.requireNonNull(
                        getCommand("dayutils"), "dayutils command must be defined in plugin.yml");

        dayUtilsCommand.setExecutor(commandHandler);
        dayUtilsCommand.setTabCompleter(commandHandler);
    }

    private void initializeScheduler() {
        timeController = new TimeController(this, settings, api);
        timeController.start();
    }

    private void initializeDayInfoService() {
        dayInfoService = new DayInfoServiceImpl(timeController.getCustomDayScheduler(), this::getSettings);
        api.setDayInfoService(dayInfoService);
    }

    private void applySettings(DaySettings newSettings) {
        this.settings = newSettings;
        if (api != null) {
            api.updateConfiguredIntervals(newSettings.customDayTypes());
        }
        if (timeController != null) {
            timeController.updateSettings(newSettings);
        }
    }

    void updateDayLength(long newLength) {
        DaySettings current = getSettings();
        applySettings(
                new DaySettings(
                        newLength,
                        current.nightLengthTicks(),
                        current.speedMultiplier(),
                        current.customDayTypes(),
                        current.reloadPermission(),
                        current.triggerPermission(),
                        current.setDayLengthPermission(),
                        current.setNightLengthPermission(),
                        current.setSpeedPermission()));
        getConfig().set("day.length", newLength);
        markConfigModified();
    }

    void updateNightLength(long newLength) {
        DaySettings current = getSettings();
        applySettings(
                new DaySettings(
                        current.dayLengthTicks(),
                        newLength,
                        current.speedMultiplier(),
                        current.customDayTypes(),
                        current.reloadPermission(),
                        current.triggerPermission(),
                        current.setDayLengthPermission(),
                        current.setNightLengthPermission(),
                        current.setSpeedPermission()));
        getConfig().set("day.nightLength", newLength);
        markConfigModified();
    }

    void updateSpeed(double newSpeed) {
        DaySettings current = getSettings();
        applySettings(
                new DaySettings(
                        current.dayLengthTicks(),
                        current.nightLengthTicks(),
                        newSpeed,
                        current.customDayTypes(),
                        current.reloadPermission(),
                        current.triggerPermission(),
                        current.setDayLengthPermission(),
                        current.setNightLengthPermission(),
                        current.setSpeedPermission()));
        getConfig().set("day.speed", newSpeed);
        markConfigModified();
    }

    void markConfigModified() {
        configModified = true;
    }
}
