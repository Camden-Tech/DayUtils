package me.BaddCamden.DayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.BaddCamden.DayUtils.api.event.DayPhaseChangeEvent;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class TimeController {
    private static final long FULL_DAY_TICKS = 24000L;
    private static final long HALF_DAY_TICKS = FULL_DAY_TICKS / 2;

    private final JavaPlugin plugin;
    private final CustomDayScheduler customDayScheduler;
    private DaySettings settings;
    private BukkitTask task;
    private final Map<UUID, Double> trackedTime = new HashMap<>();
    private final Map<UUID, Boolean> worldDayStates = new HashMap<>();

    public TimeController(JavaPlugin plugin, DaySettings settings, DayUtilsApiImpl api) {
        this.plugin = plugin;
        this.settings = settings;
        this.customDayScheduler = new CustomDayScheduler(plugin, api);
        api.setScheduler(customDayScheduler);
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
        worldDayStates.clear();
        customDayScheduler.resetTimers();
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

        double speed = Math.max(0.0001D, settings.speedMultiplier());
        double effectiveLength = configuredLength / speed;
        double incrementPerTick = HALF_DAY_TICKS / Math.max(1.0D, effectiveLength);
        double nextTime = (currentTime + incrementPerTick) % FULL_DAY_TICKS;
        trackedTime.put(world.getUID(), nextTime);
        world.setTime((long) nextTime);
        detectPhaseChange(world, isDay, nextTime);
        customDayScheduler.tickWorld(world);
    }

    public boolean triggerCustomDay(CustomDayRegistration registration, World world) {
        return customDayScheduler.triggerNow(registration, world);
    }

    public CustomDayScheduler getCustomDayScheduler() {
        return customDayScheduler;
    }

    private void detectPhaseChange(World world, boolean wasDay, double nextTime) {
        boolean nowDay = nextTime % FULL_DAY_TICKS < HALF_DAY_TICKS;
        Boolean lastState = worldDayStates.put(world.getUID(), nowDay);
        boolean previousDayState = lastState != null ? lastState : wasDay;

        if (previousDayState != nowDay) {
            DayPhaseChangeEvent.Phase phase =
                    nowDay ? DayPhaseChangeEvent.Phase.DAY : DayPhaseChangeEvent.Phase.NIGHT;
            plugin.getServer().getPluginManager().callEvent(new DayPhaseChangeEvent(world, phase));
        }
    }
}
