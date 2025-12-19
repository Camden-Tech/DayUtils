package me.BaddCamden.DayUtils;

import me.BaddCamden.DayUtils.api.DayUtilsAPI;
import me.BaddCamden.DayUtils.command.DayUtilsCommand;
import me.BaddCamden.DayUtils.config.DaySettings;
import me.BaddCamden.DayUtils.config.DayUtilsConfiguration;
import me.BaddCamden.DayUtils.cycle.DayCycleManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DayUtilsPlugin extends JavaPlugin {
    private DayUtilsConfiguration configurationModel;
    private DayCycleManager cycleManager;
    private DayUtilsAPI api;
    private boolean configDirty;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfigurationModel();

        this.api = DayUtilsAPI.bootstrap(() -> cycleManager);
        DayUtilsCommand command = new DayUtilsCommand(this);
        if (getCommand("dayutils") != null) {
            getCommand("dayutils").setExecutor(command);
            getCommand("dayutils").setTabCompleter(command);
        }
    }

    public void reloadConfigurationModel() {
        persistConfigIfDirty();
        reloadConfig();
        this.configurationModel = DayUtilsConfiguration.fromConfig(getConfig());
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
        markConfigDirty();
    }

    public DayUtilsConfiguration getConfigurationModel() {
        return configurationModel;
    }

    public DayCycleManager getCycleManager() {
        return cycleManager;
    }

    public DayUtilsAPI getApi() {
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

    private void persistConfigIfDirty() {
        if (configDirty) {
            saveConfig();
            configDirty = false;
        }
    }
}
