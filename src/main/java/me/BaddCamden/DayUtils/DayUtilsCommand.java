package me.BaddCamden.DayUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DayUtilsCommand implements CommandExecutor {
    private final DayUtilsPlugin plugin;

    public DayUtilsCommand(DayUtilsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }

        sender.sendMessage(ChatColor.YELLOW + "DayUtils usage: /" + label + " reload");
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
}
