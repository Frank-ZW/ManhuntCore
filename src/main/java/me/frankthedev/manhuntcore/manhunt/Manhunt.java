package me.frankthedev.manhuntcore.manhunt;

import io.papermc.lib.PaperLib;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.frankthedev.manhuntcore.ManhuntCore;
import me.frankthedev.manhuntcore.data.AdvancementData;
import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.data.manager.PlayerManager;
import me.frankthedev.manhuntcore.manhunt.manager.ManhuntManager;
import me.frankthedev.manhuntcore.runnable.CountdownRunnable;
import me.frankthedev.manhuntcore.util.bukkit.AdvancementUtil;
import me.frankthedev.manhuntcore.util.bukkit.ItemUtil;
import me.frankthedev.manhuntcore.util.bukkit.PlayerUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Manhunt {

	private final ManhuntCore plugin = ManhuntCore.getInstance();
	private UUID speedrunner;
	private int gameKey;
	private final Set<UUID> hunters = new HashSet<>();
	private final Set<UUID> spectators = new HashSet<>();
	private final Set<SpeedrunnerPerk> perks = new HashSet<>();
	private final Set<UUID> enteredNether = new HashSet<>();
	private final Set<UUID> enteredEnd = new HashSet<>();
	private final Map<World.Environment, World> worlds = new HashMap<>();
	private boolean finished;
	private GamemodeType gameModeType;
	private ManhuntType manhuntType;
	private long startTimestamp;
	private BukkitTask task;

	public Manhunt(UUID speedrunner, List<UUID> hunters, ManhuntType manhuntType, GamemodeType gameModeType, int gameKey) {
		this(speedrunner, hunters, manhuntType, gameModeType, gameKey, System.currentTimeMillis());
	}

	public Manhunt(UUID speedrunner, List<UUID> hunters, ManhuntType manhuntType, GamemodeType gameModeType, int gameKey, long startTimestamp) {
		this.speedrunner = speedrunner;
		this.gameKey = gameKey;
		this.hunters.addAll(hunters);
		this.manhuntType = manhuntType;
		this.gameModeType = gameModeType;
		this.finished = false;
		this.startTimestamp = startTimestamp;
	}

	public void clearAdvancements(Player player) {
		Iterator<Advancement> iterator = this.plugin.getServer().advancementIterator();
		while (iterator.hasNext()) {
			AdvancementProgress progress = player.getAdvancementProgress(iterator.next());
			for (String awarded : progress.getAwardedCriteria()) {
				progress.revokeCriteria(awarded);
			}
		}
	}

	public void startTeleport() {
		Player runner = this.getSpeedrunnerPlayer();
		if (runner == null) {
			this.broadcast(ChatColor.RED + "An error occurred while teleporting players to the worlds. Contact an administrator if this occurs.");
			this.setFinished(true);
			this.endManhunt();
			return;
		}

		float theta = 0.0F;
		float delta = 360.0F / this.hunters.size();
		int radius = Math.max(6, 3 * this.hunters.size());
		Location spawn = this.getOverworld().getSpawnLocation();
		ObjectOpenHashSet<UUID> total = this.getActivePlayers();
		Bukkit.getLogger().info(ChatColor.GREEN + "Starting Manhunt game with " + this.hunters.size() + " hunters.");
		for (UUID uniqueId : total) {
			PlayerData playerData = PlayerManager.getInstance().getPlayerData(uniqueId);
			if (playerData == null) {
				continue;
			}

			Player player = playerData.getPlayer();
			if (!player.isOnline()) {
				continue;
			}

			if (player.isDead()) {
				player.spigot().respawn();
			}

			this.clearAdvancements(player);
			playerData.setQueuedManhunt(null);
			if (this.isSpeedrunner(uniqueId)) {
				PaperLib.teleportAsync(player, spawn).thenAccept(result -> {
					if(result) {
						player.sendMessage(ChatColor.GREEN + "You are the speedrunner. Kill the Enderdragon before the hunters kill you!");
						PlayerUtil.resetAttributes(player);
						playerData.setActiveManhunt(this);
						if (!this.perks.isEmpty()) {
							for (SpeedrunnerPerk perk : this.perks) {
								if (perk == SpeedrunnerPerk.BLOCKS) {
									player.getInventory().addItem(ItemUtil.createItemStack(Material.DIRT, 32, ChatColor.GREEN + "Starting Blocks"));
								} else {
									player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 0));
								}
							}
						}

						if (this.gameModeType == GamemodeType.PRACTICE) {
							PlayerUtil.applyKit(player, true);
						}
					} else {
						player.sendMessage(ChatColor.RED + "Failed to teleport you to the Manhunt game. Contact an administrator if this occurs.");
					}
				});
			} else {
				int x = (int) (spawn.getX() + radius * Math.cos(Math.toRadians(theta)));
				int z = (int) (spawn.getZ() + radius * Math.sin(Math.toRadians(theta)));
				int y = spawn.getWorld().getHighestBlockYAt(x, z);
				Location hunter = new Location(spawn.getWorld(), x, y, z);
				Bukkit.getLogger().info(ChatColor.GREEN + String.format("%s (%s %s %s) %s", player.getName(), hunter.getX(), hunter.getY(), hunter.getZ(), theta));
				PaperLib.teleportAsync(player, hunter).thenAccept(result -> {
					if (result) {
						player.sendMessage(ChatColor.RED + "You are a hunter. You must use your Player Tracker to hunt and kill " + runner.getName() + "!");
						PlayerUtil.resetAttributes(player);
						playerData.setActiveManhunt(this);
						player.getInventory().setItem(8, ItemUtil.createPlayerTracker());
					} else {
						player.sendMessage(ChatColor.RED + "Failed to teleport you to the Manhunt game. Contact an administrator if this occurs.");
					}
				});

				theta += delta;
			}
		}

		if (this.gameModeType == GamemodeType.SURVIVAL) {
			this.task = this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () -> ManhuntManager.getInstance().endManhunt(this, true), TimeUnit.HOURS.toSeconds(1) * 20);
		}
	}

	public void endTeleport() {
		ObjectOpenHashSet<UUID> total = this.getTotalPlayers();
		for (UUID uniqueId : total) {
			PlayerData playerData = PlayerManager.getInstance().getPlayerData(uniqueId);
			if (playerData == null) {
				continue;
			}

			Player player = playerData.getPlayer();
			if (!player.isOnline()) {
				continue;
			}

			if (player.isDead()) {
				player.spigot().respawn();
			}

			PaperLib.teleportAsync(player, this.plugin.getLobbySpawn()).thenAccept(result -> {
				player.sendMessage(ChatColor.GREEN + "You have been teleported back to the lobby.");
				if (result) {
					this.clearAdvancements(player);
					if (this.isSpectator(uniqueId)) {
						PlayerUtil.unsetSpectator(player);
						playerData.setSpectateManhunt(null);
						this.showSpectator(player);
					} else {
						PlayerUtil.resetAttributes(player);
						playerData.setActiveManhunt(null);
					}
				} else {
					player.sendMessage(ChatColor.RED + "Failed to teleport you back to the lobby. Contact an administrator if this occurs.");
				}
			});
		}
	}

	/**
	 * Unhides all the spectators from a Manhunt game after the game has been
	 * ended. However if another plugin is still hiding the spectator, then
	 * the server will not show spectator until that plugin shows them too.
	 *
	 * @param spectator The spectator to be shown to other players.
	 */
	public void showSpectator(Player spectator) {
		ObjectOpenHashSet<UUID> actives = this.getActivePlayers();
		for (UUID uniqueId : actives) {
			Player player = Bukkit.getPlayer(uniqueId);
			if (player != null) {
				player.showPlayer(this.plugin, spectator);
			}
		}
	}

	public boolean createWorlds() {
		for (int i = 0; i < 3; i++) {
			World.Environment environment = World.Environment.values()[i];
			WorldCreator creator = new WorldCreator(gameKey + "_" + StringUtils.lowerCase(String.valueOf(environment)));
			creator.environment(environment);
			creator.type(this.manhuntType == ManhuntType.AMPLIFIED ? WorldType.AMPLIFIED: (this.manhuntType == ManhuntType.LARGE_BIOMES ? WorldType.LARGE_BIOMES : WorldType.NORMAL));
			World world = creator.createWorld();
			if (world != null) {
				world.setKeepSpawnInMemory(false);
				world.setAutoSave(false);
				world.setDifficulty(Difficulty.HARD);
				world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
				if (this.gameModeType == GamemodeType.SURVIVAL && environment == World.Environment.NORMAL) {
					WorldBorder border = world.getWorldBorder();
					border.setCenter(world.getSpawnLocation());
					border.setSize(500.0D);
				}

				this.worlds.put(environment, world);
			}
		}

		return this.worlds.size() == 3;
	}

	public void deleteWorlds() {
		for (World world : this.worlds.values()) {
			Bukkit.unloadWorld(world, false);
		}

		Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, () -> {
			try {
				for (World world : this.worlds.values()) {
					FileUtils.deleteDirectory(world.getWorldFolder());
				}
			} catch (IOException e) {
				Bukkit.getLogger().log(Level.WARNING, "Failed to delete world", e);
			}

			Bukkit.getLogger().info(ChatColor.GREEN + "Deleted worlds for Manhunt gamekey " + this.gameKey);
			ManhuntManager.getInstance().queueManhunt(this);
		}, 20 * (long) (Math.ceil(1.5D * this.getNumTotalPlayers())));
	}

	/**
	 * Starts the countdown for the Manhunt game. This method should
	 * be used only for players queued and starting soon. The runnable
	 * internally calls #startTeleport method.
	 */
	public void startCountdown() {
		 new CountdownRunnable(this).runTaskTimer(this.plugin, 20, 20);
	}

	/**
	 * Starts a manhunt game from the start. Creates all worlds and teleports every player. If the game
	 * should first create worlds independently and start player teleportation at a later state, the
	 * internal #createWorlds and #startTeleport methods should be used.
	 */
	public void startManhunt() {
		if (!this.createWorlds()) {
			this.broadcast(ChatColor.RED + "An error occurred while generating the worlds for the Manhunt game. Contact an administrator if this occurs.");
			this.setFinished(true);
			this.endManhunt();
			return;
		}

		this.startTeleport();
	}

	public void endManhunt() {
		this.endTeleport();
		this.deleteWorlds();
	}

	public void loadManhunt(UUID speedrunner, List<UUID> hunters, int gameKey) {
		this.loadManhunt(speedrunner, hunters, gameKey, ManhuntType.NORMAL, GamemodeType.VANILLA);
	}

	public void loadManhunt(UUID speedrunners, List<UUID> hunters, int gameKey, ManhuntType manhuntType, GamemodeType gameModeType) {
		this.speedrunner = speedrunners;
		this.gameKey = gameKey;
		this.hunters.addAll(hunters);
		this.manhuntType = manhuntType;
		this.gameModeType = gameModeType;
		this.startTimestamp = System.currentTimeMillis();
	}

	public Manhunt unloadManhunt() {
		this.speedrunner = null;
		this.hunters.clear();
		this.spectators.clear();
		this.worlds.clear();
		this.enteredNether.clear();
		this.enteredEnd.clear();
		this.perks.clear();
		this.gameKey = Integer.MIN_VALUE;
		this.startTimestamp = Long.MIN_VALUE;
		this.gameModeType = GamemodeType.UNKNOWN;
		this.manhuntType = ManhuntType.UNKNOWN;
		this.finished = false;
		return this;
	}

	public void broadcast(BaseComponent[] message) {
		this.getTotalPlayers().parallelStream().forEach(uuid -> {
			Player player = this.plugin.getServer().getPlayer(uuid);
			if (player != null) {
				player.sendMessage(message);
			}
		});
	}

	public void broadcast(String message) {
		this.getTotalPlayers().parallelStream().forEach(uuid -> {
			Player player = this.plugin.getServer().getPlayer(uuid);
			if (player != null) {
				player.sendMessage(message);
			}
		});
	}

	public void broadcastEffectWithTitle(String message) {
		Set<UUID> actives = this.getActivePlayers();
		for (UUID uniqueId : actives) {
			Player player = this.plugin.getServer().getPlayer(uniqueId);
			if (player != null) {
				player.sendTitle(message, null, 5, 10, 5);
				player.playEffect(player.getLocation(), Effect.CLICK2, Effect.CLICK2.getData());
			}
		}
	}

	public void displayAdvancements(Player sender, Advancement advancement) {
		this.displayAdvancements(sender, advancement.getKey().getKey());
	}

	public void displayAdvancements(Player sender, String key) {
		AdvancementData advancementData = AdvancementUtil.getAdvancementData(key);
		if (advancementData != null) {
			this.broadcast(advancementData.getChatMessage(sender.getName()));
			if (advancementData.isExperience()) {
				sender.giveExp(advancementData.getReward());
			}
		}
	}

	public void updatePlayerTracker(@NotNull Player sender, @NotNull ItemStack compass) {
		Player player = this.getSpeedrunnerPlayer();
		if (player == null || !player.isOnline()) {
			sender.sendActionBar(ChatColor.RED + "There are no players to track!");
			return;
		}

		CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
		if (this.isHunter(sender.getUniqueId()) && compassMeta != null && ItemUtil.PLAYER_TRACKER.equals(compassMeta.getDisplayName())) {
			if (player.getWorld().equals(sender.getWorld())) {
				compassMeta.setLodestoneTracked(false);
				compassMeta.setLodestone(player.getLocation());
				compass.setItemMeta(compassMeta);
				sender.sendActionBar(ChatColor.GREEN + "Currently tracking " + player.getName() + "'s latest location.");
			} else {
				sender.sendActionBar(ChatColor.RED + "There are no players to track!");
			}
		}
	}

	public boolean isPlayerTracker(ItemStack item) {
		if (item.getType() == Material.COMPASS) {
			CompassMeta compassMeta = (CompassMeta) item.getItemMeta();
			return compassMeta != null && ItemUtil.PLAYER_TRACKER.equals(compassMeta.getDisplayName());
		}

		return false;
	}

	public boolean addOrRemovePerk(SpeedrunnerPerk perk) {
		if (!this.perks.remove(perk)) {
			this.perks.add(perk);
			return true;
		}

		return false;
	}

	public void addSpectator(UUID uniqueId) {
		this.spectators.add(uniqueId);
	}

	public void removeSpectator(UUID uniqueId) {
		this.spectators.remove(uniqueId);
	}

	public boolean isSpectator(UUID uniqueId) {
		return this.spectators.contains(uniqueId);
	}

	public boolean isHunter(UUID uniqueId) {
		return this.hunters.contains(uniqueId);
	}

	public void removeHunter(UUID uniqueId) {
		this.hunters.remove(uniqueId);
	}

	public Set<UUID> getHunters() {
		return this.hunters;
	}

	public ObjectOpenHashSet<UUID> getTotalPlayers() {
		ObjectOpenHashSet<UUID> total = this.getActivePlayers();
		total.addAll(this.spectators);
		return total;
	}

	public ObjectOpenHashSet<UUID> getActivePlayers() {
		ObjectOpenHashSet<UUID> activePlayers = new ObjectOpenHashSet<>(this.hunters);
		activePlayers.add(this.speedrunner);
		return activePlayers;
	}

	public int getNumTotalPlayers() {
		return this.spectators.size() + this.hunters.size() + 1;
	}

	public Set<UUID> getSpectators() {
		return this.spectators;
	}

	public long getStartTimestamp() {
		return this.startTimestamp;
	}

	public int getGameKey() {
		return this.gameKey;
	}

	public Map<World.Environment, World> getWorlds() {
		return this.worlds;
	}

	public World getOverworld() {
		return this.worlds.get(World.Environment.NORMAL);
	}

	public World getNether() {
		return this.worlds.get(World.Environment.NETHER);
	}

	public World getEnd() {
		return this.worlds.get(World.Environment.THE_END);
	}

	public boolean isFinished() {
		return this.finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public UUID getSpeedrunner() {
		return this.speedrunner;
	}

	@Nullable
	public Player getSpeedrunnerPlayer() {
		return Bukkit.getPlayer(this.speedrunner);
	}

	public boolean isSpeedrunner(UUID uniqueId) {
		return this.speedrunner.equals(uniqueId);
	}

	public GamemodeType getGameModeType() {
		return this.gameModeType;
	}

	public void setGameModeType(GamemodeType gameModeType) {
		this.gameModeType = gameModeType;
	}

	public ManhuntType getManhuntType() {
		return this.manhuntType;
	}

	public void setManhuntType(ManhuntType manhuntType) {
		this.manhuntType = manhuntType;
	}

	public Set<SpeedrunnerPerk> getPerks() {
		return this.perks;
	}

	public BukkitTask getTask() {
		return this.task;
	}

	public boolean isEnteredNether(UUID uniqueId) {
		return this.enteredNether.contains(uniqueId);
	}

	public void addEnteredNether(UUID uniqueId) {
		this.enteredNether.add(uniqueId);
	}

	public boolean isEnteredEnd(UUID uniqueId) {
		return this.enteredEnd.contains(uniqueId);
	}

	public void addEnteredEnd(UUID uniqueId) {
		this.enteredEnd.add(uniqueId);
	}

	public enum SpeedrunnerPerk {
		BLOCKS,
		POTION_EFFECTS;

		SpeedrunnerPerk() {

		}
	}

	public enum ManhuntType {
		NORMAL,
		AMPLIFIED,
		LARGE_BIOMES,
		UNKNOWN;

		ManhuntType() {

		}
	}

	public enum GamemodeType {
		VANILLA,
		SURVIVAL,
		PRACTICE,
		UNKNOWN;

		GamemodeType() {

		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = result * prime + this.speedrunner.hashCode();
		result = result * prime + this.hunters.hashCode();
		result = result * prime + this.gameKey;
		result = result * prime + this.worlds.hashCode();
		result = result * prime + (this.finished ? 1 : 0);
		result = result * prime + this.spectators.hashCode();
		result = result * prime + this.perks.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Manhunt)) return false;
		Manhunt manhunt = (Manhunt) obj;
		return this.speedrunner.equals(manhunt.getSpeedrunner()) &&
				this.hunters.equals(manhunt.getHunters()) &&
				this.gameKey == manhunt.getGameKey() &&
				this.worlds == manhunt.getWorlds() &&
				this.finished == manhunt.isFinished() &&
				this.spectators.equals(manhunt.getSpectators()) &&
				this.perks.equals(manhunt.getPerks());
	}
}
