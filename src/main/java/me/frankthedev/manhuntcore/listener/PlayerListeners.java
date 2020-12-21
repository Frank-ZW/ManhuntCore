package me.frankthedev.manhuntcore.listener;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.ManhuntCore;
import me.frankthedev.manhuntcore.data.manager.PlayerManager;
import me.frankthedev.manhuntcore.manhunt.Manhunt;
import me.frankthedev.manhuntcore.manhunt.manager.ManhuntManager;
import me.frankthedev.manhuntcore.util.bukkit.ItemUtil;
import me.frankthedev.manhuntcore.util.bukkit.PlayerUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PlayerListeners implements Listener {

	private final ManhuntCore plugin;

	public PlayerListeners(ManhuntCore plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		PlayerManager.getInstance().executePlayerThread(() -> PlayerManager.getInstance().addPlayer(e.getPlayer()));
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		PlayerManager.getInstance().executePlayerThread(() -> PlayerManager.getInstance().removePlayer(e.getPlayer()));
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(e.getPlayer());
		if (playerData != null && playerData.isInActiveManhunt() && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && e.getItem().getType() == Material.COMPASS) {
			playerData.getActiveManhunt().updatePlayerTracker(playerData.getPlayer(), e.getItem());
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		Player player = e.getPlayer();
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData == null) {
			return;
		}

		if (playerData.isInActiveManhunt()) {
			Manhunt manhunt = playerData.getActiveManhunt();
			ItemStack item = e.getItemDrop().getItemStack();
			if (manhunt.isHunter(player.getUniqueId()) && item.getType() == Material.COMPASS) {
				ItemMeta compassMeta = item.getItemMeta();
				if (compassMeta != null && ItemUtil.PLAYER_TRACKER.equals(compassMeta.getDisplayName())) {
					e.setCancelled(true);
					player.sendMessage(ChatColor.RED + "You cannot drop your player tracker!");
				}
			}
		}
	}

	@EventHandler
	public void onEnderdragonDeath(EnderDragonChangePhaseEvent e) {
		if (e.getNewPhase() == EnderDragon.Phase.DYING) {
			Manhunt manhunt = ManhuntManager.getInstance().getManhunt(e.getEntity().getWorld());
			if (manhunt == null) {
				return;
			}

			ManhuntManager.getInstance().endManhunt(manhunt, true);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player player = e.getEntity();
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData == null || !playerData.isInActiveManhunt()) {
			return;
		}

		Manhunt manhunt = playerData.getActiveManhunt();
		if (manhunt.isFinished()) {
			e.setCancelled(true);
			return;
		}

		manhunt.broadcast(StringUtils.replace(e.getDeathMessage(), "[Player Tracker]", ItemUtil.PLAYER_TRACKER));
		e.setDeathMessage("");
		if (manhunt.getGameModeType() == Manhunt.GamemodeType.PRACTICE) {
			e.getDrops().clear();
			return;
		}

		if (manhunt.isSpeedrunner(player.getUniqueId())) {
			ManhuntManager.getInstance().endManhunt(manhunt, false);
		} else {
			e.getDrops().removeIf(manhunt::isPlayerTracker);
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) {
			return;
		}

		Player attacker = (Player) e.getDamager();
		Player target = (Player) e.getEntity();
		PlayerData attackerData = PlayerManager.getInstance().getPlayerData(attacker);
		PlayerData targetData = PlayerManager.getInstance().getPlayerData(target);
		if (attackerData == null || targetData == null || !attackerData.isInActiveManhunt() || !targetData.isInActiveManhunt() || e.isCancelled()) {
			return;
		}

		Manhunt manhunt = targetData.getActiveManhunt();
		if (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - manhunt.getStartTimestamp()) <= 5 || manhunt.isSpectator(target.getUniqueId()) || manhunt.isFinished()) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData == null) {
			return;
		}

		if (playerData.isInActiveManhunt()) {
			Manhunt manhunt = playerData.getActiveManhunt();
			e.setRespawnLocation(player.getBedSpawnLocation() == null ? manhunt.getOverworld().getSpawnLocation() : player.getBedSpawnLocation());
			if (manhunt.getGameModeType() == Manhunt.GamemodeType.PRACTICE) {
				PlayerUtil.applyKit(player, manhunt.isSpeedrunner(player.getUniqueId()));
				return;
			}

			if (manhunt.isHunter(player.getUniqueId())) {
				player.getInventory().setItem(8, ItemUtil.createPlayerTracker());
			}
			return;
		}

		if (playerData.isInSpectateManhunt()) {
			Manhunt manhunt = playerData.getSpectateManhunt();
			player.setGameMode(GameMode.SPECTATOR);
			e.setRespawnLocation(manhunt.getOverworld().getSpawnLocation());
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerAdvancementGrant(PlayerAdvancementCriterionGrantEvent e) {
		Player player = e.getPlayer();
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData == null || e.isCancelled()) {
			return;
		}

		if (player.getWorld().equals(this.plugin.getLobbySpawn().getWorld()) || playerData.isInSpectateManhunt()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent e) {
		Player player = e.getPlayer();
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData != null && playerData.isInActiveManhunt()) {
			playerData.getActiveManhunt().displayAdvancements(player, e.getAdvancement());
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerPortal(PlayerPortalEvent e) {
		Player player = e.getPlayer();
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData == null || e.getTo().getWorld() == null || e.getFrom().getWorld() == null || e.isCancelled()) {
			return;
		}

		Manhunt manhunt = playerData.isInActiveManhunt() ? playerData.getActiveManhunt() : (playerData.isInSpectateManhunt() ? playerData.getSpectateManhunt() : null);
		if (manhunt == null) {
			return;
		}

		World fromWorld = e.getFrom().getWorld();
		switch (e.getCause()) {
			case NETHER_PORTAL:
				switch (fromWorld.getEnvironment()) {
					case NORMAL:
						e.getTo().setWorld(manhunt.getNether());
						if (!manhunt.isEnteredNether(player.getUniqueId()) && !manhunt.isSpectator(player.getUniqueId())) {
							manhunt.addEnteredNether(player.getUniqueId());
							manhunt.displayAdvancements(player, "story/enter_the_nether");
						}

						break;
					case NETHER:
						e.setTo(new Location(manhunt.getOverworld(), e.getFrom().getX() * 8.0D, e.getFrom().getY(), e.getFrom().getZ() * 8.0D));
						break;
					default:
				}

				break;
			case END_PORTAL:
				switch (fromWorld.getEnvironment()) {
					case NORMAL:
						e.getTo().setWorld(manhunt.getEnd());
						if (!manhunt.isEnteredEnd(player.getUniqueId()) && !manhunt.isSpectator(player.getUniqueId())) {
							manhunt.addEnteredEnd(player.getUniqueId());
							manhunt.displayAdvancements(player, "story/enter_the_end");
						}

						break;
					case THE_END:
						e.setTo(player.getBedSpawnLocation() == null ? manhunt.getOverworld().getSpawnLocation() : player.getBedSpawnLocation());
						break;
					default:
				}

				break;
			default:
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player) e.getEntity();
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData == null) {
			return;
		}

		if (playerData.isInSpectateManhunt()) {
			e.setCancelled(true);
			return;
		}

		if (playerData.isInActiveManhunt()) {
			Manhunt manhunt = playerData.getActiveManhunt();
			if (manhunt.isFinished()) {
				e.setCancelled(true);
				return;
			}

			if (manhunt.isHunter(player.getUniqueId())) {
				switch (e.getCause()) {
					case SUFFOCATION:
					case FALL:
						if (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - manhunt.getStartTimestamp()) <= 1) {
							e.setCancelled(true);
						}

						break;
					default:
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData == null || e.isCancelled()) {
			return;
		}

		Set<Player> recipients = new HashSet<>(e.getRecipients());
		Iterator<Player> iterator = recipients.iterator();
		if (playerData.isInActiveManhunt()) {
			Manhunt manhunt = playerData.getActiveManhunt();
			if (manhunt.isSpeedrunner(player.getUniqueId())) {
				e.setFormat(ChatColor.GREEN + "[GAME CHAT] " + ChatColor.stripColor(player.getDisplayName()) + ChatColor.DARK_GRAY + ChatColor.BOLD + " » " + ChatColor.RESET + ChatColor.WHITE + e.getMessage());
			} else {
				e.setFormat(ChatColor.GREEN + "[GAME CHAT] " + ChatColor.RED + ChatColor.stripColor(player.getDisplayName()) + ChatColor.DARK_GRAY + ChatColor.BOLD + " » " + ChatColor.RESET + ChatColor.WHITE + e.getMessage());
			}

			while (true) {
				Player recipient;
				PlayerData recipientData;
				do {
					do {
						if (!iterator.hasNext()) {
							return;
						}

						recipient = iterator.next();
						recipientData = PlayerManager.getInstance().getPlayerData(recipient);
					} while (recipientData == null);
				} while (manhunt.equals(recipientData.getActiveManhunt()) || manhunt.equals(recipientData.getSpectateManhunt()));
				e.getRecipients().remove(recipient);
			}
		} else if (playerData.isInSpectateManhunt()) {
			e.setFormat(ChatColor.GOLD + "[SPECTATOR CHAT] " + ChatColor.BLUE + ChatColor.stripColor(player.getDisplayName()) + ChatColor.DARK_GRAY + ChatColor.BOLD + " » " + ChatColor.RESET + ChatColor.WHITE + e.getMessage());
			while (true) {
				Player recipient;
				PlayerData recipientData;
				do {
					do {
						if (!iterator.hasNext()) {
							return;
						}

						recipient = iterator.next();
						recipientData = PlayerManager.getInstance().getPlayerData(recipient);
					} while (recipientData == null);
				} while (playerData.getSpectateManhunt().equals(recipientData.getSpectateManhunt()));
				e.getRecipients().remove(recipient);
			}
		} else {
			e.setFormat(ChatColor.DARK_AQUA + "[LOBBY CHAT] " + e.getFormat());
			while (true) {
				Player recipient;
				PlayerData recipientData;
				do {
					do {
						if (!iterator.hasNext()) {
							return;
						}

						recipient = iterator.next();
						recipientData = PlayerManager.getInstance().getPlayerData(recipient);
					} while (recipientData == null);
				} while (!recipientData.isInSpectateManhunt() && !recipientData.isInActiveManhunt());
				e.getRecipients().remove(recipient);
			}
		}
	}

	@EventHandler
	public void onEntityCombust(EntityCombustEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
			if (playerData == null) {
				return;
			}

			if (playerData.isInSpectateManhunt()) {
				e.setCancelled(true);
			}
		}
	}
}
