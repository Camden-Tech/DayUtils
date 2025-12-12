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

        DaySettings settings = new DaySettings(dayLength, nightLength, customTypes, reloadPermission);
        return new LoadResult(settings, modified);
    }

    public record LoadResult(DaySettings settings, boolean modified) {
    }
}
