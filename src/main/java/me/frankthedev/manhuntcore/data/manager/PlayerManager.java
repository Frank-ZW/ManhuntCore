package me.frankthedev.manhuntcore.data.manager;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.papermc.lib.PaperLib;
import me.frankthedev.manhuntcore.ManhuntCore;
import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.manhunt.Manhunt;
import me.frankthedev.manhuntcore.manhunt.manager.ManhuntManager;
import me.frankthedev.manhuntcore.util.bukkit.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class PlayerManager {

	private static PlayerManager instance;
	private final ManhuntCore plugin;
	private final Map<UUID, PlayerData> players;
	private final Map<UUID, Manhunt> disconnectionMap;         // Map to hold player disconnection issues.
	private final ExecutorService executors;

	public PlayerManager(ManhuntCore plugin) {
		this.plugin = plugin;
		this.players = new ConcurrentHashMap<>();
		this.disconnectionMap = new ConcurrentHashMap<>();
		this.executors = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("ManhuntCore Player Threads").build());
		this.executors.execute(() -> Bukkit.getOnlinePlayers().forEach(this::addPlayer));
	}

	public static void enable(ManhuntCore plugin) {
		instance = new PlayerManager(plugin);
	}

	public static void disable() {
		instance.executors.execute(() -> Bukkit.getOnlinePlayers().forEach(instance::removePlayer));
		instance.executors.shutdown();
		instance.disconnectionMap.clear();
		instance.players.clear();
	}

	public static PlayerManager getInstance() {
		return instance;
	}

	/**
	 * Adds a player on the join event to the server cache and handles player disconnection
	 * logic. Checks to see if the player has an active Manhunt game in the disconnect map
	 * and updates their player data accordingly. Teleports players not in a Manhunt game
	 * back to the lobby and resets their attributes.
	 *
	 * @param player    The player entering the server.
	 */
	public void addPlayer(Player player) {
		PlayerData playerData = new PlayerData(player);
		Manhunt manhunt = this.disconnectionMap.remove(player.getUniqueId());
		if (manhunt != null) {
			playerData.setActiveManhunt(manhunt);
		}

		if (!playerData.isInActiveManhunt() && !player.getWorld().equals(this.plugin.getLobbySpawn().getWorld())) {
			this.plugin.getServer().getScheduler().runTask(this.plugin, () -> PaperLib.teleportAsync(player, this.plugin.getLobbySpawn()).thenAccept(result -> {
				if (result) {
					player.sendMessage(ChatColor.GREEN + "You have been teleported to the lobby.");
					PlayerUtil.resetAttributes(player);
				} else {
					player.sendMessage(ChatColor.RED + "Failed to teleport you back to the lobby. Contact an administrator if this occurs.");
				}
			}));
		}

		this.players.put(player.getUniqueId(), playerData);
//		PacketManager.getInstance().injectPlayerData(playerData);
	}

	/**
	 * Removes player data from the internal map and handles player
	 * disconnect logic for in-game players. If the player is in an
	 * active Manhunt game, the method will store the players in an
	 * internal map and remove that entry after 3 minutes (3600 ticks)
	 *
	 * @param player    The player disconnecting from the server.
	 */
	public void removePlayer(Player player) {
		PlayerData playerData = this.players.remove(player.getUniqueId());
		if (playerData == null) {
			return;
		}

		if (playerData.isInActiveManhunt()) {
			Manhunt now = playerData.getActiveManhunt();
			now.broadcast(ChatColor.RED + playerData.getName() + " has left the Manhunt. They have 3 minutes before they are eliminated.");
			this.disconnectionMap.put(player.getUniqueId(), now);
			Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
				Manhunt future = this.disconnectionMap.remove(player.getUniqueId());
				if (future != null) {
					ManhuntManager.getInstance().handleDisconnect(future, player);
				}
			}, TimeUnit.MINUTES.toSeconds(3) * 20);
		}
//		PacketManager.getInstance().uninjectPlayerData(playerData);
	}

	/**
	 * Clears internal disconnect map of the specified Manhunt game. This method
	 * should be called when a Manhunt game ends and the server must remove all
	 * instances of that Manhunt game from the disconnect cache.
	 *
	 * @param manhunt   The Manhunt game to be removed from the disconnect map.
	 */
	public void removeFromDisconnectMap(@NotNull Manhunt manhunt) {
		this.disconnectionMap.entrySet().parallelStream().forEach(entry -> {
			if (manhunt.equals(entry.getValue())) {
				this.disconnectionMap.remove(entry.getKey());
			}
		});
	}

	/**
	 * This method internally calls the #getPlayerData method for the player's UUID.
	 *
	 * @param player    The player to retrieve their corresponding player data.
	 * @return          The player data instance if the player has an entry in the player cache, or null otherwise.
	 */
	@Nullable
	public PlayerData getPlayerData(Player player) {
		return this.getPlayerData(player.getUniqueId());
	}

	/**
	 * @param playerUUID    The UUID of the player to retrieve their corresponding player data.
	 * @return              The player data instance if the player has an entry in the player cache, or null otherwise.
	 */
	@Nullable
	public PlayerData getPlayerData(UUID playerUUID) {
		return this.players.get(playerUUID);
	}

	/**
	 * Executes a runnable on the separate Player Data thread.
	 *
	 * @param runnable  The runnable that should be executed on the Player Data thread.
	 */
	public void executePlayerThread(Runnable runnable) {
		this.executors.execute(runnable);
	}
}
