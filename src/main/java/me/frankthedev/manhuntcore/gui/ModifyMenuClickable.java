package me.frankthedev.manhuntcore.gui;

import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.gui.component.GuiClickable;
import me.frankthedev.manhuntcore.gui.component.ManhuntGuiClickable;
import me.frankthedev.manhuntcore.gui.manager.GuiManager;
import me.frankthedev.manhuntcore.manhunt.Manhunt;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ModifyMenuClickable implements GuiClickable {

	private final PlayerData senderData;
	private final List<ItemStack> items;

	public ModifyMenuClickable(PlayerData senderData, List<ItemStack> items) {
		this.senderData = senderData;
		this.items = items;
	}

	@Override
	public void onInventoryClick(InventoryClickEvent e) {
		ItemStack clicked = e.getCurrentItem();
		if (clicked == null) {
			return;
		}

		switch (clicked.getType()) {
			case GRASS_BLOCK:           // Terrain generation
				GuiManager.getInstance().onModifyClick(this.senderData.getQueuedManhunt(), this.senderData.getPlayer(), clicked, TerrainClickable.class);
				break;
			case DIAMOND_SWORD:         // Manhunt gamemode
				GuiManager.getInstance().onModifyClick(this.senderData.getQueuedManhunt(), this.senderData.getPlayer(), clicked, GamemodeClickable.class);
				break;
			case DIAMOND_PICKAXE:       // Speedrunner Perks
				GuiManager.getInstance().onModifyClick(this.senderData.getQueuedManhunt(), this.senderData.getPlayer(), clicked, SpeedrunnerPerkClickable.class);
				break;
			case ARROW:
				break;
			default:
		}
	}

	@Override
	public ItemStack getItemStack() {
		return this.items.remove(0);
	}

	public static final class TerrainClickable extends ManhuntGuiClickable {

		public TerrainClickable(Manhunt manhunt, Player sender, List<ItemStack> items) {
			super(manhunt, sender, items);
		}

		@Override
		public void onInventoryClick(InventoryClickEvent e) {
			ItemStack clicked = e.getCurrentItem();
			if (clicked == null) {
				return;
			}

			ItemStack[] contents = e.getInventory().getContents();
			for (ItemStack content : contents) {
				content.removeEnchantment(Enchantment.DAMAGE_ALL);
			}

			clicked.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 0);
			ItemMeta meta = clicked.getItemMeta();
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			clicked.setItemMeta(meta);
			switch (clicked.getType()) {
				case BEACON:        // Amplified
					this.manhunt.broadcast(ChatColor.GREEN + "Set terrain generation to Amplified.");
					this.manhunt.setTerrainType(Manhunt.TerrainType.AMPLIFIED);
					break;
				case SADDLE:        // Large biomes
					this.manhunt.broadcast(ChatColor.GREEN + "Set terrain generation to Large Biomes.");
					this.manhunt.setTerrainType(Manhunt.TerrainType.LARGE_BIOMES);
					break;
				case GRASS_BLOCK:   // Normal
					this.manhunt.broadcast(ChatColor.GREEN + "Set terrain generation to Normal.");
					this.manhunt.setTerrainType(Manhunt.TerrainType.NORMAL);
					break;
				default:
			}
		}
	}

	public static final class GamemodeClickable extends ManhuntGuiClickable {

		public GamemodeClickable(Manhunt manhunt, Player sender, List<ItemStack> items) {
			super(manhunt, sender, items);
		}

		@Override
		public void onInventoryClick(InventoryClickEvent e) {

		}
	}

	public static final class SpeedrunnerPerkClickable extends ManhuntGuiClickable {

		public SpeedrunnerPerkClickable(Manhunt manhunt, Player sender, List<ItemStack> items) {
			super(manhunt, sender, items);
		}

		@Override
		public void onInventoryClick(InventoryClickEvent e) {

		}
	}
}
