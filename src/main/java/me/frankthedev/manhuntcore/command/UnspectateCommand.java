package me.frankthedev.manhuntcore.command;

import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.data.manager.PlayerManager;
import me.frankthedev.manhuntcore.manhunt.manager.ManhuntManager;
import me.frankthedev.manhuntcore.util.bukkit.ManhuntPermissions;
import me.frankthedev.manhuntcore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UnspectateCommand implements CommandExecutor {

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

		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData == null) {
			return true;
		}

		if (!playerData.isInSpectateManhunt()) {
			player.sendMessage(ChatColor.RED + "You are currently not spectating a Manhunt game.");
			return true;
		}

		if (args.length == 0) {
			ManhuntManager.getInstance().removePlayer(playerData.getSpectateManhunt(), playerData);
		} else {
			player.sendMessage(ChatColor.RED + "To unspectate a Manhunt game, type /unspectate.");
		}

		return true;
	}
}
