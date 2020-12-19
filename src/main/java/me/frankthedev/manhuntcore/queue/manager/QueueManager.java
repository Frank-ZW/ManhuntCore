package me.frankthedev.manhuntcore.queue.manager;

import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.queue.ManhuntQueue;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class QueueManager {

	private final ManhuntQueue soloQueue = new ManhuntQueue(1);
	private final ManhuntQueue doubleQueue = new ManhuntQueue(2);
	private final ManhuntQueue tripleQueue = new ManhuntQueue(3);
	private final ManhuntQueue quadQueue = new ManhuntQueue(4);
	private static QueueManager instance;

	public static void enable() {
		instance = new QueueManager();
	}

	public static void disable() {
		instance = null;
	}

	public static QueueManager getInstance() {
		return instance;
	}

	public void queuePlayer(Player player, int nHunters) {
		if (nHunters < 1 || nHunters > 4) {
			player.sendMessage(ChatColor.RED + "Manhunt games with " + nHunters + " hunters are currently unsupported. To help the developer, consider making a small donation to the server!");
			return;
		}

		player.sendMessage(ChatColor.GREEN + "You have been queued for a Manhunt game with " + nHunters + " hunter" + (nHunters == 1 ? "" : "s"));
		switch (nHunters) {
			case 1:
				this.soloQueue.queuePlayer(player);
				break;
			case 2:
				this.doubleQueue.queuePlayer(player);
				break;
			case 3:
				this.tripleQueue.queuePlayer(player);
				break;
			default:
				this.quadQueue.queuePlayer(player);
		}
	}

	public void unqueuePlayer(PlayerData playerData) {
		Player player = playerData.getPlayer();
		if (!playerData.isInQueuedManhunt()) {
			player.sendMessage(ChatColor.RED + "You aren't currently queued in a Manhunt game.");
			return;
		}

		this.soloQueue.unqueuePlayer(player);
		this.doubleQueue.unqueuePlayer(player);
		this.tripleQueue.unqueuePlayer(player);
		this.quadQueue.unqueuePlayer(player);
		playerData.setQueuedManhunt(null);
		player.sendMessage(ChatColor.RED + "You have left your current Manhunt queue.");
	}
}
