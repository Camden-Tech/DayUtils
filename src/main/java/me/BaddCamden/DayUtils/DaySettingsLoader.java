package me.BaddCamden.DayUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DaySettingsLoader {
    private final JavaPlugin plugin;

    public DaySettingsLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public LoadResult load() {
        FileConfiguration config = plugin.getConfig();
        boolean modified = false;

        long dayLength = config.getLong("day.length", 12000L);
        if (!config.contains("day.length")) {
            config.set("day.length", dayLength);
            modified = true;
        }

        long nightLength = config.getLong("day.nightLength", 12000L);
        if (!config.contains("day.nightLength")) {
            config.set("day.nightLength", nightLength);
            modified = true;
        }

        double speed = config.getDouble("day.speed", 1.0D);
        if (!config.contains("day.speed")) {
            config.set("day.speed", speed);
            modified = true;
        }

        ConfigurationSection customSection = config.getConfigurationSection("day.customTypes");
        if (customSection == null) {
            customSection = config.createSection("day.customTypes");
            modified = true;
        }

        Map<String, Long> customTypes = new LinkedHashMap<>();
        for (String key : customSection.getKeys(false)) {
            customTypes.put(key, customSection.getLong(key));
        }

        String reloadPermission = config.getString("commands.reloadPermission", "dayutils.reload");
        if (!config.contains("commands.reloadPermission")) {
            config.set("commands.reloadPermission", reloadPermission);
            modified = true;
        }

        String triggerPermission = config.getString("commands.triggerPermission", "dayutils.trigger");
        if (!config.contains("commands.triggerPermission")) {
            config.set("commands.triggerPermission", triggerPermission);
            modified = true;
        }

        String setDayLengthPermission =
                config.getString("commands.setDayLengthPermission", "dayutils.setdaylength");
        if (!config.contains("commands.setDayLengthPermission")) {
            config.set("commands.setDayLengthPermission", setDayLengthPermission);
            modified = true;
        }

        String setNightLengthPermission =
                config.getString("commands.setNightLengthPermission", "dayutils.setnightlength");
        if (!config.contains("commands.setNightLengthPermission")) {
            config.set("commands.setNightLengthPermission", setNightLengthPermission);
            modified = true;
        }

        String setSpeedPermission = config.getString("commands.setSpeedPermission", "dayutils.setspeed");
        if (!config.contains("commands.setSpeedPermission")) {
            config.set("commands.setSpeedPermission", setSpeedPermission);
            modified = true;
        }

        DaySettings settings =
                new DaySettings(
                        dayLength,
                        nightLength,
                        speed,
                        customTypes,
                        reloadPermission,
                        triggerPermission,
                        setDayLengthPermission,
                        setNightLengthPermission,
                        setSpeedPermission);
        return new LoadResult(settings, modified);
    }

    public record LoadResult(DaySettings settings, boolean modified) {
    }
}
