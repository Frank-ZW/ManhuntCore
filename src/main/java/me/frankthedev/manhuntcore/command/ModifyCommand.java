package me.frankthedev.manhuntcore.command;

import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.data.manager.PlayerManager;
import me.frankthedev.manhuntcore.gui.component.ManhuntGuiClickable;
import me.frankthedev.manhuntcore.gui.manager.GuiManager;
import me.frankthedev.manhuntcore.manhunt.Manhunt;
import me.frankthedev.manhuntcore.util.bukkit.ItemUtil;
import me.frankthedev.manhuntcore.util.bukkit.ManhuntPermissions;
import me.frankthedev.manhuntcore.util.java.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModifyCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(StringUtil.PLAYER_ONLY);
			return true;
		}

		Player player = (Player) sender;
		if (!player.hasPermission(ManhuntPermissions.MODIFY_GAME)) {
			player.sendMessage(ChatColor.RED + "This feature is currently unsupported for defaults. Consider making a small donation to the server to unlock game modifiers and attributes!");
			return true;
		}

		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData == null) {
			return true;
		}

		if (!playerData.isInQueuedManhunt()) {
			player.sendMessage(ChatColor.RED + "You must be queued for a Manhunt game to run this command.");
			return true;
		}

//		Manhunt manhunt = playerData.getQueuedManhunt();
//		GuiManager.getInstance().createFolder(manhunt, player, ChatColor.DARK_GREEN + "Manhunt Settings", 27, 10, 10, 3, ModifyClickable.class, new ArrayList<>(Arrays.asList(
//				ItemUtil.createItemStack(Material.DIAMOND_PICKAXE, ChatColor.GREEN + "Speedrunner Modifiers", ChatColor.BLUE + "Click to add or remove modifiers", ChatColor.BLUE + "for the speedrunner."),
//				ItemUtil.createItemStack(Material.GRASS_BLOCK, ChatColor.GREEN + "Terrain Generator", ChatColor.BLUE + "Click to change the world generation type", ChatColor.BLUE + "to Amplified, Large Biomes, Normal."),
//				ItemUtil.createItemStack(Material.DIAMOND_SWORD, ChatColor.GREEN + "Manhunt Gamemode", ChatColor.BLUE + "Click to change the gamemode to Practice PVP", ChatColor.BLUE + "Mini Manhunt, or plain-old Vanilla.")
//		))).openGui(player);

		GuiManager.getInstance().openModifyGui(playerData);
		return true;
	}

