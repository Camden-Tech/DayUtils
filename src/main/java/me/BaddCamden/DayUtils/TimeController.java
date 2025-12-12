package me.BaddCamden.DayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class TimeController {
    private static final long FULL_DAY_TICKS = 24000L;
    private static final long HALF_DAY_TICKS = FULL_DAY_TICKS / 2;

    private final JavaPlugin plugin;
    private DaySettings settings;
    private BukkitTask task;
    private final Map<UUID, Double> trackedTime = new HashMap<>();

    public TimeController(JavaPlugin plugin, DaySettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public void start() {
        restartTask();
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        trackedTime.clear();
    }

    public void updateSettings(DaySettings settings) {
        this.settings = settings;
        restartTask();
    }

    private void restartTask() {
        stop();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tickWorlds, 1L, 1L);
    }

    private void tickWorlds() {
        for (World world : plugin.getServer().getWorlds()) {
            advanceWorldTime(world);
        }
    }

    private void advanceWorldTime(World world) {
        double currentTime = trackedTime.computeIfAbsent(world.getUID(), id -> (double) world.getTime());
        boolean isDay = currentTime % FULL_DAY_TICKS < HALF_DAY_TICKS;
        double configuredLength = isDay ? settings.dayLengthTicks() : settings.nightLengthTicks();
        if (configuredLength <= 0) {
            configuredLength = 1;
        }

        double incrementPerTick = HALF_DAY_TICKS / configuredLength;
        double nextTime = (currentTime + incrementPerTick) % FULL_DAY_TICKS;
        trackedTime.put(world.getUID(), nextTime);
        world.setTime((long) nextTime);
    }
}
