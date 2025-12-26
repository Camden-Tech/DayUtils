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

    /**
     * Boots the plugin by loading configuration, wiring the API, and registering commands.
     */
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

    /**
     * Reloads configuration from disk, restarting the cycle manager when values change.
     */
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

    /**
     * Stops active tasks and flushes pending configuration changes when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        if (this.cycleManager != null) {
            this.cycleManager.stop();
        }
        persistConfigIfDirty();
    }

    /**
     * Applies updated day settings to the running plugin and marks the config for persistence.
     *
     * @param daySettings the new day settings to apply
     */
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

    /**
     * Returns the cached configuration model representing the current settings.
     *
     * @return the in-memory configuration
     */
    public DayUtilsConfiguration getConfigurationModel() {
        return configurationModel;
    }

    /**
     * Provides access to the running cycle manager instance.
     *
     * @return the cycle manager controlling world time
     */
    public DayCycleManager getCycleManager() {
        return cycleManager;
    }

    /**
     * Exposes the API wrapper for other plugins.
     *
     * @return a DayUtilsApi instance
     */
    public DayUtilsApi getApi() {
        return api;
    }

    /**
     * Marks the configuration as modified so it will be written to disk at the next opportunity.
     */
    public void markConfigDirty() {
        this.configDirty = true;
    }

    /**
     * Stops any running cycle manager and starts a new one using the current configuration model.
     */
    private void restartCycleManager() {
        if (this.cycleManager != null) {
            this.cycleManager.stop();
        }
        this.cycleManager = new DayCycleManager(this, configurationModel);
        this.cycleManager.start();
    }

    /**
     * Synchronises the Bukkit configuration object with the provided day settings.
     *
     * @param daySettings the settings to write back to config.yml
     */
    private void syncConfigWith(DaySettings daySettings) {
        getConfig().set("day.length", daySettings.getDayLength());
        getConfig().set("day.nightLength", daySettings.getNightLength());
        getConfig().set("day.speed", daySettings.getSpeedMultiplier());

        getConfig().set("day.customTypes", null);
        ConfigurationSection section = getConfig().createSection("day.customTypes");
        daySettings.getCustomTypes().forEach((key, type) ->
            section.set(type.getName(), type.getIntervalTicks()));
    }

    /**
     * Saves the configuration to disk if any writes have been queued.
     */
    private void persistConfigIfDirty() {
        if (configDirty) {
            saveConfig();
            configDirty = false;
        }
    }
}
