package me.frankthedev.manhuntcore.util.bukkit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ItemUtil {

	public static final String PLAYER_TRACKER = ChatColor.RED + "Player Tracker";
	public static final String SPECTATOR_TRACKER = ChatColor.GREEN + "Spectator Compass";
	public static final String SPECTATOR_LEAVE = ChatColor.RED + "Leave the game";

	public static ItemStack createPlayerTracker() {
		ItemStack compass = new ItemStack(Material.COMPASS);
		CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
		if (compassMeta != null) {
			compassMeta.setDisplayName(ItemUtil.PLAYER_TRACKER);
			compassMeta.setLodestoneTracked(false);
			compass.setItemMeta(compassMeta);
		}

		return compass;
	}

	public static ItemStack createItemStack(Material material, int amount, String displayName, String ... lore) {
		ItemStack itemStack = new ItemStack(material, amount);
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta != null) {
			itemMeta.setDisplayName(displayName);
			itemMeta.setLore(Arrays.asList(lore));
			itemStack.setItemMeta(itemMeta);
		}

		return itemStack;
	}

	public static ItemStack createItemStack(Material material, String displayName, String ... lore) {
		return ItemUtil.createItemStack(material, 1, displayName, lore);
	}
}
