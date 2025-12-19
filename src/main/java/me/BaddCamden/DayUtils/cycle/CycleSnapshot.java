package me.BaddCamden.DayUtils.cycle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import me.BaddCamden.DayUtils.config.DaySettings;
import org.bukkit.World;

/**
 * Immutable snapshot of a world's cycle state and configuration.
 */
public class CycleSnapshot {
    private final World world;
    private final boolean day;
    private final boolean night;
    private final double dayPercent;
    private final double nightPercent;
    private final double cyclePercent;
    private final Map<String, Double> customDayPercent;
    private final DaySettings settings;

    public CycleSnapshot(World world, boolean day, boolean night, double dayPercent, double nightPercent,
                         double cyclePercent, Map<String, Double> customDayPercent, DaySettings settings) {
        this.world = world;
        this.day = day;
        this.night = night;
        this.dayPercent = dayPercent;
        this.nightPercent = nightPercent;
        this.cyclePercent = cyclePercent;
        this.customDayPercent = Collections.unmodifiableMap(new HashMap<>(customDayPercent));
        this.settings = settings;
    }

    public World world() {
        return world;
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

    public Map<String, Double> getCustomDayPercent() {
        return customDayPercent;
    }

    public DaySettings getSettings() {
        return settings;
    }
}
