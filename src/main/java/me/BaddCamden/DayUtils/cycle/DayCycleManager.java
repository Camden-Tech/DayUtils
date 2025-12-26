package me.BaddCamden.DayUtils.cycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import me.BaddCamden.DayUtils.DayUtilsPlugin;
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

/**
 * Controls world time progression and emits tick events with calculated state.
 */
public class DayCycleManager {

    private final DayUtilsPlugin plugin;
    private BukkitTask tickTask;
    private DayUtilsConfiguration configuration;
    private final Map<UUID, WorldCycleState> worldStates = new HashMap<>();
    private final SessionLibraryHook sessionLibraryHook;

    /**
     * Builds a cycle manager responsible for advancing time in all worlds.
     *
     * @param plugin the owning plugin
     * @param configuration the initial configuration to apply
     */
    public DayCycleManager(DayUtilsPlugin plugin, DayUtilsConfiguration configuration) {
        this.plugin = plugin;
        this.configuration = configuration;
        this.sessionLibraryHook = new SessionLibraryHook(plugin);
    }

    /**
     * Starts ticking all loaded worlds, resetting state to match the current configuration.
     */
    public void start() {
        stop();
        this.worldStates.clear();
        for (World world : Bukkit.getWorlds()) {
            initWorld(world);
        }
        this.tickTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    /**
     * Cancels ticking and restores vanilla daylight behaviour to all tracked worlds.
     */
    public void stop() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        sessionLibraryHook.resetResolution();
        worldStates.values().forEach(WorldCycleState::restoreDefaults);
        worldStates.clear();
    }

    /**
     * Applies a refreshed configuration to existing world states.
     *
     * @param configuration the updated configuration snapshot
     */
    public void updateConfiguration(DayUtilsConfiguration configuration) {
        this.configuration = configuration;
        worldStates.values().forEach(state -> state.updateSettings(configuration.getDaySettings()));
    }

    /**
     * Returns the current configuration driving the manager.
     */
    public DayUtilsConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Produces a snapshot of the specified world's time state.
     *
     * @param world the world to inspect
     * @return a snapshot or {@code null} if the world is unknown
     */
    public CycleSnapshot snapshot(World world) {
        WorldCycleState state = stateFor(world);
        return state == null ? null : state.snapshot();
    }

    /**
     * Produces snapshots for every tracked world keyed by world UUID.
     */
    public Map<UUID, CycleSnapshot> snapshots() {
        Map<UUID, CycleSnapshot> snapshots = new HashMap<>();
        worldStates.forEach((id, state) -> snapshots.put(id, state.snapshot()));
        return snapshots;
    }

    /**
     * Manually triggers a custom day type in the given world.
     *
     * @param world the world to modify
     * @param type the custom day identifier
     * @return true if the type was recognised and triggered
     */
    public boolean triggerCustomDay(World world, String type) {
        WorldCycleState state = worldStates.get(world.getUID());
        return state != null && state.triggerCustomDay(type);
    }

    /**
     * Sets the stored nights-passed count for a world, clamping negative values.
     *
     * @param world the world to update
     * @param nightsPassed new nights passed value
     * @return true when the world was found and updated
     */
    public boolean setNightsPassed(World world, long nightsPassed) {
        if (world == null) {
            return false;
        }
        WorldCycleState state = stateFor(world);
        if (state == null) {
            return false;
        }
        state.setNightsPassed(Math.max(0L, nightsPassed));
        persistNightsPassed(world, state.nightsPassed());
        return true;
    }

    /**
     * Prepares a world for managed ticking by disabling vanilla daylight cycle and tracking state.
     */
    private void initWorld(World world) {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        worldStates.put(world.getUID(), createState(world));
    }

    /**
     * Executes a tick for each world if time advancement is permitted.
     */
    private void tick() {
        if (!sessionLibraryHook.shouldAdvanceTime()) {
            return;
        }
        for (World world : Bukkit.getWorlds()) {
            WorldCycleState state = stateFor(world);
            TickResult result = state.tick();
            TimeTickEvent event = new TimeTickEvent(world, result.isDay(), result.isNight(),
                result.dayPercent(), result.nightPercent(), result.cyclePercent(),
                result.customProgress(), result.nightsPassed());
            Bukkit.getPluginManager().callEvent(event);
            if (result.phaseChange() != null) {
                Bukkit.getPluginManager().callEvent(new DayPhaseChangeEvent(world, result.phaseChange(),
                    result.nightsPassed()));
            }
            if (result.nightCompleted()) {
                persistNightsPassed(world, result.nightsPassed());
            }
            for (CustomDayType triggered : result.triggeredCustomTypes()) {
                Bukkit.getPluginManager().callEvent(new CustomDayTriggerEvent(world, triggered));
                Bukkit.getPluginManager().callEvent(new CustomDayEvent(world, triggered, CustomDayEvent.Phase.START));
            }
        }
    }

