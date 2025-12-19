package me.BaddCamden.DayUtils.api.event;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a world's phase changes between day and night.
 *
 * <p>Useful for coarse-grained reactions like lighting adjustments or announcements.</p>
 *
 * <pre>{@code
 * @EventHandler
 * public void onPhaseChange(DayPhaseChangeEvent event) {
 *     if (event.getNewPhase() == DayPhaseChangeEvent.Phase.NIGHT) {
 *         // Enable custom mob spawns or alerts
 *     }
 * }
 * }</pre>
 * <p>Register your listener with Bukkit's plugin manager to receive callbacks.</p>
 */
public class DayPhaseChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final World world;
    private final Phase newPhase;

    public DayPhaseChangeEvent(World world, Phase newPhase) {
        this.world = world;
        this.newPhase = newPhase;
    }

    public World getWorld() {
        return world;
    }

    public Phase getNewPhase() {
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
