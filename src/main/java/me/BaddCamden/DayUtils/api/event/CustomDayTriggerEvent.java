package me.BaddCamden.DayUtils.api.event;

import me.BaddCamden.DayUtils.config.CustomDayType;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a configured {@link CustomDayType} completes its interval and triggers.
 *
 * <p>A listener can react to custom milestones or broadcast messages:</p>
 * <pre>{@code
 * @EventHandler
 * public void onCustom(CustomDayTriggerEvent event) {
 *     if ("bloodmoon".equalsIgnoreCase(event.getType().getName())) {
 *         Bukkit.broadcastMessage("Brace yourselves!");
 *     }
 * }
 * }</pre>
 * <p>Register your listener with Bukkit's plugin manager to receive callbacks.</p>
 */
public class CustomDayTriggerEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final World world;
    private final CustomDayType type;

    public CustomDayTriggerEvent(World world, CustomDayType type) {
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
