package me.BaddCamden.DayUtils.api.event;

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

    /**
     * Creates a new event for the given custom day phase transition.
     */
    public CustomDayEvent(World world, CustomDayType dayType, Phase phase) {
        this.world = world;
        this.dayType = dayType;
        this.phase = phase;
    }

    /**
     * @return world where the custom day is occurring
     */
    public World world() {
        return world;
    }

    /**
     * @return the triggered custom day type
     */
    public CustomDayType dayType() {
        return dayType;
    }

    /**
     * @return whether this event marks the start or end of the custom day
     */
    public Phase phase() {
        return phase;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * @return shared handler list for Bukkit's event system
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum Phase {
        START,
        END
    }
}
