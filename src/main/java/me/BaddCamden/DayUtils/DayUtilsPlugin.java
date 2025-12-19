package me.BaddCamden.DayUtils;

import me.BaddCamden.DayUtils.api.DayUtilsAPI;
import me.BaddCamden.DayUtils.command.DayUtilsCommand;
import me.BaddCamden.DayUtils.config.DayUtilsConfiguration;
import me.BaddCamden.DayUtils.cycle.DayCycleManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DayUtilsPlugin extends JavaPlugin {
    private DayUtilsConfiguration configurationModel;
    private DayCycleManager cycleManager;
    private DayUtilsAPI api;

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
        reloadConfig();
        this.configurationModel = DayUtilsConfiguration.fromConfig(getConfig());
        if (this.cycleManager == null) {
            this.cycleManager = new DayCycleManager(this, configurationModel);
            this.cycleManager.start();
        } else {
            this.cycleManager.updateConfiguration(configurationModel);
        }
    }

    @Override
    public void onDisable() {
        if (this.cycleManager != null) {
            this.cycleManager.stop();
        }
        saveConfig();
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
}
