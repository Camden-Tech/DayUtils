package me.BaddCamden.DayUtils;

import java.util.function.Supplier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class DayCycleListener implements Listener {
    private final Supplier<DaySettings> settingsSupplier;

    public DayCycleListener(Supplier<DaySettings> settingsSupplier) {
        this.settingsSupplier = settingsSupplier;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        DaySettings settings = settingsSupplier.get();
        event.getPlayer().sendMessage(
                "Day cycle lengths - Day: " + settings.dayLengthTicks() + " ticks, Night: " + settings.nightLengthTicks());
    }
}
