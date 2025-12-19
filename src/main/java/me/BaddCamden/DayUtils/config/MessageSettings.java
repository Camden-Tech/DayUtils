package me.BaddCamden.DayUtils.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Messages shown to players and console.
 */
public class MessageSettings {
    private final String usage;
    private final ReloadMessages reload;
    private final LengthMessages setDayLength;
    private final LengthMessages setNightLength;
    private final SpeedMessages setSpeed;
    private final TriggerMessages trigger;
    private final NightsPassedMessages setNightsPassed;
    private final StatusMessages status;

    public MessageSettings(String usage, ReloadMessages reload, LengthMessages setDayLength,
                           LengthMessages setNightLength, SpeedMessages setSpeed,
                           TriggerMessages trigger, NightsPassedMessages setNightsPassed, StatusMessages status) {
        this.usage = usage;
        this.reload = reload;
        this.setDayLength = setDayLength;
        this.setNightLength = setNightLength;
        this.setSpeed = setSpeed;
        this.trigger = trigger;
        this.setNightsPassed = setNightsPassed;
        this.status = status;
    }

    public String getUsage() {
        return usage;
    }

    public ReloadMessages getReload() {
        return reload;
    }

    public LengthMessages getSetDayLength() {
        return setDayLength;
    }

    public LengthMessages getSetNightLength() {
        return setNightLength;
    }

    public SpeedMessages getSetSpeed() {
        return setSpeed;
    }

    public TriggerMessages getTrigger() {
        return trigger;
    }

    public NightsPassedMessages getSetNightsPassed() {
        return setNightsPassed;
    }

    public StatusMessages getStatus() {
        return status;
    }

    public static MessageSettings fromConfig(FileConfiguration config) {
        String usage = config.getString("messages.usage",
            "&eDayUtils usage: /{label} reload | /{label} trigger <type> [world] | /{label} setdaylength <ticks> | "
                + "/{label} setnightlength <ticks> | /{label} setspeed <0.1-10> | /{label} setnightspassed <count> [world] | /{label} status [world]");
        ReloadMessages reload = new ReloadMessages(
            config.getString("messages.reload.noPermission", "&cYou do not have permission to reload DayUtils."),
            config.getString("messages.reload.success", "&aDayUtils configuration reloaded.")
        );
        LengthMessages setDayLength = new LengthMessages(
            config.getString("messages.setDayLength.noPermission", "&cYou do not have permission."),
            config.getString("messages.setDayLength.usage", "&eUsage: /dayutils setdaylength <ticks>"),
            config.getString("messages.setDayLength.notNumber", "&cDay length must be a number of ticks."),
            config.getString("messages.setDayLength.outOfBounds", "&cDay length must be between {min} and {max} ticks."),
            config.getString("messages.setDayLength.success", "&aDay length updated to {ticks} ticks.")
        );
        LengthMessages setNightLength = new LengthMessages(
            config.getString("messages.setNightLength.noPermission", "&cYou do not have permission."),
            config.getString("messages.setNightLength.usage", "&eUsage: /dayutils setnightlength <ticks>"),
            config.getString("messages.setNightLength.notNumber", "&cNight length must be a number of ticks."),
            config.getString("messages.setNightLength.outOfBounds", "&cNight length must be between {min} and {max} ticks."),
            config.getString("messages.setNightLength.success", "&aNight length updated to {ticks} ticks.")
        );
        SpeedMessages setSpeed = new SpeedMessages(
            config.getString("messages.setSpeed.noPermission", "&cYou do not have permission."),
            config.getString("messages.setSpeed.usage", "&eUsage: /dayutils setspeed <multiplier>"),
            config.getString("messages.setSpeed.notNumber", "&cSpeed must be a decimal value."),
            config.getString("messages.setSpeed.outOfBounds", "&cSpeed multiplier must be between {min} and {max}."),
            config.getString("messages.setSpeed.success", "&aDay/night speed multiplier updated to {multiplier}.")
        );
        TriggerMessages trigger = new TriggerMessages(
            config.getString("messages.trigger.noPermission", "&cYou do not have permission."),
            config.getString("messages.trigger.worldMissing", "&cUnable to resolve world to trigger the event."),
            config.getString("messages.trigger.success", "&aTriggered custom day '{type}' in {world}."),
            config.getString("messages.trigger.unknownType", "&cUnknown custom day '{type}'. Available: {available}")
        );
        NightsPassedMessages setNightsPassed = new NightsPassedMessages(
            config.getString("messages.setNightsPassed.noPermission", "&cYou do not have permission."),
            config.getString("messages.setNightsPassed.usage", "&eUsage: /dayutils setnightspassed <count> [world]"),
            config.getString("messages.setNightsPassed.notNumber", "&cNights passed must be a whole number."),
            config.getString("messages.setNightsPassed.outOfBounds", "&cNights passed cannot be negative."),
            config.getString("messages.setNightsPassed.worldMissing", "&cUnable to resolve world to set nights."),
            config.getString("messages.setNightsPassed.success", "&aSet nights passed in {world} to {count}.")
        );
        StatusMessages status = new StatusMessages(
            config.getString("messages.status.worldMissing", "&cUnable to resolve world for status."),
            config.getString("messages.status.customDay", "&bCustom day {name} progress: {progress} in {world}"),
            config.getString("messages.status.day", "&aDaytime in {world} ({progress} complete)."),
            config.getString("messages.status.night", "&9Nighttime in {world} ({progress} complete)."),
            config.getString("messages.status.nightsPassed", "&7Nights passed in {world}: {count}"),
            config.getString("messages.status.unavailable", "&eCycle status unavailable for {world}")
        );

        return new MessageSettings(usage, reload, setDayLength, setNightLength, setSpeed, trigger, setNightsPassed,
            status);
    }

    public record ReloadMessages(String noPermission, String success) {
    }

    public record LengthMessages(String noPermission, String usage, String notNumber, String outOfBounds,
                                 String success) {
    }

    public record SpeedMessages(String noPermission, String usage, String notNumber, String outOfBounds,
                                String success) {
    }

    public record TriggerMessages(String noPermission, String worldMissing, String success, String unknownType) {
    }

    public record NightsPassedMessages(String noPermission, String usage, String notNumber, String outOfBounds,
                                       String worldMissing, String success) {
    }

    public record StatusMessages(String worldMissing, String customDay, String day, String night, String nightsPassed,
                                 String unavailable) {
    }
}
