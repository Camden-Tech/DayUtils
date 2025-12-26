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

    /**
     * Constructs immutable day settings, clamping values within configured bounds.
     *
     * @param dayLength day duration in ticks
     * @param nightLength night duration in ticks
     * @param speedMultiplier how quickly time should progress
     * @param customTypes map of custom day types keyed by lower-case identifier
     */
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

    /**
     * @return configured day length in ticks
     */
    public long getDayLength() {
        return dayLength;
    }

    /**
     * @return configured night length in ticks
     */
    public long getNightLength() {
        return nightLength;
    }

    /**
     * @return multiplier applied to time progression speed
     */
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    /**
     * @return unmodifiable map of registered custom day types keyed by identifier
     */
    public Map<String, CustomDayType> getCustomTypes() {
        return customTypes;
    }

    /**
     * Builds settings from the Bukkit configuration, applying sensible defaults.
     */
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
