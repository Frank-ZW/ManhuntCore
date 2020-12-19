package me.frankthedev.manhuntcore.command;

import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.data.manager.PlayerManager;
import me.frankthedev.manhuntcore.manhunt.manager.ManhuntManager;
import me.frankthedev.manhuntcore.util.bukkit.ManhuntPermissions;
import me.frankthedev.manhuntcore.util.java.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpectateCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You must be a player to run this command.");
			return true;
		}

		Player player = (Player) sender;
		if (!player.hasPermission(ManhuntPermissions.MANHUNT_SPECTATE)) {
			player.sendMessage(StringUtil.NO_PERMISSION);
			return true;
		}

		PlayerData senderData = PlayerManager.getInstance().getPlayerData(player);
		if (senderData == null) {
			return true;
		}

		if (senderData.isInActiveManhunt()) {
			player.sendMessage(ChatColor.RED + "You cannot spectate whilst in a game.");
			return true;
		}

		if (args.length == 1) {
			Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				player.sendMessage(ChatColor.RED + "You can only spectate players that are online.");
				return true;
			}

			ManhuntManager.getInstance().addSpectator(senderData, target);
		} else {
			player.sendMessage(ChatColor.RED + "To spectate a player, type /spectate <player>.");
		}

		return true;
	}
}
