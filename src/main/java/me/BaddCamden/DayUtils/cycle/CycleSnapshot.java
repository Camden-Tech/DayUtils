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
    private final long nightsPassed;

    /**
     * Captures a single point-in-time view of world cycle progress.
     */
    public CycleSnapshot(World world, boolean day, boolean night, double dayPercent, double nightPercent,
                         double cyclePercent, Map<String, Double> customDayPercent, DaySettings settings,
                         long nightsPassed) {
        this.world = world;
        this.day = day;
        this.night = night;
        this.dayPercent = dayPercent;
        this.nightPercent = nightPercent;
        this.cyclePercent = cyclePercent;
        this.customDayPercent = Collections.unmodifiableMap(new HashMap<>(customDayPercent));
        this.settings = settings;
        this.nightsPassed = nightsPassed;
    }

    /**
     * @return world associated with this snapshot
     */
    public World world() {
        return world;
    }

    /**
     * @return true if the snapshot indicates daytime
     */
    public boolean isDay() {
        return day;
    }

    /**
     * @return true if the snapshot indicates nighttime
     */
    public boolean isNight() {
        return night;
    }

    /**
     * @return progress through the day portion
     */
    public double getDayPercent() {
        return dayPercent;
    }

    /**
     * @return progress through the night portion
     */
    public double getNightPercent() {
        return nightPercent;
    }

    /**
     * @return progress through the entire day/night cycle
     */
    public double getCyclePercent() {
        return cyclePercent;
    }

    /**
     * @return unmodifiable map of custom day progress ratios
     */
    public Map<String, Double> getCustomDayPercent() {
        return customDayPercent;
    }

    /**
     * @return settings used for this snapshot
     */
    public DaySettings getSettings() {
        return settings;
    }

    /**
     * @return number of completed nights at snapshot time
     */
    public long getNightsPassed() {
        return nightsPassed;
    }
}
