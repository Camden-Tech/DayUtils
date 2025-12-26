package me.BaddCamden.DayUtils.command;

import java.util.Locale;
import java.util.Map;
import me.BaddCamden.DayUtils.DayUtilsPlugin;
import me.BaddCamden.DayUtils.api.DayStatus;
import me.BaddCamden.DayUtils.config.DayUtilsConfiguration;
import me.BaddCamden.DayUtils.config.MessageSettings;
import me.BaddCamden.DayUtils.config.CustomDayType;
import me.BaddCamden.DayUtils.config.CommandSettings;
import me.BaddCamden.DayUtils.config.DaySettings;
import me.BaddCamden.DayUtils.config.SettingsConstraints;
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

    /**
     * Creates a command handler bound to the provided plugin instance.
     *
     * @param plugin the owning plugin
     */
    public DayUtilsCommand(DayUtilsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Routes the /dayutils command to the appropriate sub-command implementation.
     */
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
                return handleReload(sender, messages);
            }
            case "trigger" -> {
                return handleTrigger(sender, label, args, permissions, messages, manager);
            }
            case "setdaylength" -> {
                return handleSetDayLength(sender, args, permissions, messages, configuration);
            }
            case "setnightlength" -> {
                return handleSetNightLength(sender, args, permissions, messages, configuration);
            }
            case "setspeed" -> {
                return handleSetSpeed(sender, args, permissions, messages, configuration);
            }
            case "status" -> {
                return handleStatus(sender, args, messages, manager);
            }
            case "setnightspassed" -> {
                return handleSetNightsPassed(sender, args, permissions, messages, manager);
            }
            default -> {
                sender.sendMessage(colour(messages.getUsage().replace("{label}", label)));
                return true;
            }
        }
    }

    /**
     * Reloads configuration when the sender has permission and notifies them of the result.
     */
    private boolean handleReload(CommandSender sender, MessageSettings messages) {
        CommandSettings permissions = plugin.getConfigurationModel().getCommandSettings();
        if (!sender.hasPermission(permissions.getReloadPermission())) {
            sender.sendMessage(colour(messages.getReload().noPermission()));
            return true;
        }
        plugin.reloadConfigurationModel();
        sender.sendMessage(colour(messages.getReload().success()));
        return true;
    }

    /**
     * Triggers a custom day cycle type for a world, validating permissions and arguments.
     */
    private boolean handleTrigger(CommandSender sender, String label, String[] args, CommandSettings permissions,
                                  MessageSettings messages, DayCycleManager manager) {
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

    /**
     * Updates the configured day length when the sender provides a valid tick value.
     */
    private boolean handleSetDayLength(CommandSender sender, String[] args, CommandSettings permissions,
                                       MessageSettings messages, DayUtilsConfiguration configuration) {
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
        if (!validateBounds(value, SettingsConstraints.MIN_LENGTH_TICKS, SettingsConstraints.MAX_LENGTH_TICKS)) {
            sender.sendMessage(colour(messages.getSetDayLength().outOfBounds()
                .replace("{min}", String.valueOf(SettingsConstraints.MIN_LENGTH_TICKS))
                .replace("{max}", String.valueOf(SettingsConstraints.MAX_LENGTH_TICKS))));
            return true;
        }

        DaySettings current = configuration.getDaySettings();
        DaySettings updated = new DaySettings(value, current.getNightLength(), current.getSpeedMultiplier(),
            current.getCustomTypes());
        plugin.updateDaySettings(updated);

        sender.sendMessage(colour(messages.getSetDayLength().success()
            .replace("{ticks}", value.toString())));
        return true;
    }

    /**
     * Updates the configured night length when the sender provides a valid tick value.
     */
    private boolean handleSetNightLength(CommandSender sender, String[] args, CommandSettings permissions,
                                         MessageSettings messages, DayUtilsConfiguration configuration) {
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
        if (!validateBounds(value, SettingsConstraints.MIN_LENGTH_TICKS, SettingsConstraints.MAX_LENGTH_TICKS)) {
            sender.sendMessage(colour(messages.getSetNightLength().outOfBounds()
                .replace("{min}", String.valueOf(SettingsConstraints.MIN_LENGTH_TICKS))
                .replace("{max}", String.valueOf(SettingsConstraints.MAX_LENGTH_TICKS))));
            return true;
        }

        DaySettings current = configuration.getDaySettings();
        DaySettings updated = new DaySettings(current.getDayLength(), value, current.getSpeedMultiplier(),
            current.getCustomTypes());
        plugin.updateDaySettings(updated);

        sender.sendMessage(colour(messages.getSetNightLength().success()
            .replace("{ticks}", value.toString())));
        return true;
    }

    /**
     * Adjusts the time progression speed multiplier when supplied within valid bounds.
     */
    private boolean handleSetSpeed(CommandSender sender, String[] args, CommandSettings permissions,
                                   MessageSettings messages, DayUtilsConfiguration configuration) {
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
        if (multiplier < SettingsConstraints.MIN_SPEED || multiplier > SettingsConstraints.MAX_SPEED) {
            sender.sendMessage(colour(messages.getSetSpeed().outOfBounds()
                .replace("{min}", String.valueOf(SettingsConstraints.MIN_SPEED))
                .replace("{max}", String.valueOf(SettingsConstraints.MAX_SPEED))));
            return true;
        }

        DaySettings current = configuration.getDaySettings();
        DaySettings updated = new DaySettings(current.getDayLength(), current.getNightLength(), multiplier,
            current.getCustomTypes());
        plugin.updateDaySettings(updated);

        sender.sendMessage(colour(messages.getSetSpeed().success()
            .replace("{multiplier}", multiplier.toString())));
        return true;
    }

    /**
     * Overrides the recorded nights passed count for a world if the sender provides a valid number.
     */
    private boolean handleSetNightsPassed(CommandSender sender, String[] args, CommandSettings permissions,
                                          MessageSettings messages, DayCycleManager manager) {
        if (!sender.hasPermission(permissions.getSetNightsPassedPermission())) {
            sender.sendMessage(colour(messages.getSetNightsPassed().noPermission()));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(colour(messages.getSetNightsPassed().usage()));
            return true;
        }
        Long value = parseLong(args[1]);
        if (value == null) {
            sender.sendMessage(colour(messages.getSetNightsPassed().notNumber()));
            return true;
        }
        if (value < 0) {
            sender.sendMessage(colour(messages.getSetNightsPassed().outOfBounds()));
            return true;
        }

        World world = resolveWorld(sender, args.length > 2 ? args[2] : null);
        if (world == null) {
            sender.sendMessage(colour(messages.getSetNightsPassed().worldMissing()));
            return true;
        }

        manager.setNightsPassed(world, value);
        sender.sendMessage(colour(messages.getSetNightsPassed().success()
            .replace("{count}", value.toString())
            .replace("{world}", world.getName())));
        return true;
    }

    /**
     * Reports current day/night status and custom progress for the selected world.
     */
    private boolean handleStatus(CommandSender sender, String[] args, MessageSettings messages,
                                 DayCycleManager manager) {
        World world = resolveWorld(sender, args.length > 1 ? args[1] : null);
        if (world == null) {
            sender.sendMessage(colour(messages.getStatus().worldMissing()));
            return true;
        }

        DayStatus status = plugin.getApi().status(world);
        if (status == null) {
            sender.sendMessage(colour(messages.getStatus().unavailable()
                .replace("{world}", world.getName())));
            return true;
        }

        sender.sendMessage(colour(messages.getStatus().nightsPassed()
            .replace("{world}", world.getName())
            .replace("{count}", String.valueOf(status.getNightsPassed()))));
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

    /**
     * Supplies tab-completion candidates for /dayutils based on the entered arguments.
     */
    @Override
    public @Nullable java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                          @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return java.util.List.of("reload", "trigger", "setdaylength", "setnightlength", "setspeed",
                "setnightspassed", "status");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("trigger")) {
            return java.util.List.copyOf(plugin.getConfigurationModel().getDaySettings().getCustomTypes().keySet());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("status")) {
            return Bukkit.getWorlds().stream().map(World::getName).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("setnightspassed")) {
            return Bukkit.getWorlds().stream().map(World::getName).toList();
        }
        return java.util.Collections.emptyList();
    }

    /**
     * Translates legacy colour codes using Bukkit's ChatColor.
     */
    private String colour(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    /**
     * Confirms that a provided long value falls within the given inclusive bounds.
     */
    private boolean validateBounds(long value, long min, long max) {
        return value >= min && value <= max;
    }

    /**
     * Attempts to parse a long, returning null when the input is not numeric.
     */
    private Long parseLong(String input) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Attempts to parse a double, returning null when the input is not numeric.
     */
    private Double parseDouble(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Resolves a target world from an optional name or falls back to the sender's world/default.
     */
    private World resolveWorld(CommandSender sender, @Nullable String name) {
        if (name != null && !name.isEmpty()) {
            return Bukkit.getWorld(name);
        }
        if (sender instanceof Player player) {
            return player.getWorld();
        }
        return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().getFirst();
    }

    /**
     * Formats a decimal value as a percentage with one decimal place.
     */
    private String percent(double value) {
        return String.format(Locale.US, "%.1f%%", value * 100.0d);
    }
}
