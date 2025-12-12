package me.BaddCamden.DayUtils;

import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.java.JavaPlugin;

public class DayScheduler {
    private final JavaPlugin plugin;
    private DaySettings settings;
    private BukkitTask task;

    public DayScheduler(JavaPlugin plugin, DaySettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public void start() {
        restartTask();
    }

    public void updateSettings(DaySettings settings) {
        this.settings = settings;
        restartTask();
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void restartTask() {
        stop();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            plugin.getLogger().fine(
                    String.format(
                            "Running day cycle tick (day=%d, night=%d, custom=%d)",
                            settings.dayLengthTicks(), settings.nightLengthTicks(), settings.customDayTypes().size()));
        }, 0L, Math.max(1L, settings.dayLengthTicks()));
    }
}
