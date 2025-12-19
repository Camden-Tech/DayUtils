package me.BaddCamden.DayUtils.api.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired every tick with the current time state for a world.
 *
 * <p>Listeners can use this event to react to fine-grained progress updates. For example:</p>
 * <pre>{@code
 * @EventHandler
 * public void onTick(TimeTickEvent event) {
 *     double cycle = event.getCyclePercent();
 *     if (cycle > 0.5) {
 *         // Second half of the day/night cycle
 *     }
 *     long nights = event.getNightsPassed();
 * }
 * }</pre>
 * <p>Register your listener with Bukkit's plugin manager to receive callbacks.</p>
 */
public class TimeTickEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final World world;
    private final boolean day;
    private final boolean night;
    private final double dayPercent;
    private final double nightPercent;
    private final double cyclePercent;
    private final Map<String, Double> customProgress;
    private final long nightsPassed;

    public TimeTickEvent(World world, boolean day, boolean night, double dayPercent, double nightPercent,
                         double cyclePercent, Map<String, Double> customProgress, long nightsPassed) {
        this.world = world;
        this.day = day;
        this.night = night;
        this.dayPercent = dayPercent;
        this.nightPercent = nightPercent;
        this.cyclePercent = cyclePercent;
        this.customProgress = Collections.unmodifiableMap(new HashMap<>(customProgress));
        this.nightsPassed = nightsPassed;
    }

    public World getWorld() {
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

    public Map<String, Double> getCustomProgress() {
        return customProgress;
    }

    public long getNightsPassed() {
        return nightsPassed;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
