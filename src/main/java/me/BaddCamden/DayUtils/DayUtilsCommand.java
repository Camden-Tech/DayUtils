package me.BaddCamden.DayUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import me.BaddCamden.DayUtils.api.CustomDayType;
import me.BaddCamden.DayUtils.api.DayInfoService;
import me.BaddCamden.DayUtils.api.DayUtilsApi;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class DayUtilsCommand implements CommandExecutor, TabCompleter {
    private static final long MIN_LENGTH_TICKS = 1L;
    private static final long MAX_LENGTH_TICKS = 240000L;
    private static final double MIN_SPEED = 0.1D;
    private static final double MAX_SPEED = 10.0D;

    private final DayUtilsPlugin plugin;
    private final DayInfoService dayInfoService;

    public DayUtilsCommand(DayUtilsPlugin plugin, DayInfoService dayInfoService) {
        this.plugin = plugin;
        this.dayInfoService = dayInfoService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            return handleStatus(sender, args);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("setdaylength")) {
            return handleSetDayLength(sender, args);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("setnightlength")) {
            return handleSetNightLength(sender, args);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("setspeed")) {
            return handleSetSpeed(sender, args);
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("trigger")) {
            return handleTrigger(sender, args);
        }

        sender.sendMessage(ChatColor.YELLOW
                + "DayUtils usage: /"
                + label
                + " reload | /"
                + label
                + " trigger <type> [world] | /"
                + label
                + " setdaylength <ticks> | /"
                + label
                + " setnightlength <ticks> | /"
                + label
                + " setspeed <0.1-10> | /"
                + label
                + " status [world]");
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        DaySettings settings = plugin.getSettings();
        if (!sender.hasPermission(settings.reloadPermission())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to reload DayUtils.");
            return true;
        }

        plugin.reloadDaySettings();
        sender.sendMessage(ChatColor.GREEN + "DayUtils configuration reloaded.");
        return true;
    }

    private boolean handleSetDayLength(CommandSender sender, String[] args) {
        DaySettings settings = plugin.getSettings();
        if (!sender.hasPermission(settings.setDayLengthPermission())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to set the day length.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /dayutils setdaylength <ticks>");
            return true;
        }

        Long value = parseLong(args[1]);
        if (value == null) {
            sender.sendMessage(ChatColor.RED + "Day length must be a number of ticks.");
            return true;
        }

        if (value < MIN_LENGTH_TICKS || value > MAX_LENGTH_TICKS) {
            sender.sendMessage(
                    ChatColor.RED
                            + "Day length must be between "
                            + MIN_LENGTH_TICKS
                            + " and "
                            + MAX_LENGTH_TICKS
                            + " ticks.");
            return true;
        }

        plugin.updateDayLength(value);
        sender.sendMessage(ChatColor.GREEN + "Day length updated to " + value + " ticks.");
        return true;
    }

    private boolean handleSetNightLength(CommandSender sender, String[] args) {
        DaySettings settings = plugin.getSettings();
        if (!sender.hasPermission(settings.setNightLengthPermission())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to set the night length.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /dayutils setnightlength <ticks>");
            return true;
        }

        Long value = parseLong(args[1]);
        if (value == null) {
            sender.sendMessage(ChatColor.RED + "Night length must be a number of ticks.");
            return true;
        }

        if (value < MIN_LENGTH_TICKS || value > MAX_LENGTH_TICKS) {
            sender.sendMessage(
                    ChatColor.RED
                            + "Night length must be between "
                            + MIN_LENGTH_TICKS
                            + " and "
                            + MAX_LENGTH_TICKS
                            + " ticks.");
            return true;
        }

        plugin.updateNightLength(value);
        sender.sendMessage(ChatColor.GREEN + "Night length updated to " + value + " ticks.");
        return true;
    }

    private boolean handleSetSpeed(CommandSender sender, String[] args) {
        DaySettings settings = plugin.getSettings();
        if (!sender.hasPermission(settings.setSpeedPermission())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to change the day speed.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /dayutils setspeed <multiplier>");
            return true;
        }

        Double value = parseDouble(args[1]);
        if (value == null) {
            sender.sendMessage(ChatColor.RED + "Speed must be a decimal value.");
            return true;
        }

        if (value < MIN_SPEED || value > MAX_SPEED) {
            sender.sendMessage(
                    ChatColor.RED
                            + "Speed multiplier must be between "
                            + MIN_SPEED
                            + " and "
                            + MAX_SPEED
                            + ".");
            return true;
        }

        plugin.updateSpeed(value);
        sender.sendMessage(ChatColor.GREEN + "Day/night speed multiplier updated to " + value + ".");
        return true;
    }

    private boolean handleTrigger(CommandSender sender, String[] args) {
        DaySettings settings = plugin.getSettings();
        if (!sender.hasPermission(settings.triggerPermission())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to trigger custom days.");
            return true;
        }

        String typeId = args[1];
        World world = resolveWorld(sender, args.length >= 3 ? args[2] : null);
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "Unable to resolve world to trigger the event.");
            return true;
        }

        DayUtilsApi api = plugin.getApi();
        boolean triggered = api.triggerCustomDay(typeId, world);
        if (triggered) {
            sender.sendMessage(ChatColor.GREEN + "Triggered custom day '" + typeId + "' in " + world.getName());
        } else {
            sender.sendMessage(
                    ChatColor.RED + "Unknown custom day '" + typeId + "'. Available: " + describeRegistered(api));
        }
        return true;
    }

    private World resolveWorld(CommandSender sender, String worldName) {
        if (worldName != null) {
            return plugin.getServer().getWorld(worldName);
        }

        if (sender instanceof Player player) {
            return player.getWorld();
        }

        return plugin.getServer().getWorlds().stream().findFirst().orElse(null);
    }

    private String describeRegistered(DayUtilsApi api) {
        Collection<CustomDayType> registered = api.getRegisteredCustomDayTypes();
        if (registered.isEmpty()) {
            return "(none registered)";
        }

        return registered.stream().map(CustomDayType::id).collect(Collectors.joining(", "));
    }

    private boolean handleStatus(CommandSender sender, String[] args) {
        World world = resolveWorld(sender, args.length >= 2 ? args[1] : null);
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "Unable to resolve world for status.");
            return true;
        }

        DayUtilsApi api = plugin.getApi();
        CustomDayType active = findActiveCustomDay(world, api.getRegisteredCustomDayTypes());
        boolean isDay = dayInfoService.isDay(world);
        boolean isNight = dayInfoService.isNight(world);

        if (active != null) {
            sender.sendMessage(ChatColor.AQUA
                    + "Custom day "
                    + active.displayName()
                    + " progress: "
                    + formatPercent(dayInfoService.getFullCycleProgress(world))
                    + " in "
                    + world.getName());
            return true;
        }

        if (isDay) {
            sender.sendMessage(ChatColor.GREEN
                    + "Daytime in "
                    + world.getName()
                    + " ("
                    + formatPercent(dayInfoService.getDayProgress(world))
                    + " complete).");
            return true;
        }

        if (isNight) {
            sender.sendMessage(ChatColor.BLUE
                    + "Nighttime in "
                    + world.getName()
                    + " ("
                    + formatPercent(dayInfoService.getNightProgress(world))
                    + " complete).");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Cycle status unavailable for " + world.getName());
        return true;
    }

    private CustomDayType findActiveCustomDay(World world, Collection<CustomDayType> registered) {
        for (CustomDayType type : registered) {
            if (dayInfoService.isCustomDay(world, type.id())) {
                return type;
            }
        }
        return null;
    }

    private String formatPercent(double progress) {
        return String.format("%.1f%%", progress * 100);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterStartingWith(
                    List.of("status", "reload", "trigger", "setdaylength", "setnightlength", "setspeed"),
                    args[0]);
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            return switch (subCommand) {
                case "status" -> worldNames(args[1]);
                case "trigger" -> filterStartingWith(
                        plugin.getApi().getRegisteredCustomDayTypes().stream()
                                .map(CustomDayType::id)
                                .toList(),
                        args[1]);
                case "setdaylength" -> suggestValue(plugin.getSettings().dayLengthTicks(), args[1]);
                case "setnightlength" -> suggestValue(plugin.getSettings().nightLengthTicks(), args[1]);
                case "setspeed" -> suggestValue(plugin.getSettings().speedMultiplier(), args[1]);
                default -> List.of();
            };
        }

        if (args.length == 3 && subCommand.equals("trigger")) {
            return worldNames(args[2]);
        }

        return List.of();
    }

    private List<String> filterStartingWith(List<String> options, String prefix) {
        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lowerPrefix)) {
                matches.add(option);
            }
        }
        return matches;
    }

    private List<String> worldNames(String prefix) {
        return filterStartingWith(
                plugin.getServer().getWorlds().stream().map(World::getName).toList(), prefix);
    }

    private List<String> suggestValue(double value, String prefix) {
        String formatted = value % 1 == 0 ? String.valueOf((long) value) : String.valueOf(value);
        return filterStartingWith(List.of(formatted), prefix);
    }

    private Long parseLong(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double parseDouble(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
