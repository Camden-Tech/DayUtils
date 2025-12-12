package me.BaddCamden.DayUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import me.BaddCamden.DayUtils.api.CustomDayContext;
import me.BaddCamden.DayUtils.api.CustomDayEvent;
import me.BaddCamden.DayUtils.api.CustomDayType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

class CustomDayScheduler {
    private final JavaPlugin plugin;
    private final DayUtilsApiImpl api;
    private final Map<java.util.UUID, Map<String, Long>> timers = new HashMap<>();

    CustomDayScheduler(JavaPlugin plugin, DayUtilsApiImpl api) {
        this.plugin = plugin;
        this.api = api;
    }

    void tickWorld(World world) {
        Map<String, Long> worldTimers = timers.computeIfAbsent(world.getUID(), id -> new HashMap<>());
        Set<String> seen = new HashSet<>();

        for (CustomDayRegistration registration : api.getRegistrations()) {
            String id = registration.type().id();
            seen.add(id);
            long remaining = worldTimers.getOrDefault(id, registration.intervalTicks());
            if (remaining <= 0) {
                triggerNow(registration, world);
                worldTimers.put(id, registration.intervalTicks());
            } else {
                worldTimers.put(id, remaining - 1);
            }
        }

        worldTimers.keySet().removeIf(id -> !seen.contains(id));
    }

    boolean triggerNow(CustomDayRegistration registration, World world) {
        CustomDayType type = registration.type();
        CustomDayContext context = new CustomDayContext(plugin, world, type);
        PluginManager pluginManager = Bukkit.getPluginManager();
        type.onStart(context);
        pluginManager.callEvent(new CustomDayEvent(world, type, CustomDayEvent.Phase.START));

        long duration = Math.max(1L, type.durationTicks());
        plugin.getServer()
                .getScheduler()
                .runTaskLater(plugin, () -> endCustomDay(type, world), duration);

        for (Player player : world.getPlayers()) {
            type.effects().forEach(player::addPotionEffect);
        }
        timers.computeIfAbsent(world.getUID(), id -> new HashMap<>())
                .put(type.id(), registration.intervalTicks());
        return true;
    }

    private void endCustomDay(CustomDayType type, World world) {
        CustomDayContext context = new CustomDayContext(plugin, world, type);
        type.onEnd(context);
        Bukkit.getPluginManager().callEvent(new CustomDayEvent(world, type, CustomDayEvent.Phase.END));
    }

    void resetTimers() {
        timers.clear();
    }
}
