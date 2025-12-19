package me.BaddCamden.DayUtils.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Aggregated configuration model loaded once and cached.
 */
public class DayUtilsConfiguration {
    private final DaySettings daySettings;
    private final CommandSettings commandSettings;
    private final MessageSettings messageSettings;

    public DayUtilsConfiguration(DaySettings daySettings, CommandSettings commandSettings,
                                 MessageSettings messageSettings) {
        this.daySettings = daySettings;
        this.commandSettings = commandSettings;
        this.messageSettings = messageSettings;
    }

    public DaySettings getDaySettings() {
        return daySettings;
    }

    public CommandSettings getCommandSettings() {
        return commandSettings;
    }

    public MessageSettings getMessageSettings() {
        return messageSettings;
    }

    public static DayUtilsConfiguration fromConfig(FileConfiguration config) {
        return new DayUtilsConfiguration(
            DaySettings.fromConfig(config),
            CommandSettings.fromConfig(config),
            MessageSettings.fromConfig(config)
        );
    }
}
