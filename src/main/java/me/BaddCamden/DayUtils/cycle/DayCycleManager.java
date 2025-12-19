package me.BaddCamden.DayUtils.cycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import me.BaddCamden.DayUtils.config.CustomDayType;
import me.BaddCamden.DayUtils.config.DaySettings;
import me.BaddCamden.DayUtils.config.DayUtilsConfiguration;
import me.BaddCamden.DayUtils.api.event.CustomDayTriggerEvent;
import me.BaddCamden.DayUtils.api.event.DayPhaseChangeEvent;
import me.BaddCamden.DayUtils.api.event.TimeTickEvent;
import me.BaddCamden.DayUtils.api.event.CustomDayEvent;
import me.BaddCamden.DayUtils.integration.SessionLibraryHook;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.Plugin;

/**
 * Controls world time progression and emits tick events with calculated state.
 */
public class DayCycleManager {

    private final Plugin plugin;
    private BukkitTask tickTask;
    private DayUtilsConfiguration configuration;
    private final Map<UUID, WorldCycleState> worldStates = new HashMap<>();
    private final SessionLibraryHook sessionLibraryHook;

    public DayCycleManager(Plugin plugin, DayUtilsConfiguration configuration) {
        this.plugin = plugin;
        this.configuration = configuration;
        this.sessionLibraryHook = new SessionLibraryHook(plugin);
    }

    public void start() {
        stop();
        this.worldStates.clear();
        for (World world : Bukkit.getWorlds()) {
            initWorld(world);
        }
        this.tickTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void stop() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        sessionLibraryHook.resetResolution();
        worldStates.values().forEach(WorldCycleState::restoreDefaults);
        worldStates.clear();
    }

    public void updateConfiguration(DayUtilsConfiguration configuration) {
        this.configuration = configuration;
        worldStates.values().forEach(state -> state.updateSettings(configuration.getDaySettings()));
    }

    public DayUtilsConfiguration getConfiguration() {
        return configuration;
    }

    public CycleSnapshot snapshot(World world) {
        WorldCycleState state = worldStates.computeIfAbsent(world.getUID(),
            id -> new WorldCycleState(world, configuration.getDaySettings()));
        return state == null ? null : state.snapshot();
    }

    public Map<UUID, CycleSnapshot> snapshots() {
        Map<UUID, CycleSnapshot> snapshots = new HashMap<>();
        worldStates.forEach((id, state) -> snapshots.put(id, state.snapshot()));
        return snapshots;
    }

    public boolean triggerCustomDay(World world, String type) {
        WorldCycleState state = worldStates.get(world.getUID());
        return state != null && state.triggerCustomDay(type);
    }

    private void initWorld(World world) {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        worldStates.put(world.getUID(), new WorldCycleState(world, configuration.getDaySettings()));
    }

    private void tick() {
        if (!sessionLibraryHook.shouldAdvanceTime()) {
            return;
        }
        for (World world : Bukkit.getWorlds()) {
            worldStates.computeIfAbsent(world.getUID(), id -> new WorldCycleState(world, configuration.getDaySettings()));
            WorldCycleState state = worldStates.get(world.getUID());
            TickResult result = state.tick();
            TimeTickEvent event = new TimeTickEvent(world, result.isDay(), result.isNight(),
                result.dayPercent(), result.nightPercent(), result.cyclePercent(),
                result.customProgress());
            Bukkit.getPluginManager().callEvent(event);
            if (result.phaseChange() != null) {
                Bukkit.getPluginManager().callEvent(new DayPhaseChangeEvent(world, result.phaseChange()));
            }
            for (CustomDayType triggered : result.triggeredCustomTypes()) {
                Bukkit.getPluginManager().callEvent(new CustomDayTriggerEvent(world, triggered));
                Bukkit.getPluginManager().callEvent(new CustomDayEvent(world, triggered, CustomDayEvent.Phase.START));
            }
        }
    }

    private static class WorldCycleState {
        private final World world;
        private DaySettings settings;
        private double cycleProgress;
        private final Map<String, CustomDayProgress> customProgress = new HashMap<>();
        private Boolean lastDayState;

        WorldCycleState(World world, DaySettings settings) {
            this.world = world;
            this.settings = settings;
            this.cycleProgress = 0;
            settings.getCustomTypes().values()
                .forEach(type -> customProgress.put(type.getName().toLowerCase(Locale.ROOT),
                    new CustomDayProgress(type)));
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        }

