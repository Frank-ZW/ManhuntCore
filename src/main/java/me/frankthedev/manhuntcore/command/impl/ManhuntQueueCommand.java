package me.frankthedev.manhuntcore.command.impl;

import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.queue.manager.QueueManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ManhuntQueueCommand implements ManhuntSubcommand {

	@Override
	public void execute(PlayerData senderData, String[] args) {
		Player sender = senderData.getPlayer();
		if (args.length == 2) {
			try {
				int nHunters = Integer.parseInt(args[1]);
				QueueManager.getInstance().queuePlayer(sender, nHunters);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "The number of hunters specified must be an integer.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "To place yourself in a queue for a Manhunt game, type /manhunt queue <number of Hunters>");
		}
	}
}
