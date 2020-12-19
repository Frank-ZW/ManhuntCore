package me.frankthedev.manhuntcore.command.impl;

import me.frankthedev.manhuntcore.ManhuntCore;
import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.util.bukkit.ManhuntPermissions;
import me.frankthedev.manhuntcore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ManhuntReloadCommand implements ManhuntSubcommand {

	@Override
	public void execute(PlayerData senderData, String[] args) {
		Player sender = senderData.getPlayer();
		if (!sender.hasPermission(ManhuntPermissions.RELOAD)) {
			sender.sendMessage(StringUtil.NO_PERMISSION);
			return;
		}

		if (args.length == 1) {
			ManhuntCore.getInstance().reloadConfig();
			ManhuntCore.getInstance().readConfig(false);
			sender.sendMessage(ChatColor.GREEN + "Successfully reloaded Manhunt config and internal values.");
		} else {
			sender.sendMessage(ChatColor.RED + "To reload the Manhunt config, type /manhunt reload");
		}
	}
}
