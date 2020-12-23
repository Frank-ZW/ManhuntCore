package me.frankthedev.manhuntcore.gui;

import me.frankthedev.manhuntcore.gui.component.GuiItem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class GuiPage {

	private final GuiFolder folder;
	private final Map<Integer, GuiItem> items;

	public GuiPage(GuiFolder folder) {
		this.folder = folder;
		this.items = new LinkedHashMap<>();
	}

	public void updatePage() {
		this.folder.getInventory().clear();
		this.items.forEach((slot, item) -> this.folder.getInventory().setItem(slot, item.getItemStack()));
		this.folder.getInventory().getViewers().forEach(viewer -> ((Player) viewer).updateInventory());
	}

	@Nullable
	public GuiItem getItem(int index) {
		return this.items.get(index);
	}

	public void addItem(int index, GuiItem item) {
		this.items.put(index, item);
	}

	public void clearItems() {
		this.items.clear();
	}

	public GuiFolder getFolder() {
		return this.folder;
	}

	public Map<Integer, GuiItem> getItems() {
		return this.items;
	}

	@Override
	public int hashCode() {
		final int prime = 13;
		int result = 1;
		result = result * prime + this.folder.hashCode();
		result = result * prime + this.items.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof GuiPage)) {
			return false;
		}

		GuiPage page = (GuiPage) obj;
		return this.folder.equals(page.getFolder()) &&
				this.items.equals(page.getItems());
	}
}