    /**
     * Creates a new world cycle state with persisted progress restored.
     */
    private WorldCycleState createState(World world) {
        long nightsPassed = loadNightsPassed(world);
        return new WorldCycleState(world, configuration.getDaySettings(), nightsPassed);
    }

    /**
     * Retrieves or creates the managed state object for the given world.
     */
    private WorldCycleState stateFor(World world) {
        return worldStates.computeIfAbsent(world.getUID(), id -> createState(world));
    }

    /**
     * Reads the persisted nights-passed value for a world from configuration.
     */
    private long loadNightsPassed(World world) {
        return plugin.getConfig().getLong(statePath(world), 0L);
    }

    /**
     * Writes the nights-passed value to configuration and marks it dirty for saving.
     */
    private void persistNightsPassed(World world, long nightsPassed) {
        plugin.getConfig().set(statePath(world), nightsPassed);
        plugin.markConfigDirty();
    }

    /**
     * Builds the configuration path used to store state for a world.
     */
    private String statePath(World world) {
        return "state." + world.getUID() + ".nightsPassed";
    }

    private static class WorldCycleState {
        private final World world;
        private DaySettings settings;
        private double cycleProgress;
        private final Map<String, CustomDayProgress> customProgress = new HashMap<>();
        private Boolean lastDayState;
        private long nightsPassed;

        /**
         * Creates managed state for a single world with the given settings and progress.
         */
        WorldCycleState(World world, DaySettings settings, long nightsPassed) {
            this.world = world;
            this.settings = settings;
            this.cycleProgress = 0;
            this.nightsPassed = Math.max(0L, nightsPassed);
            settings.getCustomTypes().values()
                .forEach(type -> customProgress.put(type.getName().toLowerCase(Locale.ROOT),
                    new CustomDayProgress(type)));
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        }

        /**
         * Updates the state to reflect new settings, adding or removing custom day trackers as needed.
         */
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

        /**
         * Advances the world's cycle progress and returns the calculated tick result payload.
         */
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
            Boolean previousDayState = lastDayState;
            if (previousDayState != null && previousDayState != isDay) {
                phaseChange = isDay ? DayPhaseChangeEvent.Phase.DAY : DayPhaseChangeEvent.Phase.NIGHT;
            }
            lastDayState = isDay;
            boolean nightCompleted = previousDayState != null && !previousDayState && isDay;
            if (nightCompleted) {
                nightsPassed++;
            }

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
                customPercentages, nightsPassed, nightCompleted, phaseChange, triggeredTypes);
        }

        /**
         * Builds a snapshot of the current world state without mutating progress.
         */
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
                clamp(cyclePercent), customPercentages, settings, nightsPassed);
        }

        /**
         * Re-enables vanilla daylight cycle for this world.
         */
        void restoreDefaults() {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        }

        /**
         * Fires a custom day event immediately for the supplied type key.
         */
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

        /**
         * Overrides the nights passed counter using a clamped non-negative value.
         */
        void setNightsPassed(long nightsPassed) {
            this.nightsPassed = Math.max(0L, nightsPassed);
        }

        /**
         * Returns the number of nights that have completed for this world.
         */
        long nightsPassed() {
            return nightsPassed;
        }

        /**
         * Restricts a value to the inclusive range 0.0-1.0.
         */
        private double clamp(double value) {
            return Math.max(0.0d, Math.min(1.0d, value));
        }
    }

    private static class CustomDayProgress {
        private CustomDayType type;
        private double elapsed;

        /**
         * Tracks progress toward the next trigger for a custom day type.
         */
        CustomDayProgress(CustomDayType type) {
            this.type = type;
            this.elapsed = 0.0d;
        }

        /**
         * Refreshes the tracked type, ensuring current progress respects the new interval.
         */
        void updateType(CustomDayType type) {
            this.type = type;
            this.elapsed = Math.min(this.elapsed, type.getIntervalTicks());
        }

        /**
         * Advances elapsed progress by the given step and returns the new progress ratio.
         */
        double advance(double step) {
            elapsed += step;
            return progress();
        }

        /**
         * Determines whether the accumulated elapsed time is ready to trigger the custom day.
         */
        boolean shouldTrigger() {
            return elapsed >= type.getIntervalTicks();
        }

        /**
         * Resets elapsed progress back to zero after a trigger.
         */
        void reset() {
            elapsed = 0.0d;
        }

        /**
         * Returns progress toward the next trigger as a ratio between 0 and 1.
         */
        double progress() {
            return Math.min(1.0d, elapsed / type.getIntervalTicks());
        }

        /**
         * Retrieves the custom day type being tracked.
         */
        CustomDayType type() {
            return type;
        }
    }

    private record TickResult(boolean isDay, boolean isNight, double dayPercent,
                              double nightPercent, double cyclePercent,
                              Map<String, Double> customProgress,
                              long nightsPassed, boolean nightCompleted,
                              DayPhaseChangeEvent.Phase phaseChange,
                              List<CustomDayType> triggeredCustomTypes) {
    }
}
