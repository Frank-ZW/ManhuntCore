package me.frankthedev.manhuntcore.listener;

import me.frankthedev.manhuntcore.gui.component.GuiClickable;
import me.frankthedev.manhuntcore.gui.GuiFolder;
import me.frankthedev.manhuntcore.gui.component.GuiItem;
import me.frankthedev.manhuntcore.gui.manager.GuiManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Optional;

public class GuiListeners implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Optional<GuiFolder> optional = GuiManager.getInstance().getFolders().stream().filter(folder -> e.getView().getTitle().equals(folder.getName())).findFirst();
		if (optional.isPresent()) {
			GuiFolder folder = optional.get();
			GuiItem item = folder.getCurrentPage().getItem(e.getSlot());
			if (item != null) {
				e.setCancelled(true);
				if (item instanceof GuiClickable) {
					((GuiClickable) item).onInventoryClick(e);
				}
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		Optional<GuiFolder> optional = GuiManager.getInstance().getFolders().stream().filter(folder -> e.getView().getTitle().equals(folder.getName())).findFirst();
		if (optional.isPresent()) {
			GuiFolder folder = optional.get();
			GuiManager.getInstance().removeFolder(folder);
		}
	}
}
