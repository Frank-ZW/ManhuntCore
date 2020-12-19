package me.frankthedev.manhuntcore.command.impl;

import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.queue.manager.QueueManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ManhuntUnqueueCommand implements ManhuntSubcommand {

	@Override
	public void execute(PlayerData senderData, String[] args) {
		Player sender = senderData.getPlayer();
		if (args.length == 1) {
			QueueManager.getInstance().unqueuePlayer(senderData);
		} else {
			sender.sendMessage(ChatColor.RED + "To leave a current Manhunt queue, type /manhunt unqueue");
		}
	}
}
