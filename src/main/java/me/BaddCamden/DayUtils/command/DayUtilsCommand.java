package me.BaddCamden.DayUtils.command;

import java.util.Locale;
import java.util.Map;
import me.BaddCamden.DayUtils.DayUtilsPlugin;
import me.BaddCamden.DayUtils.config.DayUtilsConfiguration;
import me.BaddCamden.DayUtils.config.MessageSettings;
import me.BaddCamden.DayUtils.config.CustomDayType;
import me.BaddCamden.DayUtils.config.CommandSettings;
import me.BaddCamden.DayUtils.cycle.DayCycleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles the /dayutils command and subcommands.
 */
public class DayUtilsCommand implements CommandExecutor, TabCompleter {

    private final DayUtilsPlugin plugin;

    public DayUtilsCommand(DayUtilsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        DayUtilsConfiguration configuration = plugin.getConfigurationModel();
        MessageSettings messages = configuration.getMessageSettings();
        CommandSettings permissions = configuration.getCommandSettings();
        DayCycleManager manager = plugin.getCycleManager();

        if (args.length == 0) {
            sender.sendMessage(colour(messages.getUsage().replace("{label}", label)));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                if (!sender.hasPermission(permissions.getReloadPermission())) {
                    sender.sendMessage(colour(messages.getReload().noPermission()));
                    return true;
                }
                plugin.reloadConfigurationModel();
                sender.sendMessage(colour(messages.getReload().success()));
                return true;
            }
            case "trigger" -> {
                if (!sender.hasPermission(permissions.getTriggerPermission())) {
                    sender.sendMessage(colour(messages.getTrigger().noPermission()));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(colour(messages.getUsage().replace("{label}", label)));
                    return true;
                }
                String type = args[1].toLowerCase(Locale.ROOT);
                World world = resolveWorld(sender, args.length > 2 ? args[2] : null);
                if (world == null) {
                    sender.sendMessage(colour(messages.getTrigger().worldMissing()));
                    return true;
                }
                if (!manager.getConfiguration().getDaySettings().getCustomTypes().containsKey(type)) {
                    String available = String.join(", ",
                        manager.getConfiguration().getDaySettings().getCustomTypes().keySet());
                    sender.sendMessage(colour(messages.getTrigger().unknownType()
                        .replace("{type}", type)
                        .replace("{available}", available)));
                    return true;
                }
                manager.triggerCustomDay(world, type);
                sender.sendMessage(colour(messages.getTrigger().success()
                    .replace("{type}", type)
                    .replace("{world}", world.getName())));
                return true;
            }
            case "setdaylength" -> {
                if (!sender.hasPermission(permissions.getSetDayLengthPermission())) {
                    sender.sendMessage(colour(messages.getSetDayLength().noPermission()));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(colour(messages.getSetDayLength().usage()));
                    return true;
                }
                Long value = parseLong(args[1]);
                if (value == null) {
                    sender.sendMessage(colour(messages.getSetDayLength().notNumber()));
                    return true;
                }
                if (!validateBounds(value, 20, 240000)) {
                    sender.sendMessage(colour(messages.getSetDayLength().outOfBounds()
                        .replace("{min}", "20")
                        .replace("{max}", "240000")));
                    return true;
                }
                plugin.getConfig().set("day.length", value);
                plugin.saveConfig();
                plugin.reloadConfigurationModel();
                sender.sendMessage(colour(messages.getSetDayLength().success()
                    .replace("{ticks}", value.toString())));
                return true;
            }
            case "setnightlength" -> {
                if (!sender.hasPermission(permissions.getSetNightLengthPermission())) {
                    sender.sendMessage(colour(messages.getSetNightLength().noPermission()));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(colour(messages.getSetNightLength().usage()));
                    return true;
                }
                Long value = parseLong(args[1]);
                if (value == null) {
                    sender.sendMessage(colour(messages.getSetNightLength().notNumber()));
                    return true;
                }
                if (!validateBounds(value, 20, 240000)) {
                    sender.sendMessage(colour(messages.getSetNightLength().outOfBounds()
                        .replace("{min}", "20")
                        .replace("{max}", "240000")));
                    return true;
                }
                plugin.getConfig().set("day.nightLength", value);
                plugin.saveConfig();
                plugin.reloadConfigurationModel();
                sender.sendMessage(colour(messages.getSetNightLength().success()
                    .replace("{ticks}", value.toString())));
                return true;
            }
            case "setspeed" -> {
                if (!sender.hasPermission(permissions.getSetSpeedPermission())) {
                    sender.sendMessage(colour(messages.getSetSpeed().noPermission()));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(colour(messages.getSetSpeed().usage()));
                    return true;
                }
                Double multiplier = parseDouble(args[1]);
                if (multiplier == null) {
                    sender.sendMessage(colour(messages.getSetSpeed().notNumber()));
                    return true;
                }
                if (multiplier < 0.1d || multiplier > 10.0d) {
                    sender.sendMessage(colour(messages.getSetSpeed().outOfBounds()
                        .replace("{min}", "0.1")
                        .replace("{max}", "10")));
                    return true;
                }
                plugin.getConfig().set("day.speed", multiplier);
                plugin.saveConfig();
                plugin.reloadConfigurationModel();
                sender.sendMessage(colour(messages.getSetSpeed().success()
                    .replace("{multiplier}", multiplier.toString())));
                return true;
            }
            case "status" -> {
                World world = resolveWorld(sender, args.length > 1 ? args[1] : null);
                if (world == null) {
                    sender.sendMessage(colour(messages.getStatus().worldMissing()));
                    return true;
                }
                var status = plugin.getApi().status(world);
                if (status == null) {
                    sender.sendMessage(colour(messages.getStatus().unavailable()
                        .replace("{world}", world.getName())));
                    return true;
                }
                sender.sendMessage(colour(status.isDay()
                    ? messages.getStatus().day().replace("{world}", world.getName())
                    .replace("{progress}", percent(status.getDayPercent()))
                    : messages.getStatus().night().replace("{world}", world.getName())
                    .replace("{progress}", percent(status.getNightPercent()))));
                for (Map.Entry<String, Double> entry : status.getCustomPercent().entrySet()) {
                    CustomDayType type = manager.getConfiguration().getDaySettings().getCustomTypes().get(entry.getKey());
                    if (type == null) {
                        continue;
                    }
                    sender.sendMessage(colour(messages.getStatus().customDay()
                        .replace("{name}", type.getName())
                        .replace("{progress}", percent(entry.getValue()))
                        .replace("{world}", world.getName())));
                }
                return true;
            }
            default -> {
                sender.sendMessage(colour(messages.getUsage().replace("{label}", label)));
                return true;
            }
        }
    }

    @Override
    public @Nullable java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                          @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return java.util.List.of("reload", "trigger", "setdaylength", "setnightlength", "setspeed", "status");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("trigger")) {
            return java.util.List.copyOf(plugin.getConfigurationModel().getDaySettings().getCustomTypes().keySet());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("status")) {
            return Bukkit.getWorlds().stream().map(World::getName).toList();
        }
        return java.util.Collections.emptyList();
    }

    private String colour(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    private boolean validateBounds(long value, long min, long max) {
        return value >= min && value <= max;
    }

    private Long parseLong(String input) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private World resolveWorld(CommandSender sender, @Nullable String name) {
        if (name != null && !name.isEmpty()) {
            return Bukkit.getWorld(name);
        }
        if (sender instanceof Player player) {
            return player.getWorld();
        }
        return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().getFirst();
    }

    private String percent(double value) {
        return String.format(Locale.US, "%.1f%%", value * 100.0d);
    }
}
