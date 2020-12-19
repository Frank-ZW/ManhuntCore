package me.frankthedev.manhuntcore.command.impl;

import me.frankthedev.manhuntcore.ManhuntCore;
import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.util.bukkit.ManhuntPermissions;
import me.frankthedev.manhuntcore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ManhuntSpawnCommand implements ManhuntSubcommand {

	@Override
	public void execute(PlayerData senderData, String[] args) {
		Player sender = senderData.getPlayer();
		if (!sender.hasPermission(ManhuntPermissions.SET_SPAWN)) {
			sender.sendMessage(StringUtil.NO_PERMISSION);
			return;
		}

		if (args.length == 1) {
			Location spawnLocation = sender.getLocation().clone();
			spawnLocation.setX(spawnLocation.getBlockX() + 0.5D);
			spawnLocation.setZ(spawnLocation.getBlockZ() + 0.5D);
			ManhuntCore.getInstance().setLobbySpawn(spawnLocation);
			ManhuntCore.getInstance().writeConfig();
			sender.sendMessage(ChatColor.GREEN + "Updated manhunt spawn to (" + spawnLocation.getX() + ", " + spawnLocation.getY() + ", " + spawnLocation.getZ() + ") in world " + spawnLocation.getWorld().getName() + ".");
		} else {
			sender.sendMessage(ChatColor.RED + "To set the new spawn location of the lobby, type /manhunt setspawn");
		}
	}
}
