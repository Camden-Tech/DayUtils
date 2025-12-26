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

    /**
     * Captures the current cycle status for a world.
     */
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

    /**
     * @return true when the world is currently in the day phase
     */
    public boolean isDay() {
        return day;
    }

    /**
     * @return true when the world is currently in the night phase
     */
    public boolean isNight() {
        return night;
    }

    /**
     * @return progress through the day portion of the cycle
     */
    public double getDayPercent() {
        return dayPercent;
    }

    /**
     * @return progress through the night portion of the cycle
     */
    public double getNightPercent() {
        return nightPercent;
    }

    /**
     * @return progress through the full day/night cycle
     */
    public double getCyclePercent() {
        return cyclePercent;
    }

    /**
     * @return progress for each configured custom day type
     */
    public Map<String, Double> getCustomPercent() {
        return customPercent;
    }

    /**
     * @return the day settings used to compute this status
     */
    public DaySettings getSettings() {
        return settings;
    }

    /**
     * @return number of nights completed in this world
     */
    public long getNightsPassed() {
        return nightsPassed;
    }
}
