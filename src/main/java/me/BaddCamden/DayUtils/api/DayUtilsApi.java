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

    public DayUtilsApi(Supplier<DayCycleManager> cycleManagerSupplier) {
        this.cycleManagerSupplier = cycleManagerSupplier;
        instance = this;
    }

    public static DayUtilsApi get() {
        return instance;
    }

    public boolean isDay(World world) {
        DayStatus status = status(world);
        return status != null && status.isDay();
    }

    public boolean isNight(World world) {
        DayStatus status = status(world);
        return status != null && status.isNight();
    }

    public boolean isCustomDay(World world, String type) {
        DayStatus status = status(world);
        return status != null && status.getCustomPercent().containsKey(type.toLowerCase(Locale.ROOT));
    }

    public Double getDayPercent(World world) {
        DayStatus status = status(world);
        return status != null ? status.getDayPercent() : null;
    }

    public Double getNightPercent(World world) {
        DayStatus status = status(world);
        return status != null ? status.getNightPercent() : null;
    }

    public Double getCyclePercent(World world) {
        DayStatus status = status(world);
        return status != null ? status.getCyclePercent() : null;
    }

    public Double getCustomDayPercent(World world, String type) {
        DayStatus status = status(world);
        if (status == null) {
            return null;
        }
        return status.getCustomPercent().get(type.toLowerCase(Locale.ROOT));
    }

    public Long getNightsPassed(World world) {
        DayStatus status = status(world);
        return status != null ? status.getNightsPassed() : null;
    }

    public DaySettings getSettings() {
        DayCycleManager manager = cycleManagerSupplier.get();
        return manager.getConfiguration().getDaySettings();
    }

    public Map<String, CustomDayType> getCustomDayTypes() {
        return Collections.unmodifiableMap(getSettings().getCustomTypes());
    }

    public boolean triggerCustomDay(World world, String type) {
        DayCycleManager manager = cycleManagerSupplier.get();
        return manager.triggerCustomDay(world, type);
    }

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

    public DayUtilsConfiguration configuration() {
        return cycleManagerSupplier.get().getConfiguration();
    }

    public static DayUtilsApi bootstrap(Supplier<DayCycleManager> cycleManagerSupplier) {
        return new DayUtilsApi(cycleManagerSupplier);
    }

    public static World world(String name) {
        return Bukkit.getWorld(name);
    }
}
