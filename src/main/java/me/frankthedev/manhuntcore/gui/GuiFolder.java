package me.frankthedev.manhuntcore.gui;

import me.frankthedev.manhuntcore.ManhuntCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class GuiFolder {

	private final Inventory inventory;
	private final String name;
	private final int size;
	private GuiPage currentPage;

	public GuiFolder(String name, int size) {
		this.name = name;
		this.size = size;
		this.inventory = Bukkit.createInventory(null, size, name);
	}

	public void openGui(Player player, int tickDelay) {
		Bukkit.getScheduler().runTaskLater(ManhuntCore.getInstance(), () -> this.openGui(player), tickDelay);
	}

	public void openGui(Player player) {
		player.closeInventory();
		player.openInventory(this.inventory);
	}

	public void setCurrentPage(GuiPage currentPage) {
		this.currentPage = currentPage;
		this.currentPage.updatePage();
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public String getName() {
		return this.name;
	}

	public int getSize() {
		return this.size;
	}

	public GuiPage getCurrentPage() {
		return this.currentPage;
	}

	@Override
	public int hashCode() {
		final int prime = 13;
		int result = 7;
		result = result * prime + this.inventory.hashCode();
		result = result * prime + this.name.hashCode();
		result = result * prime + size;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof GuiFolder)) {
			return false;
		}

		GuiFolder folder = (GuiFolder) obj;
		return this.inventory.equals(folder.getInventory()) &&
				this.name.equals(folder.getName()) &&
				this.size == folder.getSize();
	}
}
