package me.frankthedev.manhuntcore.listener;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.ManhuntCore;
import me.frankthedev.manhuntcore.data.manager.PlayerManager;
import me.frankthedev.manhuntcore.manhunt.Manhunt;
import me.frankthedev.manhuntcore.manhunt.manager.ManhuntManager;
import org.bukkit.*;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

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
		if (playerData != null) {
			playerData.handleManhuntEvent(e);
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		Player player = e.getPlayer();
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData != null) {
			playerData.handleManhuntEvent(e);
		}
	}

	@EventHandler
	public void onEnderdragonDeath(EnderDragonChangePhaseEvent e) {
		if (e.getNewPhase() == EnderDragon.Phase.DYING) {
			Manhunt manhunt = ManhuntManager.getInstance().getManhunt(e.getEntity().getWorld());
			if (manhunt != null) {
				ManhuntManager.getInstance().endManhunt(manhunt, true);
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player player = e.getEntity();
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData != null) {
			playerData.handleManhuntEvent(e);
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
		if (playerData != null) {
			playerData.handleManhuntEvent(e);
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
		String prefix = ChatColor.translateAlternateColorCodes('&', this.plugin.getVaultChat().getPlayerPrefix(player));
		if (playerData.isInActiveManhunt()) {
			e.setFormat(ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + "Game Chat" + ChatColor.DARK_GRAY + "] " + prefix + " " + (playerData.getActiveManhunt().isSpeedrunner(player.getUniqueId()) ? ChatColor.GREEN : ChatColor.RED) + ChatColor.stripColor("%s") + ChatColor.DARK_GRAY + ChatColor.BOLD + " » " + ChatColor.RESET + ChatColor.WHITE + "%s");
		} else if (playerData.isInSpectateManhunt()) {
			e.setFormat(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Spectator Chat" + ChatColor.DARK_GRAY + "] " + prefix + " " + ChatColor.BLUE + ChatColor.stripColor("%s") + ChatColor.DARK_GRAY + ChatColor.BOLD + " » " + ChatColor.RESET + ChatColor.WHITE + "%s");
		} else {
			e.setFormat(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "Lobby Chat" + ChatColor.DARK_GRAY + "] " + e.getFormat());
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
			} while (playerData.isInActiveManhunt() ? playerData.getActiveManhunt().equals(recipientData.getActiveManhunt()) || playerData.getActiveManhunt().equals(recipientData.getSpectateManhunt()) : (playerData.isInSpectateManhunt() ? playerData.getSpectateManhunt().equals(recipientData.getSpectateManhunt()) : !recipientData.isInActiveManhunt() && !recipientData.isInSpectateManhunt()));
			e.getRecipients().remove(recipient);
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
