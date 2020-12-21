package me.frankthedev.manhuntcore.listener;

import me.frankthedev.manhuntcore.gui.component.GuiClickable;
import me.frankthedev.manhuntcore.gui.GuiFolder;
import me.frankthedev.manhuntcore.gui.component.GuiItem;
import me.frankthedev.manhuntcore.gui.manager.GuiManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GuiListeners implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		GuiFolder folder = GuiManager.getInstance().getFolder(e.getWhoClicked().getUniqueId());
		if (folder != null) {
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
		if (e.getReason() != InventoryCloseEvent.Reason.PLUGIN) {
			GuiManager.getInstance().removeFolder(e.getPlayer().getUniqueId());
		}
	}
}
