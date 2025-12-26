package me.BaddCamden.DayUtils.api;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import me.BaddCamden.DayUtils.config.CustomDayType;
import me.BaddCamden.DayUtils.config.DaySettings;
import me.BaddCamden.DayUtils.config.DayUtilsConfiguration;
import me.BaddCamden.DayUtils.cycle.CycleSnapshot;
import me.BaddCamden.DayUtils.cycle.DayCycleManager;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Public API entry-point for DayUtils.
 */
public class DayUtilsApi {

    private static DayUtilsApi instance;

    private final Supplier<DayCycleManager> cycleManagerSupplier;

    /**
     * Creates a new API instance backed by a supplier of the active cycle manager.
     */
    public DayUtilsApi(Supplier<DayCycleManager> cycleManagerSupplier) {
        this.cycleManagerSupplier = cycleManagerSupplier;
        instance = this;
    }

    /**
     * @return the singleton API instance
     */
    public static DayUtilsApi get() {
        return instance;
    }

    /**
     * @return true if the specified world is currently in the configured day phase
     */
    public boolean isDay(World world) {
        DayStatus status = status(world);
        return status != null && status.isDay();
    }

    /**
     * @return true if the specified world is currently in the configured night phase
     */
    public boolean isNight(World world) {
        DayStatus status = status(world);
        return status != null && status.isNight();
    }

    /**
     * Checks whether the given world currently tracks progress for a custom day type.
     */
    public boolean isCustomDay(World world, String type) {
        DayStatus status = status(world);
        return status != null && status.getCustomPercent().containsKey(type.toLowerCase(Locale.ROOT));
    }

    /**
     * @return day progress as a ratio, or null if the world is unmanaged
     */
    public Double getDayPercent(World world) {
        DayStatus status = status(world);
        return status != null ? status.getDayPercent() : null;
    }

    /**
     * @return night progress as a ratio, or null if the world is unmanaged
     */
    public Double getNightPercent(World world) {
        DayStatus status = status(world);
        return status != null ? status.getNightPercent() : null;
    }

    /**
     * @return overall day/night cycle progress as a ratio, or null if unavailable
     */
    public Double getCyclePercent(World world) {
        DayStatus status = status(world);
        return status != null ? status.getCyclePercent() : null;
    }

    /**
     * @return progress for a custom day type, or null if no status exists
     */
    public Double getCustomDayPercent(World world, String type) {
        DayStatus status = status(world);
        if (status == null) {
            return null;
        }
        return status.getCustomPercent().get(type.toLowerCase(Locale.ROOT));
    }

    /**
     * @return nights passed in the specified world, or null if unmanaged
     */
    public Long getNightsPassed(World world) {
        DayStatus status = status(world);
        return status != null ? status.getNightsPassed() : null;
    }

    /**
     * @return current day settings from the active configuration
     */
    public DaySettings getSettings() {
        DayCycleManager manager = cycleManagerSupplier.get();
        return manager.getConfiguration().getDaySettings();
    }

    /**
     * @return an unmodifiable view of configured custom day types
     */
    public Map<String, CustomDayType> getCustomDayTypes() {
        return Collections.unmodifiableMap(getSettings().getCustomTypes());
    }

    /**
     * Triggers a custom day type immediately.
     */
    public boolean triggerCustomDay(World world, String type) {
        DayCycleManager manager = cycleManagerSupplier.get();
        return manager.triggerCustomDay(world, type);
    }

    /**
     * Produces a status object describing the current phase for the given world.
     */
    public DayStatus status(World world) {
        DayCycleManager manager = cycleManagerSupplier.get();
        CycleSnapshot snapshot = manager.snapshot(world);
        if (snapshot == null) {
            return null;
        }
        return new DayStatus(snapshot.isDay(), snapshot.isNight(), snapshot.getDayPercent(),
            snapshot.getNightPercent(), snapshot.getCyclePercent(), snapshot.getCustomDayPercent(),
            snapshot.getSettings(), snapshot.getNightsPassed());
    }

    /**
     * @return status snapshots for all tracked worlds keyed by UUID
     */
    public Map<UUID, DayStatus> allStatuses() {
        DayCycleManager manager = cycleManagerSupplier.get();
        return manager.snapshots().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                CycleSnapshot snapshot = entry.getValue();
                return new DayStatus(snapshot.isDay(), snapshot.isNight(), snapshot.getDayPercent(),
                    snapshot.getNightPercent(), snapshot.getCyclePercent(), snapshot.getCustomDayPercent(),
                    snapshot.getSettings(), snapshot.getNightsPassed());
            }));
    }

    /**
     * @return the current configuration backing the cycle manager
     */
    public DayUtilsConfiguration configuration() {
        return cycleManagerSupplier.get().getConfiguration();
    }

    /**
     * Initializes the singleton API using a supplier for lazy resolution.
     */
    public static DayUtilsApi bootstrap(Supplier<DayCycleManager> cycleManagerSupplier) {
        return new DayUtilsApi(cycleManagerSupplier);
    }

    /**
     * Convenience helper for retrieving a world by name.
     */
    public static World world(String name) {
        return Bukkit.getWorld(name);
    }
}
