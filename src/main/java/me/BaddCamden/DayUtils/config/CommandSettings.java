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

    public CommandSettings(String reloadPermission, String triggerPermission, String setDayLengthPermission,
                           String setNightLengthPermission, String setSpeedPermission) {
        this.reloadPermission = reloadPermission;
        this.triggerPermission = triggerPermission;
        this.setDayLengthPermission = setDayLengthPermission;
        this.setNightLengthPermission = setNightLengthPermission;
        this.setSpeedPermission = setSpeedPermission;
    }

    public String getReloadPermission() {
        return reloadPermission;
    }

    public String getTriggerPermission() {
        return triggerPermission;
    }

    public String getSetDayLengthPermission() {
        return setDayLengthPermission;
    }

    public String getSetNightLengthPermission() {
        return setNightLengthPermission;
    }

    public String getSetSpeedPermission() {
        return setSpeedPermission;
    }

    public static CommandSettings fromConfig(org.bukkit.configuration.file.FileConfiguration config) {
        String reload = config.getString("commands.reloadPermission", "dayutils.reload");
        String trigger = config.getString("commands.triggerPermission", "dayutils.trigger");
        String setDayLength = config.getString("commands.setDayLengthPermission", "dayutils.setdaylength");
        String setNightLength = config.getString("commands.setNightLengthPermission", "dayutils.setnightlength");
        String setSpeed = config.getString("commands.setSpeedPermission", "dayutils.setspeed");
        return new CommandSettings(reload, trigger, setDayLength, setNightLength, setSpeed);
    }
}
