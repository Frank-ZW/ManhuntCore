package me.frankthedev.manhuntcore.command.impl;

import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.manhunt.manager.ManhuntManager;
import me.frankthedev.manhuntcore.util.bukkit.ManhuntPermissions;
import me.frankthedev.manhuntcore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ManhuntLeaveCommand implements ManhuntSubcommand {

	@Override
	public void execute(PlayerData senderData, String[] args) {
		Player sender = senderData.getPlayer();
		if (!sender.hasPermission(ManhuntPermissions.MANHUNT_LEAVE)) {
			sender.sendMessage(StringUtil.NO_PERMISSION);
			return;
		}

		if (!senderData.isInActiveManhunt()) {
			sender.sendMessage(ChatColor.RED + "You must be in an active Manhunt game to run this command.");
			return;
		}

		ManhuntManager.getInstance().removePlayer(senderData.getActiveManhunt(), senderData);
	}
}
