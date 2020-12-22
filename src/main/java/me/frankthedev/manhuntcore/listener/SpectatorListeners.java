package me.frankthedev.manhuntcore.listener;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.lib.PaperLib;
import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.data.manager.PlayerManager;
import me.frankthedev.manhuntcore.gui.component.GuiClickable;
import me.frankthedev.manhuntcore.gui.GuiFolder;
import me.frankthedev.manhuntcore.gui.GuiPage;
import me.frankthedev.manhuntcore.gui.manager.GuiManager;
import me.frankthedev.manhuntcore.manhunt.Manhunt;
import me.frankthedev.manhuntcore.manhunt.manager.ManhuntManager;
import me.frankthedev.manhuntcore.util.bukkit.ItemUtil;
import me.frankthedev.manhuntcore.util.bukkit.ManhuntPermissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SpectatorListeners implements Listener {

	private final Map<UUID, Long> spectators = new HashMap<>();

	public void clearMap() {
		this.spectators.clear();
	}

	@EventHandler
	public void onSpectatorBreak(BlockBreakEvent e) {
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(e.getPlayer());
		if (playerData != null && playerData.isInSpectateManhunt()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onSpectatorPlace(BlockPlaceEvent e) {
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(e.getPlayer());
		if (playerData != null && playerData.isInSpectateManhunt()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onSpectatorPickupItem(EntityPickupItemEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player) e.getEntity();
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData != null && playerData.isInSpectateManhunt()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onSpectatorPickupProjectile(PlayerPickupArrowEvent e) {
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(e.getPlayer());
		if (playerData != null && playerData.isInSpectateManhunt()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onSpectatorPickupExp(PlayerPickupExperienceEvent e) {
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(e.getPlayer());
		if (playerData != null && playerData.isInSpectateManhunt()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onSpectatorDrop(PlayerDropItemEvent e) {
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(e.getPlayer());
		if (playerData != null && playerData.isInSpectateManhunt()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
			if (playerData != null && playerData.isInSpectateManhunt()) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerDamageBySpectator(EntityDamageByEntityEvent e) {
		Player player = e.getDamager() instanceof Player ? (Player) e.getDamager() : null;
		if (player == null) {
			return;
		}

		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData != null && playerData.isInSpectateManhunt()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		PlayerData playerData = PlayerManager.getInstance().getPlayerData((Player) e.getEntity());
		if (playerData != null && playerData.isInSpectateManhunt()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		PlayerData playerData = PlayerManager.getInstance().getPlayerData((Player) e.getWhoClicked());
		if (playerData != null && playerData.isInSpectateManhunt()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onBellRing(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player spectator = e.getPlayer();
			PlayerData spectatorData = PlayerManager.getInstance().getPlayerData(spectator);
			if (spectatorData == null || !spectatorData.isInSpectateManhunt()) {
				return;
			}

			Block clicked = e.getClickedBlock();
			if (clicked != null && clicked.getType() == Material.BELL) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onRightClickCompass(PlayerInteractEvent e) {
		if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getItem() == null) {
			return;
		}

		Player spectator = e.getPlayer();
		PlayerData spectatorData = PlayerManager.getInstance().getPlayerData(spectator);
		if (spectatorData == null) {
			return;
		}

		if (!spectatorData.isInSpectateManhunt()) {
			return;
		}

		ItemStack item = e.getItem();
		switch (item.getType()) {
			case COMPASS:
				CompassMeta compassMeta = (CompassMeta) item.getItemMeta();
				if (ItemUtil.SPECTATOR_TRACKER.equals(compassMeta.getDisplayName())) {
					if (this.spectators.containsKey(spectator.getUniqueId())) {
						long secondsLeft = (spectator.hasPermission(ManhuntPermissions.REDUCED_SPECTATOR_COOLDOWN) ? 5 : 10) - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.spectators.get(spectator.getUniqueId()));
						if (secondsLeft > 0) {
							spectator.sendMessage(ChatColor.RED + "You must wait " + secondsLeft + " seconds before you can teleport to another player.");
							return;
						}
					}

					Manhunt manhunt = spectatorData.getSpectateManhunt();
					Set<UUID> totalPlayers = manhunt.getTotal();
					GuiFolder folder = new GuiFolder(ChatColor.DARK_GREEN + "Teleport to any player", 9 * (int) (2 + Math.ceil(totalPlayers.size() / 9.0D)));
					GuiPage page = new GuiPage(folder);
					List<ItemStack> items = new ArrayList<>();
					for (UUID uniqueId : totalPlayers) {
						OfflinePlayer offline = Bukkit.getOfflinePlayer(uniqueId);
						if (manhunt.isSpectator(uniqueId) || !offline.isOnline()) {
							continue;
						}

						ItemStack head = new ItemStack(Material.PLAYER_HEAD);
						SkullMeta headMeta = (SkullMeta) head.getItemMeta();
						headMeta.setDisplayName((manhunt.isSpeedrunner(uniqueId) ? ChatColor.GREEN : ChatColor.RED) + offline.getName());
						headMeta.setOwningPlayer(offline);
						head.setItemMeta(headMeta);
						items.add(head);
					}

					int index = 9;
					for (int i = 0; i < items.size(); i++) {
						index++;
						if (index % 9 == 8) {
							index += 2;
						}

						page.addItem(index, new CompassClickable(spectator, this, items));
					}

					GuiManager.getInstance().addFolder(spectator.getUniqueId(), folder);
					folder.setCurrentPage(page);
					folder.openGui(spectator);
				}

				break;
			case RED_BED:
				ItemMeta bedMeta = item.getItemMeta();
				if (ItemUtil.SPECTATOR_LEAVE.equals(bedMeta.getDisplayName())) {
					e.setCancelled(true);
					ManhuntManager.getInstance().removePlayer(spectatorData.getSpectateManhunt(), spectatorData);
				}

				break;
			default:
		}
	}

	@EventHandler
	public void onPlayerInteractPhysical(PlayerInteractEvent e) {
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(e.getPlayer());
		if (playerData != null && playerData.isInSpectateManhunt() && e.getAction() == Action.PHYSICAL) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent e) {
		if (!(e.getEntered() instanceof Player)) {
			return;
		}

		Player entered = (Player) e.getEntered();
		PlayerData enterData = PlayerManager.getInstance().getPlayerData(entered);
		if (enterData != null && enterData.isInSpectateManhunt()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityTarget(EntityTargetEvent e) {
		if (e.getTarget() instanceof Player) {
			Player target = (Player) e.getTarget();
			PlayerData targetData = PlayerManager.getInstance().getPlayerData(target);
			if (targetData != null && targetData.isInSpectateManhunt()) {
				e.setCancelled(true);
			}
		}
	}

	public void addSpectatorCooldown(UUID uniqueId) {
		this.spectators.put(uniqueId, System.currentTimeMillis());
	}

	public static class CompassClickable implements GuiClickable {

		private final List<ItemStack> items;
		private final Player sender;
		private final SpectatorListeners listeners;

		public CompassClickable(@NotNull Player sender, SpectatorListeners listeners, @NotNull List<ItemStack> items) {
			this.items = items;
			this.sender = sender;
			this.listeners = listeners;
		}

		@Override
		public void onInventoryClick(InventoryClickEvent e) {
			this.sender.closeInventory();
			ItemStack item = e.getCurrentItem();
			if (item == null || item.getType() != Material.PLAYER_HEAD) {
				return;
			}

			Player target = Bukkit.getPlayer(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
			if (target == null) {
				this.sender.sendMessage(ChatColor.RED + "This player is not online.");
			} else {
				PaperLib.teleportAsync(this.sender, target.getLocation()).thenAcceptAsync(result -> {
					if (result) {
						this.sender.sendMessage(ChatColor.GREEN + "Teleported you to " + target.getName());
						this.listeners.addSpectatorCooldown(this.sender.getUniqueId());
					} else {
						this.sender.sendMessage(ChatColor.RED + "Failed to teleport you to " + target.getName());
					}
				});
			}
		}

		@Override
		public ItemStack getItemStack() {
			return this.items.remove(0);
		}
	}
}
