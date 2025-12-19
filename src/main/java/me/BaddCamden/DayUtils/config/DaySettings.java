package me.BaddCamden.DayUtils.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Immutable configuration backing for day/night behaviour.
 */
public class DaySettings {
    private final long dayLength;
    private final long nightLength;
    private final double speedMultiplier;
    private final Map<String, CustomDayType> customTypes;

    public DaySettings(long dayLength, long nightLength, double speedMultiplier,
                       Map<String, CustomDayType> customTypes) {
        this.dayLength = SettingsConstraints.clampLength(dayLength);
        this.nightLength = SettingsConstraints.clampLength(nightLength);
        this.speedMultiplier = SettingsConstraints.clampSpeed(speedMultiplier);
        this.customTypes = Collections.unmodifiableMap(
            customTypes.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    CustomDayType type = entry.getValue();
                    long interval = SettingsConstraints.clampCustomInterval(type.getIntervalTicks());
                    return new CustomDayType(type.getName(), interval);
                }, (a, b) -> a, LinkedHashMap::new)));
    }

    public long getDayLength() {
        return dayLength;
    }

    public long getNightLength() {
        return nightLength;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public Map<String, CustomDayType> getCustomTypes() {
        return customTypes;
    }

    public static DaySettings fromConfig(FileConfiguration config) {
        long day = config.getLong("day.length", 12000L);
        long night = config.getLong("day.nightLength", 12000L);
        double speed = config.getDouble("day.speed", 1.0d);
        Map<String, CustomDayType> customTypes = new LinkedHashMap<>();

        ConfigurationSection section = config.getConfigurationSection("day.customTypes");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                long interval = section.getLong(key, 0L);
                customTypes.put(key.toLowerCase(), new CustomDayType(key, interval));
            }
        }

        return new DaySettings(day, night, speed, customTypes);
    }
}
