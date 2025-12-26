package me.BaddCamden.DayUtils.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Aggregated configuration model loaded once and cached.
 */
public class DayUtilsConfiguration {
    private final DaySettings daySettings;
    private final CommandSettings commandSettings;
    private final MessageSettings messageSettings;

    /**
     * Combines settings sections into a single immutable configuration model.
     */
    public DayUtilsConfiguration(DaySettings daySettings, CommandSettings commandSettings,
                                 MessageSettings messageSettings) {
        this.daySettings = daySettings;
        this.commandSettings = commandSettings;
        this.messageSettings = messageSettings;
    }

    /**
     * @return the configured day settings
     */
    public DaySettings getDaySettings() {
        return daySettings;
    }

    /**
     * @return command permission settings
     */
    public CommandSettings getCommandSettings() {
        return commandSettings;
    }

    /**
     * @return templated plugin messages
     */
    public MessageSettings getMessageSettings() {
        return messageSettings;
    }

    /**
     * Loads a configuration model from the provided Bukkit configuration snapshot.
     */
    public static DayUtilsConfiguration fromConfig(FileConfiguration config) {
        return new DayUtilsConfiguration(
            DaySettings.fromConfig(config),
            CommandSettings.fromConfig(config),
            MessageSettings.fromConfig(config)
        );
    }
}
