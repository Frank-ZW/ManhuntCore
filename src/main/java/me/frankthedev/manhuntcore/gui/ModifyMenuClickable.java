package me.frankthedev.manhuntcore.gui;

import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.gui.component.GuiClickable;
import me.frankthedev.manhuntcore.gui.component.ManhuntGuiClickable;
import me.frankthedev.manhuntcore.gui.manager.GuiManager;
import me.frankthedev.manhuntcore.manhunt.Manhunt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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
			case DIAMOND_PICKAXE:       // Speedrunner Perks
				this.senderData.getPlayer().closeInventory();
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
}
