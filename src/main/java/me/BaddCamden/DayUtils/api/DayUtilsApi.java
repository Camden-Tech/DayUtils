package me.BaddCamden.DayUtils.api;

import java.util.Collection;
import org.bukkit.World;

/**
 * Entry point exposed to hook plugins that want to schedule or trigger custom day segments.
 */
public interface DayUtilsApi {
    /**
     * Registers a custom day type using the interval defined in the DayUtils configuration (or a default of one
     * vanilla day if the type is not present in the config).
     */
    void registerCustomDayType(CustomDayType type);

    /**
     * Registers a custom day type with an explicit interval measured in server ticks.
     */
    void registerCustomDayType(CustomDayType type, long intervalTicks);

    /**
     * Unregisters a custom day type using its id.
     *
     * @return true if the type was previously registered
     */
    boolean unregisterCustomDayType(String typeId);

    /**
     * Triggers the given custom day immediately in the provided world.
     *
     * @return true if the custom day id was found and triggered
     */
    boolean triggerCustomDay(String typeId, World world);

    /**
     * Returns a snapshot of the currently registered custom day types.
     */
    Collection<CustomDayType> getRegisteredCustomDayTypes();
}