        void updateSettings(DaySettings newSettings) {
            this.settings = newSettings;
            customProgress.keySet().removeIf(key -> !newSettings.getCustomTypes().containsKey(key));
            newSettings.getCustomTypes().values()
                .forEach(type -> {
                    String key = type.getName().toLowerCase(Locale.ROOT);
                    CustomDayProgress progress = customProgress.get(key);
                    if (progress == null) {
                        customProgress.put(key, new CustomDayProgress(type));
                    } else {
                        progress.updateType(type);
                    }
                });
        }

        TickResult tick() {
            double cycleLength = settings.getDayLength() + settings.getNightLength();
            double speed = settings.getSpeedMultiplier();
            cycleProgress += speed;
            while (cycleProgress >= cycleLength) {
                cycleProgress -= cycleLength;
            }

            boolean isDay = cycleProgress < settings.getDayLength();
            boolean isNight = !isDay;
            double dayPercent = isDay ? (cycleProgress / settings.getDayLength()) : 1.0d;
            double nightPercent = isDay ? 0.0d : ((cycleProgress - settings.getDayLength()) / settings.getNightLength());
            double cyclePercent = cycleProgress / cycleLength;

            DayPhaseChangeEvent.Phase phaseChange = null;
            if (lastDayState != null && lastDayState != isDay) {
                phaseChange = isDay ? DayPhaseChangeEvent.Phase.DAY : DayPhaseChangeEvent.Phase.NIGHT;
            }
            lastDayState = isDay;

            // Map to vanilla time
            long worldTime = Math.round(cyclePercent * 24000.0d) % 24000L;
            world.setFullTime(world.getFullTime() - (world.getFullTime() % 24000L) + worldTime);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

            Map<String, Double> customPercentages = new HashMap<>();
            List<CustomDayType> triggeredTypes = new ArrayList<>();
            for (CustomDayProgress progress : customProgress.values()) {
                double value = progress.advance(speed);
                if (progress.shouldTrigger()) {
                    triggeredTypes.add(progress.type());
                    progress.reset();
                    value = progress.progress();
                }
                customPercentages.put(progress.type().getName().toLowerCase(Locale.ROOT), value);
            }

            return new TickResult(isDay, isNight, clamp(dayPercent), clamp(nightPercent), clamp(cyclePercent),
                customPercentages, phaseChange, triggeredTypes);
        }

        CycleSnapshot snapshot() {
            double cycleLength = settings.getDayLength() + settings.getNightLength();
            boolean isDay = cycleProgress < settings.getDayLength();
            double cyclePercent = cycleProgress / cycleLength;
            double dayPercent = isDay ? (cycleProgress / settings.getDayLength()) : 1.0d;
            double nightPercent = isDay ? 0.0d : ((cycleProgress - settings.getDayLength()) / settings.getNightLength());
            Map<String, Double> customPercentages = new HashMap<>();
            for (Map.Entry<String, CustomDayProgress> entry : customProgress.entrySet()) {
                customPercentages.put(entry.getKey(), entry.getValue().progress());
            }
            return new CycleSnapshot(world, isDay, !isDay, clamp(dayPercent), clamp(nightPercent),
                clamp(cyclePercent), customPercentages, settings);
        }

        void restoreDefaults() {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        }

        boolean triggerCustomDay(String type) {
            CustomDayProgress progress = customProgress.get(type.toLowerCase(Locale.ROOT));
            if (progress == null) {
                return false;
            }
            Bukkit.getPluginManager().callEvent(new CustomDayTriggerEvent(world, progress.type()));
            Bukkit.getPluginManager().callEvent(new CustomDayEvent(world, progress.type(), CustomDayEvent.Phase.START));
            progress.reset();
            return true;
        }

        private double clamp(double value) {
            return Math.max(0.0d, Math.min(1.0d, value));
        }
    }

    private static class CustomDayProgress {
        private CustomDayType type;
        private double elapsed;

        CustomDayProgress(CustomDayType type) {
            this.type = type;
            this.elapsed = 0.0d;
        }

        void updateType(CustomDayType type) {
            this.type = type;
            this.elapsed = Math.min(this.elapsed, type.getIntervalTicks());
        }

        double advance(double step) {
            elapsed += step;
            return progress();
        }

        boolean shouldTrigger() {
            return elapsed >= type.getIntervalTicks();
        }

        void reset() {
            elapsed = 0.0d;
        }

        double progress() {
            return Math.min(1.0d, elapsed / type.getIntervalTicks());
        }

        CustomDayType type() {
            return type;
        }
    }

    private record TickResult(boolean isDay, boolean isNight, double dayPercent,
                              double nightPercent, double cyclePercent,
                              Map<String, Double> customProgress,
                              DayPhaseChangeEvent.Phase phaseChange,
                              List<CustomDayType> triggeredCustomTypes) {
    }
}
