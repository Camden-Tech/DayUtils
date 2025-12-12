package me.BaddCamden.DayUtils.api;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffect;

/**
 * Represents a custom cycle segment that can be scheduled by DayUtils or triggered manually.
 */
public interface CustomDayType {
    /**
     * Unique identifier used for lookups and configuration keys.
     */
    String id();

    /**
     * Display name shown to users when referencing this custom type.
     */
    String displayName();

    /**
     * Primary color associated with the custom day.
     */
    ChatColor color();

    /**
     * Effects that should be applied or displayed when the custom day starts.
     */
    List<PotionEffect> effects();

    /**
     * Duration, in ticks, before the end callback should fire.
     */
    long durationTicks();

    /**
     * Invoked when the custom day is scheduled to begin.
     */
    void onStart(CustomDayContext context);

    /**
     * Invoked when the custom day ends.
     */
    void onEnd(CustomDayContext context);
}
