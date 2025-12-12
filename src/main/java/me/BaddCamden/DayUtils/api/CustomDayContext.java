package me.BaddCamden.DayUtils.api;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

/**
 * Context passed to custom day lifecycle callbacks.
 */
public record CustomDayContext(Plugin plugin, World world, CustomDayType dayType) {
}
