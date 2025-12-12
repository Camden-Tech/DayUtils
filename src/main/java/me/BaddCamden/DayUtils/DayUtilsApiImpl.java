package me.BaddCamden.DayUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import me.BaddCamden.DayUtils.api.CustomDayType;
import me.BaddCamden.DayUtils.api.DayInfoService;
import me.BaddCamden.DayUtils.api.DayUtilsApi;
import org.bukkit.World;

class DayUtilsApiImpl implements DayUtilsApi {
    private static final long DEFAULT_INTERVAL = 24000L;

    private final Map<String, CustomDayRegistration> registrations = new ConcurrentHashMap<>();
    private volatile Map<String, Long> configuredIntervals = Map.of();
    private CustomDayScheduler scheduler;
    private DayInfoService dayInfoService;

    DayUtilsApiImpl() {}

    public void setScheduler(CustomDayScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setDayInfoService(DayInfoService dayInfoService) {
        this.dayInfoService = dayInfoService;
    }

    public void updateConfiguredIntervals(Map<String, Long> configuredIntervals) {
        this.configuredIntervals = Map.copyOf(configuredIntervals);
        registrations.replaceAll((key, registration) -> applyConfigOverrideIfManaged(registration));
    }

    @Override
    public void registerCustomDayType(CustomDayType type) {
        long interval = configuredIntervals.getOrDefault(type.id(), DEFAULT_INTERVAL);
        register(type, interval, true);
    }

    @Override
    public void registerCustomDayType(CustomDayType type, long intervalTicks) {
        register(type, intervalTicks, false);
    }

    private void register(CustomDayType type, long intervalTicks, boolean managedByConfig) {
        Objects.requireNonNull(type, "custom day type");
        long interval = Math.max(1L, intervalTicks);
        String key = normalizeId(type.id());
        CustomDayRegistration registration = new CustomDayRegistration(type, interval, managedByConfig);
        registrations.put(key, applyConfigOverrideIfManaged(registration));
    }

    @Override
    public boolean unregisterCustomDayType(String typeId) {
        return registrations.remove(normalizeId(typeId)) != null;
    }

    @Override
    public boolean triggerCustomDay(String typeId, World world) {
        if (scheduler == null) {
            return false;
        }

        Optional<CustomDayRegistration> registration = getRegistration(typeId);
        return registration.filter(reg -> scheduler.triggerNow(reg, world)).isPresent();
    }

    @Override
    public Collection<CustomDayType> getRegisteredCustomDayTypes() {
        Collection<CustomDayType> snapshot = new ArrayList<>();
        registrations.values().forEach(reg -> snapshot.add(reg.type()));
        return Collections.unmodifiableCollection(snapshot);
    }

    @Override
    public DayInfoService getDayInfoService() {
        return dayInfoService;
    }

    Collection<CustomDayRegistration> getRegistrations() {
        return Collections.unmodifiableCollection(registrations.values());
    }

    Optional<CustomDayRegistration> getRegistration(String id) {
        return Optional.ofNullable(registrations.get(normalizeId(id)));
    }

    private String normalizeId(String id) {
        return id.toLowerCase(Locale.ROOT);
    }

    private CustomDayRegistration applyConfigOverrideIfManaged(CustomDayRegistration registration) {
        if (!registration.managedByConfig()) {
            return registration;
        }

        long interval = configuredIntervals.getOrDefault(registration.type().id(), registration.intervalTicks());
        return registration.updateInterval(Math.max(1L, interval));
    }
}

record CustomDayRegistration(CustomDayType type, long intervalTicks, boolean managedByConfig) {
    CustomDayRegistration {
        Objects.requireNonNull(type, "type");
    }

    CustomDayRegistration updateInterval(long intervalTicks) {
        return new CustomDayRegistration(type, intervalTicks, managedByConfig);
    }
}
