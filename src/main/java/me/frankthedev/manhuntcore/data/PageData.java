package me.frankthedev.manhuntcore.data;

import me.frankthedev.manhuntcore.gui.GuiFolder;
import me.frankthedev.manhuntcore.gui.component.ManhuntGuiClickable;
import me.frankthedev.manhuntcore.manhunt.Manhunt;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageData {

	private final Map<Integer, ManhuntGuiClickable> itemMap;

	public PageData(GuiFolder folder, Manhunt manhunt, Player sender, int startPadding, int endPadding, int increment, Class<? extends ManhuntGuiClickable> clazz, List<ItemStack> items) {
		this.itemMap = new HashMap<>();
		for (int i = startPadding; i < folder.getSize() - endPadding; i += increment) {
			try {
				this.itemMap.put(i, clazz.asSubclass(ManhuntGuiClickable.class).getConstructor(Manhunt.class, Player.class, List.class).newInstance(manhunt, sender, items));
			} catch (ReflectiveOperationException e) {
				Bukkit.getLogger().warning("Failed to instantiate gui for " + clazz.getSimpleName());
			}
		}
	}

	public PageData(Map<Integer, ManhuntGuiClickable> itemMap) {
		this.itemMap = itemMap;
	}

	public Map<Integer, ManhuntGuiClickable> getItems() {
		return this.itemMap;
	}
}
