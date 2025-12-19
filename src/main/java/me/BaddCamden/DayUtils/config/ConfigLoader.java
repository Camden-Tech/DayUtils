package me.BaddCamden.DayUtils.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Reads and validates configuration, applying defaults and clamping values.
 */
public class ConfigLoader {
    private final JavaPlugin plugin;
    private final Logger logger;

    public ConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public LoadResult load() {
        FileConfiguration config = plugin.getConfig();
        boolean modified = false;

        modified |= ensureCustomSection(config);

        long dayLength = readLength(config, "day.length", SettingsConstraints.DEFAULT_DAY_LENGTH);
        modified |= updateValue(config, "day.length", dayLength);

        long nightLength = readLength(config, "day.nightLength", SettingsConstraints.DEFAULT_NIGHT_LENGTH);
        modified |= updateValue(config, "day.nightLength", nightLength);

        double speed = readSpeed(config);
        modified |= updateValue(config, "day.speed", speed);

        Map<String, CustomDayType> customTypes = readCustomTypes(config);
        for (CustomDayType type : customTypes.values()) {
            String path = "day.customTypes." + type.getName();
            modified |= updateValue(config, path, type.getIntervalTicks());
        }

        CommandSettings commandSettings = CommandSettings.fromConfig(config);
        MessageSettings messageSettings = MessageSettings.fromConfig(config);
        DaySettings daySettings = new DaySettings(dayLength, nightLength, speed, customTypes);

        return new LoadResult(new DayUtilsConfiguration(daySettings, commandSettings, messageSettings), modified);
    }

    private long readLength(FileConfiguration config, String path, long defaultValue) {
        Object raw = config.get(path);
        if (!(raw instanceof Number)) {
            if (config.contains(path)) {
                logger.warning(() -> "Invalid value for " + path + " (" + raw + "); using default "
                    + defaultValue + " ticks.");
            }
            return SettingsConstraints.clampLength(defaultValue);
        }

        long value = ((Number) raw).longValue();
        long clamped = SettingsConstraints.clampLength(value);
        if (clamped != value) {
            logger.warning(() -> path + " value " + value + " outside bounds (" + SettingsConstraints.MIN_LENGTH_TICKS
                + "-" + SettingsConstraints.MAX_LENGTH_TICKS + "); clamped to " + clamped + ".");
        }
        return clamped;
    }

    private double readSpeed(FileConfiguration config) {
        Object raw = config.get("day.speed");
        if (!(raw instanceof Number)) {
            if (config.contains("day.speed")) {
                logger.warning(() -> "Invalid value for day.speed (" + raw + "); using default "
                    + SettingsConstraints.DEFAULT_SPEED + ".");
            }
            return SettingsConstraints.DEFAULT_SPEED;
        }

        double value = ((Number) raw).doubleValue();
        double clamped = SettingsConstraints.clampSpeed(value);
        if (clamped != value) {
            logger.warning(() -> "day.speed value " + value + " outside bounds (" + SettingsConstraints.MIN_SPEED
                + "-" + SettingsConstraints.MAX_SPEED + "); clamped to " + clamped + ".");
        }
        return clamped;
    }

    private Map<String, CustomDayType> readCustomTypes(FileConfiguration config) {
        Map<String, CustomDayType> customTypes = new LinkedHashMap<>();
        ConfigurationSection section = config.getConfigurationSection("day.customTypes");
        if (section == null) {
            return customTypes;
        }

        for (String key : section.getKeys(false)) {
            Object raw = section.get(key);
            if (!(raw instanceof Number)) {
                logger.warning(() -> "Invalid interval for custom day '" + key + "' (" + raw
                    + "); using minimum of " + SettingsConstraints.MIN_CUSTOM_INTERVAL + " tick.");
                customTypes.put(key.toLowerCase(), new CustomDayType(key, SettingsConstraints.MIN_CUSTOM_INTERVAL));
                continue;
            }

            long interval = ((Number) raw).longValue();
            long clamped = SettingsConstraints.clampCustomInterval(interval);
            if (clamped != interval) {
                logger.warning(() -> "Custom day '" + key + "' interval " + interval + " outside bounds ("
                    + SettingsConstraints.MIN_CUSTOM_INTERVAL + "-" + SettingsConstraints.MAX_LENGTH_TICKS
                    + "); clamped to " + clamped + ".");
            }
            customTypes.put(key.toLowerCase(), new CustomDayType(key, clamped));
        }
        return customTypes;
    }

    private boolean ensureCustomSection(FileConfiguration config) {
        if (config.getConfigurationSection("day.customTypes") != null) {
            return false;
        }
        config.createSection("day.customTypes");
        return true;
    }

    private boolean updateValue(FileConfiguration config, String path, Object value) {
        Object existing = config.get(path);
        if (valuesEqual(existing, value)) {
            return false;
        }
        config.set(path, value);
        return true;
    }

    private boolean valuesEqual(Object a, Object b) {
        if (a instanceof Number left && b instanceof Number right) {
            return Double.compare(left.doubleValue(), right.doubleValue()) == 0;
        }
        return Objects.equals(a, b);
    }

    public record LoadResult(DayUtilsConfiguration configuration, boolean modified) {
    }
}
