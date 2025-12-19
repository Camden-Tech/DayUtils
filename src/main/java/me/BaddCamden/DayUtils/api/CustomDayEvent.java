package me.BaddCamden.DayUtils.api;

import me.BaddCamden.DayUtils.config.CustomDayType;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a custom day type starts or ends.
 */
public class CustomDayEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final World world;
    private final CustomDayType dayType;
    private final Phase phase;

    public CustomDayEvent(World world, CustomDayType dayType, Phase phase) {
        this.world = world;
        this.dayType = dayType;
        this.phase = phase;
    }

    public World world() {
        return world;
    }

    public CustomDayType dayType() {
        return dayType;
    }

    public Phase phase() {
        return phase;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum Phase {
        START,
        END
    }
}
