package me.BaddCamden.DayUtils;

import me.BaddCamden.DayUtils.api.DayUtilsApi;
import me.BaddCamden.DayUtils.command.DayUtilsCommand;
import me.BaddCamden.DayUtils.config.DaySettings;
import me.BaddCamden.DayUtils.config.ConfigLoader;
import me.BaddCamden.DayUtils.config.DayUtilsConfiguration;
import me.BaddCamden.DayUtils.cycle.DayCycleManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class DayUtilsPlugin extends JavaPlugin {
    private ConfigLoader configLoader;
    private DayUtilsConfiguration configurationModel;
    private DayCycleManager cycleManager;
    private DayUtilsApi api;
    private boolean configDirty;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.configLoader = new ConfigLoader(this);
        reloadConfigurationModel();

        this.api = DayUtilsApi.bootstrap(() -> cycleManager);
        DayUtilsCommand command = new DayUtilsCommand(this);
        if (getCommand("dayutils") != null) {
            getCommand("dayutils").setExecutor(command);
            getCommand("dayutils").setTabCompleter(command);
        }
    }

    public void reloadConfigurationModel() {
        persistConfigIfDirty();
        reloadConfig();
        ConfigLoader.LoadResult result = configLoader.load();
        this.configurationModel = result.configuration();
        if (result.modified()) {
            saveConfig();
        }
        restartCycleManager();
    }

    @Override
    public void onDisable() {
        if (this.cycleManager != null) {
            this.cycleManager.stop();
        }
        persistConfigIfDirty();
    }

    public void updateDaySettings(DaySettings daySettings) {
        this.configurationModel = new DayUtilsConfiguration(daySettings, configurationModel.getCommandSettings(),
            configurationModel.getMessageSettings());
        if (this.cycleManager == null) {
            restartCycleManager();
        } else {
            this.cycleManager.updateConfiguration(configurationModel);
        }
        syncConfigWith(daySettings);
        markConfigDirty();
    }

    public DayUtilsConfiguration getConfigurationModel() {
        return configurationModel;
    }

    public DayCycleManager getCycleManager() {
        return cycleManager;
    }

    public DayUtilsApi getApi() {
        return api;
    }

    public void markConfigDirty() {
        this.configDirty = true;
    }

    private void restartCycleManager() {
        if (this.cycleManager != null) {
            this.cycleManager.stop();
        }
        this.cycleManager = new DayCycleManager(this, configurationModel);
        this.cycleManager.start();
    }

    private void syncConfigWith(DaySettings daySettings) {
        getConfig().set("day.length", daySettings.getDayLength());
        getConfig().set("day.nightLength", daySettings.getNightLength());
        getConfig().set("day.speed", daySettings.getSpeedMultiplier());

        getConfig().set("day.customTypes", null);
        ConfigurationSection section = getConfig().createSection("day.customTypes");
        daySettings.getCustomTypes().forEach((key, type) ->
            section.set(type.getName(), type.getIntervalTicks()));
    }

    private void persistConfigIfDirty() {
        if (configDirty) {
            saveConfig();
            configDirty = false;
        }
    }
}
