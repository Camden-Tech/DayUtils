package me.BaddCamden.DayUtils.event;

import me.BaddCamden.DayUtils.config.CustomDayType;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a configured custom day interval completes.
 */
public class CustomDayEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final World world;
    private final CustomDayType type;

    public CustomDayEvent(World world, CustomDayType type) {
        this.world = world;
        this.type = type;
    }

    public World getWorld() {
        return world;
    }

    public CustomDayType getType() {
        return type;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
