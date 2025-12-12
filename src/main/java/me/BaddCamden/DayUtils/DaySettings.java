package me.BaddCamden.DayUtils;

import java.util.Collections;
import java.util.Map;

public final class DaySettings {
    private final long dayLengthTicks;
    private final long nightLengthTicks;
    private final double speedMultiplier;
    private final Map<String, Long> customDayTypes;
    private final String reloadPermission;
    private final String triggerPermission;
    private final String setDayLengthPermission;
    private final String setNightLengthPermission;
    private final String setSpeedPermission;

    public DaySettings(
            long dayLengthTicks,
            long nightLengthTicks,
            double speedMultiplier,
            Map<String, Long> customDayTypes,
            String reloadPermission,
            String triggerPermission,
            String setDayLengthPermission,
            String setNightLengthPermission,
            String setSpeedPermission) {
        this.dayLengthTicks = dayLengthTicks;
        this.nightLengthTicks = nightLengthTicks;
        this.speedMultiplier = speedMultiplier;
        this.customDayTypes = Collections.unmodifiableMap(customDayTypes);
        this.reloadPermission = reloadPermission;
        this.triggerPermission = triggerPermission;
        this.setDayLengthPermission = setDayLengthPermission;
        this.setNightLengthPermission = setNightLengthPermission;
        this.setSpeedPermission = setSpeedPermission;
    }

    public long dayLengthTicks() {
        return dayLengthTicks;
    }

    public long nightLengthTicks() {
        return nightLengthTicks;
    }

    public double speedMultiplier() {
        return speedMultiplier;
    }

    public Map<String, Long> customDayTypes() {
        return customDayTypes;
    }

    public String reloadPermission() {
        return reloadPermission;
    }

    public String triggerPermission() {
        return triggerPermission;
    }

    public String setDayLengthPermission() {
        return setDayLengthPermission;
    }

    public String setNightLengthPermission() {
        return setNightLengthPermission;
    }

    public String setSpeedPermission() {
        return setSpeedPermission;
    }
}
