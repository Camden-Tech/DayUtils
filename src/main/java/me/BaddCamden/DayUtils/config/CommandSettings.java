package me.BaddCamden.DayUtils.config;

/**
 * Stores permission nodes configured for the /dayutils command set.
 */
public class CommandSettings {
    private final String reloadPermission;
    private final String triggerPermission;
    private final String setDayLengthPermission;
    private final String setNightLengthPermission;
    private final String setSpeedPermission;
    private final String setNightsPassedPermission;

    /**
     * Captures all permission nodes required by command handlers.
     */
    public CommandSettings(String reloadPermission, String triggerPermission, String setDayLengthPermission,
                           String setNightLengthPermission, String setSpeedPermission,
                           String setNightsPassedPermission) {
        this.reloadPermission = reloadPermission;
        this.triggerPermission = triggerPermission;
        this.setDayLengthPermission = setDayLengthPermission;
        this.setNightLengthPermission = setNightLengthPermission;
        this.setSpeedPermission = setSpeedPermission;
        this.setNightsPassedPermission = setNightsPassedPermission;
    }

    /**
     * @return permission required to reload configuration
     */
    public String getReloadPermission() {
        return reloadPermission;
    }

    /**
     * @return permission required to trigger custom days
     */
    public String getTriggerPermission() {
        return triggerPermission;
    }

    /**
     * @return permission required to change day length
     */
    public String getSetDayLengthPermission() {
        return setDayLengthPermission;
    }

    /**
     * @return permission required to change night length
     */
    public String getSetNightLengthPermission() {
        return setNightLengthPermission;
    }

    /**
     * @return permission required to alter time speed multiplier
     */
    public String getSetSpeedPermission() {
        return setSpeedPermission;
    }

    /**
     * @return permission required to override nights passed counter
     */
    public String getSetNightsPassedPermission() {
        return setNightsPassedPermission;
    }

    /**
     * Reads permission nodes from the configuration file, applying defaults if missing.
     */
    public static CommandSettings fromConfig(org.bukkit.configuration.file.FileConfiguration config) {
        String reload = config.getString("commands.reloadPermission", "dayutils.reload");
        String trigger = config.getString("commands.triggerPermission", "dayutils.trigger");
        String setDayLength = config.getString("commands.setDayLengthPermission", "dayutils.setdaylength");
        String setNightLength = config.getString("commands.setNightLengthPermission", "dayutils.setnightlength");
        String setSpeed = config.getString("commands.setSpeedPermission", "dayutils.setspeed");
        String setNightsPassed = config.getString("commands.setNightsPassedPermission", "dayutils.setnightspassed");
        return new CommandSettings(reload, trigger, setDayLength, setNightLength, setSpeed, setNightsPassed);
    }
}
