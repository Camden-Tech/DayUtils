package me.BaddCamden.DayUtils.api;

import org.bukkit.World;

/**
 * Exposes read-only information about the current day/night cycle and any active custom days.
 */
public interface DayInfoService {
    /**
     * Returns the current progress through the configured day length as a value between 0.0 and 1.0.
     * If the world is not currently in its day phase, this returns 0.
     */
    double getDayProgress(World world);

    /**
     * Returns the current progress through the configured night length as a value between 0.0 and 1.0.
     * If the world is not currently in its night phase, this returns 0.
     */
    double getNightProgress(World world);

    /**
     * Returns the progress through the full day/night cycle (0.0 to 1.0). If a custom day type is
     * active for the world, its elapsed duration is used instead.
     */
    double getFullCycleProgress(World world);

    /**
     * True when the world is presently in its day phase.
     */
    boolean isDay(World world);

    /**
     * True when the world is presently in its night phase.
     */
    boolean isNight(World world);

    /**
     * True when the provided custom day id matches the active custom day for the world.
     */
    boolean isCustomDay(World world, String typeId);
}
