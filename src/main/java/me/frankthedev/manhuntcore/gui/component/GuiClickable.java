package me.frankthedev.manhuntcore.gui.component;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface GuiClickable extends GuiItem {

	void onInventoryClick(InventoryClickEvent e);
}