//	public static class ModifyClickable implements GuiClickable {
//
//		private final List<ItemStack> items;
//
//		public ModifyClickable(List<ItemStack> items) {
//			this.items = items;
//		}
//
//		@Override
//		public void onInventoryClick(InventoryClickEvent e) {
//			Player sender = (Player) e.getWhoClicked();
//			PlayerData senderData = PlayerManager.getInstance().getPlayerData(sender);
//			ItemStack clicked = e.getCurrentItem();
//			if (clicked == null || senderData == null) {
//				return;
//			}
//
//			GuiManager.getInstance().onModifyClick(senderData, clicked.getType());
//
////			GuiFolder folder = GuiManager.getInstance().getFolder(sender.getUniqueId());
////			TreeNode<Material, Map<Integer, ItemStack>> clickedNode = GuiManager.getInstance().getModifyNode(clicked.getType());
////			if (clickedNode == null) {
////				sender.sendMessage(ChatColor.RED + "An internal error occurred. Contact an administrator if this occurs.");
////				return;
////			}
////
////			switch (clicked.getType()) {
////				case DIAMOND_PICKAXE:       // Speedrunner perks
////					break;
////				case GRASS_BLOCK:           // Terrain generation
////					GuiManager.getInstance().handleModifyClick(folder, senderData.getQueuedManhunt(), sender, Material.GRASS_BLOCK, WorldTypeClickable.class);
////					break;
////				case DIAMOND_SWORD:         // Manhunt gamemode
////					break;
////				default:
////			}
//		}
//
//		@Override
//		public ItemStack getItemStack() {
//			return this.items.remove(0);
//		}
//	}

	public static class ModifyClickable extends ManhuntGuiClickable {

		public ModifyClickable(Manhunt manhunt, Player sender, List<ItemStack> items) {
			super(manhunt, sender, items);
		}

		@Override
		public void onInventoryClick(InventoryClickEvent e) {
			this.sender.closeInventory();
			ItemStack clickedItem = e.getCurrentItem();
			if (clickedItem == null) {
				return;
			}

			switch (clickedItem.getType()) {
				case DIAMOND_PICKAXE:
					GuiManager.getInstance().createFolder(this.manhunt, this.sender, ChatColor.DARK_GREEN + "Speedrunner Perks", 9, 2, 0, 4, BlockPerkClickable.class, new ArrayList<>(Arrays.asList(
							ItemUtil.createItemStack(Material.DIRT, ChatColor.GREEN + "Extra blocks", ChatColor.BLUE + "Give the speedrunner extra blocks at the start of the game."),
							ItemUtil.createItemStack(Material.POTION, ChatColor.GREEN + "Speed boost", ChatColor.BLUE + "Give the speedrunner an additional potion effect at the start of the game.")
					))).openGui(this.sender, 1);
					break;
				case GRASS_BLOCK:
					GuiManager.getInstance().createFolder(this.manhunt, this.sender, ChatColor.DARK_GREEN + "Terrain Generation", 9, 2, 2, 2, WorldTypeClickable.class, new ArrayList<>(Arrays.asList(
							ItemUtil.createItemStack(Material.GRASS_BLOCK, ChatColor.GREEN + "Normal", ChatColor.BLUE + "Set the world generation type to be normal."),
							ItemUtil.createItemStack(Material.SADDLE, ChatColor.GREEN + "Large Biomes", ChatColor.BLUE + "Set the world generation type to be large biomes."),
							ItemUtil.createItemStack(Material.BEACON, ChatColor.GREEN + "Amplified", ChatColor.BLUE + "Set the world generation type to be amplified.")
					))).openGui(this.sender, 1);
					break;
				case DIAMOND_SWORD:
					GuiManager.getInstance().createFolder(this.manhunt, this.sender, ChatColor.DARK_GREEN + "Manhunt Gamemode", 9, 2, 2, 2, GameModeClickable.class, new ArrayList<>(Arrays.asList(
							ItemUtil.createItemStack(Material.GRASS_BLOCK, ChatColor.GREEN + "Vanilla", ChatColor.BLUE + "Set the gamemode to be a default game of Manhunt."),
							ItemUtil.createItemStack(Material.DIAMOND_HOE, ChatColor.GREEN + "Survival", ChatColor.BLUE + "Set the gamemode to be survival."),
							ItemUtil.createItemStack(Material.SHIELD, ChatColor.GREEN + "Practice", ChatColor.BLUE + "Set the gamemode to be Practice PVP")
					))).openGui(this.sender, 1);
					break;
				default:
			}
		}
	}

	public static class BlockPerkClickable extends ManhuntGuiClickable {

		public BlockPerkClickable(Manhunt manhunt, Player sender, List<ItemStack> items) {
			super(manhunt, sender, items);
		}

		@Override
		public void onInventoryClick(InventoryClickEvent e) {
			this.sender.closeInventory();
			ItemStack clickedItem = e.getCurrentItem();
			if (clickedItem == null) {
				return;
			}

			switch (clickedItem.getType()) {
				case DIRT:
					if (this.manhunt.addOrRemovePerk(Manhunt.SpeedrunnerPerk.BLOCKS)) {
						this.manhunt.broadcast(ChatColor.GREEN + "Updated game modifier: the Speedrunner will now receive 32x dirt blocks upon starting the game.");
					} else {
						this.manhunt.broadcast(ChatColor.GREEN + "Updated game modifier: the Speedrunner will no longer receive 32x dirt blocks upon starting the game.");
					}

					break;
				case POTION:
					if (this.manhunt.addOrRemovePerk(Manhunt.SpeedrunnerPerk.POTION_EFFECTS)) {
						this.manhunt.broadcast(ChatColor.GREEN + "Updated game modifier: the Speedrunner will now have a Speed I potion effect for 20 seconds upon starting the game.");
					} else {
						this.manhunt.broadcast(ChatColor.GREEN + "Updated game modifier: the Speedrunner will now have a Speed effect upon starting the game.");
					}

					break;
				default:
					this.sender.sendMessage(ChatColor.RED + "An error occurred whilst updating the Manhunt speedrunner perks.");
			}
		}
	}

	public static class WorldTypeClickable extends ManhuntGuiClickable {

		public WorldTypeClickable(Manhunt manhunt, Player sender, List<ItemStack> items) {
			super(manhunt, sender, items);
		}

		@Override
		public void onInventoryClick(InventoryClickEvent e) {
			ItemStack clickedItem = e.getCurrentItem();
			if (clickedItem == null) {
				return;
			}

//			this.sender.sendMessage("You clicked " + clickedItem.getType());
			this.sender.closeInventory();
			switch (clickedItem.getType()) {
				case GRASS_BLOCK:
					this.manhunt.setManhuntType(Manhunt.ManhuntType.NORMAL);
					this.manhunt.broadcast(ChatColor.GREEN + "Updated terrain generation type to Normal");
					break;
				case SADDLE:
					this.manhunt.setManhuntType(Manhunt.ManhuntType.LARGE_BIOMES);
					this.manhunt.broadcast(ChatColor.GREEN + "Updated terrain generation type to Large Biomes");
					break;
				case BEACON:
					this.manhunt.setManhuntType(Manhunt.ManhuntType.AMPLIFIED);
					this.manhunt.broadcast(ChatColor.GREEN + "Updated terrain generation type to Amplified");
					break;
				default:
					this.sender.sendMessage(ChatColor.RED + "An error occurred whilst updating the Manhunt terrain generation.");
			}
		}
	}

	public static class GameModeClickable extends ManhuntGuiClickable {

		public GameModeClickable(Manhunt manhunt, Player sender, List<ItemStack> items) {
			super(manhunt, sender, items);
		}

		@Override
		public void onInventoryClick(InventoryClickEvent e) {
			this.sender.closeInventory();
			ItemStack clickedItem = e.getCurrentItem();
			if (clickedItem == null) {
				return;
			}

			switch (clickedItem.getType()) {
				case DIAMOND_HOE:
					this.manhunt.setGamemodeType(Manhunt.GamemodeType.SURVIVAL);
					this.manhunt.broadcast(ChatColor.GREEN + "Updated gamemode to Survival");
					break;
				case GRASS_BLOCK:
					this.manhunt.setGamemodeType(Manhunt.GamemodeType.VANILLA);
					this.manhunt.broadcast(ChatColor.GREEN + "Updated gamemode to default Manhunt");
					break;
				case SHIELD:
					this.manhunt.setGamemodeType(Manhunt.GamemodeType.PRACTICE);
					this.manhunt.broadcast(ChatColor.GREEN + "Updated gamemode to Practice PVP");
					break;
				default:
					this.sender.sendMessage(ChatColor.RED + "An error occurred whilst updating the Manhunt terrain generation.");
			}
		}
	}
}
