package me.BaddCamden.DayUtils;

import java.util.Collection;
import java.util.stream.Collectors;
import me.BaddCamden.DayUtils.api.CustomDayType;
import me.BaddCamden.DayUtils.api.DayInfoService;
import me.BaddCamden.DayUtils.api.DayUtilsApi;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DayUtilsCommand implements CommandExecutor {
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
}
