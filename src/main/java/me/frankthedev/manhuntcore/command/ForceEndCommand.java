package me.frankthedev.manhuntcore.command;

import me.frankthedev.manhuntcore.manhunt.Manhunt;
import me.frankthedev.manhuntcore.manhunt.manager.ManhuntManager;
import me.frankthedev.manhuntcore.util.bukkit.ManhuntPermissions;
import me.frankthedev.manhuntcore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ForceEndCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!sender.hasPermission(ManhuntPermissions.FORCE_END)) {
			sender.sendMessage(StringUtil.NO_PERMISSION);
			return true;
		}

		if (args.length == 1) {
			try {
				int gameKey = Integer.parseInt(args[0]);
				Manhunt manhunt = ManhuntManager.getInstance().getManhunt(gameKey);
				if (manhunt == null) {
					sender.sendMessage(ChatColor.RED + "There aren't any ongoing Manhunt games with the game key " + gameKey);
					return true;
				}

				ManhuntManager.getInstance().endManhuntNow(manhunt);
				sender.sendMessage(ChatColor.GREEN + "The manhunt game with the game key " + gameKey + " has forcefully been ended.");
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "The game key entered must be an integer.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "To force end an ongoing manhunt game, type /forceend <game key>");
		}

		return true;
	}
}
