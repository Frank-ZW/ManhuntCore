package me.frankthedev.manhuntcore.manhunt.manager;

import io.papermc.lib.PaperLib;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.frankthedev.manhuntcore.ManhuntCore;
import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.data.manager.PlayerManager;
import me.frankthedev.manhuntcore.manhunt.Manhunt;
import me.frankthedev.manhuntcore.util.bukkit.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ManhuntManager {

	private final ManhuntCore plugin;
	private final Map<Integer, Manhunt> gameKeyMap;        // Cache to store the game key and corresponding game
	private final Map<World, Manhunt> gameWorldMap;        // Cache to store the world in an active game and its corresponding game
	private final Queue<Manhunt> gameQueue;                // Queue of inactive manhunt game instances
	private final Random random;
	private final AtomicInteger gameKey;
	private static ManhuntManager instance;

	public ManhuntManager(ManhuntCore plugin) {
		this.plugin = plugin;
		this.gameKeyMap = new ConcurrentHashMap<>();
		this.gameWorldMap = new ConcurrentHashMap<>();
		this.gameKey = new AtomicInteger();
		this.gameQueue = new ConcurrentLinkedQueue<>();
		this.random = new Random();
	}

	public static void enable(ManhuntCore plugin) {
		instance = new ManhuntManager(plugin);
	}

	public static void disable() {
		instance.gameKeyMap.clear();
		instance.gameQueue.clear();
		instance = null;
	}

	public static ManhuntManager getInstance() {
		return instance;
	}

	public void createManhunt(@NotNull UUID speedrunner, @NotNull List<UUID> hunters) {
		this.createManhunt(speedrunner, hunters, Manhunt.ManhuntType.NORMAL, Manhunt.GamemodeType.VANILLA);
	}

	public void createManhunt(@NotNull UUID speedrunner, @NotNull List<UUID> hunters, @NotNull Manhunt.ManhuntType manhuntType, @NotNull Manhunt.GamemodeType gameModeType) {
		Manhunt manhunt = this.gameQueue.poll();
		if (manhunt == null) {
			manhunt = new Manhunt(speedrunner, hunters, manhuntType, gameModeType, this.getGameKeyAndIncrement());
		} else {
			manhunt.loadManhunt(speedrunner, hunters, this.getGameKeyAndIncrement());
		}

		manhunt.startManhunt();
		this.addManhunt(manhunt);
	}

	/**
	 * Selects a random player from the list of players specified to be chosen as the speedrunner
	 * and internally calls the #createManhuntGame method with the specified speedrunner and
	 * players list.
	 *
	 * @param players   The list of players to play the Manhunt.
	 */
	public void createManhunt(@NotNull List<UUID> players) {
		int index = this.random.nextInt(players.size());
		UUID speedrunner = players.remove(index);
		this.createManhunt(speedrunner, players);
	}

	public void endManhunt(@NotNull Manhunt manhunt, boolean speedrunnerWon) {
		manhunt.setFinished(true);
		if (manhunt.getGamemodeType() == Manhunt.GamemodeType.SURVIVAL && manhunt.getTask() != null) {
			manhunt.getTask().cancel();
		}

		if (speedrunnerWon) {
			manhunt.broadcast(ChatColor.GREEN + "The speedrunner has won the Manhunt game.");
		} else {
			manhunt.broadcast(ChatColor.GREEN + "The hunters have won the Manhunt game.");
		}

		Bukkit.getScheduler().runTaskLater(this.plugin, manhunt::endManhunt, 200L);
	}

	public void endManhuntNow(@NotNull Manhunt manhunt) {
		manhunt.setFinished(true);
		manhunt.endManhunt();
	}

	public void queueManhunt(@NotNull Manhunt manhunt) {
		PlayerManager.getInstance().removeFromDisconnectMap(manhunt);
		this.removeManhunt(manhunt);
		this.gameQueue.add(manhunt.unloadManhunt());
	}

	public void queueStartingManhunt(List<UUID> players) {
		int index = this.random.nextInt(players.size());
		UUID speedrunner = players.remove(index);
		players.remove(speedrunner);
		this.queueStartingManhunt(speedrunner, players);
	}

	/**
	 * Queues a Manhunt game in the starting stage and updates each player data variable. A
	 * Manhunt game is considered "starting" when the plugin displays the countdown for its
	 * participants.
	 *
	 * @param speedrunner   The designated speedrunner for the Manhunt game.
	 * @param hunters       The designated hunters for the Manhunt game.
	 */
	public void queueStartingManhunt(@NotNull UUID speedrunner, @NotNull List<UUID> hunters) {
		Manhunt manhunt = this.gameQueue.poll();
		if (manhunt == null) {
			manhunt = new Manhunt(speedrunner, hunters, Manhunt.ManhuntType.NORMAL, Manhunt.GamemodeType.VANILLA, this.getGameKeyAndIncrement());
		} else {
			manhunt.loadManhunt(speedrunner, hunters, this.getGameKeyAndIncrement());
		}

		for (UUID uniqueId : manhunt.getTotal()) {
			PlayerData playerData = PlayerManager.getInstance().getPlayerData(uniqueId);
			if (playerData != null) {
				playerData.setQueuedManhunt(manhunt);
			}
		}

//		manhunt.createWorlds();
		manhunt.startCountdown();
	}

	/**
	 * @param spectatorData The player data of the player that sent the command to spectate a game.
	 * @param target        The player to be spectated.
	 */
	public void addSpectator(@NotNull PlayerData spectatorData, @NotNull Player target) {
		Player spectator = spectatorData.getPlayer();
		PlayerData targetData = PlayerManager.getInstance().getPlayerData(target);
		if (targetData == null) {
			return;
		}

		if (spectatorData.getUniqueId().equals(targetData.getUniqueId())) {
			spectator.sendMessage(ChatColor.RED + "You cannot spectate yourself.");
			return;
		}

		if (!targetData.isInActiveManhunt()) {
			spectator.sendMessage(ChatColor.RED + "You can only spectate players in an active Manhunt game.");
			return;
		}

		Manhunt manhunt = targetData.getActiveManhunt();
		for (UUID uniqueId : manhunt.getTotal()) {
			PlayerData playerData = PlayerManager.getInstance().getPlayerData(uniqueId);
			if (playerData == null || manhunt.isSpectator(uniqueId)) {
				continue;
			}

			Player player = playerData.getPlayer();
			player.hidePlayer(this.plugin, spectator);
		}

		manhunt.addSpectator(spectatorData.getUniqueId());
		PlayerUtil.setSpectator(spectator, spectatorData, manhunt);
		PaperLib.teleportAsync(spectator, target.getLocation()).thenAccept(result -> {
			if (result) {
				spectator.setAllowFlight(true);
				spectator.setFlying(true);
				spectator.sendMessage(ChatColor.GREEN + "You are now spectating " + target.getName() + "'s game.");
			} else {
				spectator.sendMessage(ChatColor.RED + "Failed to teleport you to " + target.getName() + "'s game.");
			}
		});
	}

	/**
	 * Removes the specified player from the Manhunt game. This method works for both
	 * speedrunner, hunters, and spectators.
	 *
	 * @param manhunt       The Manhunt game to be removed from.
	 * @param playerData    The player data of the player to be removed from.
	 */
	public void removePlayer(@NotNull Manhunt manhunt, @NotNull PlayerData playerData) {
		Player player = playerData.getPlayer();
		PaperLib.teleportAsync(player, this.plugin.getLobbySpawn()).thenAccept(result -> {
			if (result) {
				if (manhunt.isSpectator(player.getUniqueId())) {
					player.sendMessage(ChatColor.RED + "You have left the Manhunt game you were spectating.");
					manhunt.removeSpectator(player.getUniqueId());
					ObjectOpenHashSet<UUID> actives = manhunt.getActivePlayers();
					for (UUID uniqueId : actives) {
						Player active = Bukkit.getPlayer(uniqueId);
						if (active != null) {
							active.showPlayer(this.plugin, player);
						}
					}
				} else {
					player.sendMessage(ChatColor.RED + "You have left the Manhunt game.");
					PlayerUtil.resetAttributes(player);
					playerData.setActiveManhunt(null);
					this.handleDisconnect(manhunt, player);
				}
			} else {
				player.sendMessage(ChatColor.RED + "Failed to teleport you to the lobby. Contact an administrator if this occurs.");
			}
		});
	}

	@Deprecated
	public void removeSpectator(@NotNull Player spectator, @NotNull PlayerData spectatorData) {
		if (!spectatorData.isInSpectateManhunt()) {
			spectator.sendMessage(ChatColor.RED + "You aren't spectating a game currently.");
			return;
		}

		Manhunt manhunt = spectatorData.getSpectateManhunt();
		PaperLib.teleportAsync(spectator, this.plugin.getLobbySpawn()).thenAccept(result -> {
			if (result) {
				manhunt.removeSpectator(spectator.getUniqueId());
				PlayerUtil.unsetSpectator(spectator);
				for (UUID uniqueId : manhunt.getTotal()) {
					PlayerData playerData = PlayerManager.getInstance().getPlayerData(uniqueId);
					if (playerData == null) {
						continue;
					}

					Player player = playerData.getPlayer();
					player.showPlayer(this.plugin, spectator);
				}

				spectator.sendMessage(ChatColor.GREEN + "You have left the game as a spectator.");
			} else {
				spectator.sendMessage(ChatColor.RED + "Failed to teleport you to the lobby. Contact an administrator if this occurs.");
			}
		});
	}

	/**
	 * Handles player disconnection from the Manhunt game. This method should be called
	 * when the player has left the Manhunt and cannot rejoin.
	 *
	 * @param manhunt   The Manhunt game the player left
	 * @param player    The player leaving the Manhunt game
	 */
	public void handleDisconnect(@NotNull Manhunt manhunt, @NotNull Player player) {
		if (manhunt.isSpeedrunner(player.getUniqueId())) {
			manhunt.broadcast(ChatColor.RED + player.getName() + " disconnected.");
			this.endManhunt(manhunt, false);
		} else {
			manhunt.removeHunter(player.getUniqueId());
			manhunt.broadcast(ChatColor.RED + player.getName() + " disconnected.");
			if (manhunt.getHunters().isEmpty()) {
				this.endManhunt(manhunt, true);
			}
		}
	}

	@Nullable
	public Manhunt getManhunt(@NotNull World world) {
		return this.gameWorldMap.get(world);
	}

	/**
	 * @param gameKey  The game's unique gamekey.
	 * @return          The game if there is a mapping available, null otherwise.
	 */
	@Nullable
	public Manhunt getManhunt(int gameKey) {
		return this.gameKeyMap.get(gameKey);
	}

	public Set<Integer> getGameKeys() {
		return this.gameKeyMap.keySet();
	}

	public int getGameKeyAndIncrement() {
		return this.gameKey.getAndIncrement();
	}

	public int getGameKey() {
		return this.gameKey.get();
	}

	public void setGameKey(int gameKey) {
		this.gameKey.set(gameKey);
	}

	public void addManhunt(@NotNull Manhunt manhunt) {
		this.gameKeyMap.put(manhunt.getGameKey(), manhunt);
		this.gameWorldMap.put(manhunt.getOverworld(), manhunt);
		this.gameWorldMap.put(manhunt.getNether(), manhunt);
		this.gameWorldMap.put(manhunt.getEnd(), manhunt);
	}

	public void removeManhunt(@NotNull Manhunt manhunt) {
		this.gameKeyMap.remove(manhunt.getGameKey());
		this.gameWorldMap.remove(manhunt.getOverworld());
		this.gameWorldMap.remove(manhunt.getNether());
		this.gameWorldMap.remove(manhunt.getEnd());
	}
}
