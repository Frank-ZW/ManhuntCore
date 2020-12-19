package me.frankthedev.manhuntcore.queue;

import me.frankthedev.manhuntcore.manhunt.manager.ManhuntManager;
import me.frankthedev.manhuntcore.util.bukkit.ManhuntPermissions;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ManhuntQueue {

	private final Queue<UUID> priorityQueue;
	private final Queue<UUID> defaultQueue;
	private final int nHunters;

	public ManhuntQueue(int nHunters) {
		this.nHunters = nHunters;
		this.priorityQueue = new ConcurrentLinkedDeque<>();
		this.defaultQueue = new ConcurrentLinkedDeque<>();
	}

	public void queuePlayer(Player player) {
		if (player.hasPermission(ManhuntPermissions.PRIORITY_QUEUE)) {
			this.priorityQueue.add(player.getUniqueId());
		} else {
			this.defaultQueue.add(player.getUniqueId());
		}

		this.attemptToStartGame();
	}

	public void unqueuePlayer(Player player) {
		CompletableFuture.runAsync(() -> {
			Iterator<UUID> iterator = this.priorityQueue.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().equals(player.getUniqueId())) {
					iterator.remove();
					break;
				}
			}

			iterator = this.defaultQueue.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().equals(player.getUniqueId())) {
					iterator.remove();
					break;
				}
			}
		});
	}

	public void attemptToStartGame() {
		if (this.defaultQueue.size() + this.priorityQueue.size() < this.nHunters + 1) {
			return;
		}

		List<UUID> players = new ArrayList<>();
		for (int i = 0; i < this.nHunters + 1; i++) {
			players.add((i % 4 == 3 || this.priorityQueue.isEmpty()) ? this.defaultQueue.poll() : this.priorityQueue.poll());
		}

		ManhuntManager.getInstance().queueStartingManhunt(players);
	}
}
