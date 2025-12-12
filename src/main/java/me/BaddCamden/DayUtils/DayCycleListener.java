package me.BaddCamden.DayUtils;

import java.util.function.Supplier;
import me.BaddCamden.DayUtils.api.DayInfoService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class DayCycleListener implements Listener {
    private final Supplier<DaySettings> settingsSupplier;
    private final DayInfoService dayInfoService;

    public DayCycleListener(Supplier<DaySettings> settingsSupplier, DayInfoService dayInfoService) {
        this.settingsSupplier = settingsSupplier;
        this.dayInfoService = dayInfoService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        DaySettings settings = settingsSupplier.get();
        String phase = resolvePhase(event);
        String progress = String.format("%.1f%%", dayInfoService.getFullCycleProgress(event.getPlayer().getWorld()) * 100);
        event.getPlayer()
                .sendMessage(
                        "Day cycle lengths - Day: "
                                + settings.dayLengthTicks()
                                + " ticks, Night: "
                                + settings.nightLengthTicks()
                                + " ticks, Speed: x"
                                + settings.speedMultiplier()
                                + ". Current phase: "
                                + phase
                                + " ("
                                + progress
                                + ")");
    }

    private String resolvePhase(PlayerJoinEvent event) {
        if (dayInfoService.isDay(event.getPlayer().getWorld())) {
            return "Day";
        }

        if (dayInfoService.isNight(event.getPlayer().getWorld())) {
            return "Night";
        }

        return "Custom";
    }
}
