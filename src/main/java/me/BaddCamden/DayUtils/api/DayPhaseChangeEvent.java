package me.BaddCamden.DayUtils.api;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a world's vanilla day phase switches between day and night.
 */
public class DayPhaseChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final World world;
    private final Phase newPhase;

    public DayPhaseChangeEvent(World world, Phase newPhase) {
        this.world = world;
        this.newPhase = newPhase;
    }

    public World world() {
        return world;
    }

    public Phase newPhase() {
        return newPhase;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum Phase {
        DAY,
        NIGHT
    }
}
