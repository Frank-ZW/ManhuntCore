package me.frankthedev.manhuntcore.command.impl;

import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.data.manager.PlayerManager;
import me.frankthedev.manhuntcore.manhunt.manager.ManhuntManager;
import me.frankthedev.manhuntcore.util.bukkit.ManhuntPermissions;
import me.frankthedev.manhuntcore.util.java.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class ManhuntStartCommand implements ManhuntSubcommand {

	@Override
	public void execute(PlayerData senderData, String[] args) {
		Player sender = senderData.getPlayer();
		if (!sender.hasPermission(ManhuntPermissions.MANHUNT_START)) {
			sender.sendMessage(StringUtil.NO_PERMISSION);
			return;
		}

		if (senderData.isInActiveManhunt() || senderData.isInQueuedManhunt() || senderData.isInSpectateManhunt()) {
			sender.sendMessage(ChatColor.RED + "You are already in a Manhunt game.");
			return;
		}

		if (args.length > 1) {
			boolean random = false;
			boolean delayed = false;
			List<UUID> players = new ArrayList<>();
			for (int i = 1; i < args.length; i++) {
				switch (StringUtils.lowerCase(args[i])) {
					case "-r":
						random = true;
						break;
					case "-d":
						delayed = true;
						break;
					default:
						Player p = Bukkit.getPlayer(args[i]);
						if (p == null) {
							continue;
						}

						PlayerData pData = PlayerManager.getInstance().getPlayerData(p);
						if (pData == null || pData.isInActiveManhunt() || pData.isInSpectateManhunt() || pData.isInQueuedManhunt()) {
							continue;
						}

						players.add(p.getUniqueId());
				}
			}

			if (random) {
				/*
				 * The Manhunt game started should randomly select a player to be the
				 * speedrunner.
				 */
				players.add(sender.getUniqueId());
				if (delayed) {
					ManhuntManager.getInstance().queueStartingManhunt(players);
				} else {
					ManhuntManager.getInstance().createManhunt(players);
				}
			} else {
				/*
				 * The Manhunt game started should start with the commandsender as the
				 * speedrunner.
				 */
				if (delayed) {
					ManhuntManager.getInstance().queueStartingManhunt(sender.getUniqueId(), players);
				} else {
					ManhuntManager.getInstance().createManhunt(sender.getUniqueId(), players);
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "To start a Manhunt game, type /manhunt start <hunters>");
		}
	}
}
