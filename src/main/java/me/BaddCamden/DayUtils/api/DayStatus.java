package me.BaddCamden.DayUtils.api;

import java.util.Map;
import me.BaddCamden.DayUtils.config.DaySettings;

/**
 * Data holder describing the current day/night cycle state.
 */
public class DayStatus {
    private final boolean day;
    private final boolean night;
    private final double dayPercent;
    private final double nightPercent;
    private final double cyclePercent;
    private final Map<String, Double> customPercent;
    private final DaySettings settings;
    private final long nightsPassed;

    public DayStatus(boolean day, boolean night, double dayPercent, double nightPercent, double cyclePercent,
                     Map<String, Double> customPercent, DaySettings settings, long nightsPassed) {
        this.day = day;
        this.night = night;
        this.dayPercent = dayPercent;
        this.nightPercent = nightPercent;
        this.cyclePercent = cyclePercent;
        this.customPercent = customPercent;
        this.settings = settings;
        this.nightsPassed = nightsPassed;
    }

    public boolean isDay() {
        return day;
    }

    public boolean isNight() {
        return night;
    }

    public double getDayPercent() {
        return dayPercent;
    }

    public double getNightPercent() {
        return nightPercent;
    }

    public double getCyclePercent() {
        return cyclePercent;
    }

    public Map<String, Double> getCustomPercent() {
        return customPercent;
    }

    public DaySettings getSettings() {
        return settings;
    }

    public long getNightsPassed() {
        return nightsPassed;
    }
}
