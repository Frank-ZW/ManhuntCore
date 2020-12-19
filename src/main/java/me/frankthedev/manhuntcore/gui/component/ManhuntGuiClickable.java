package me.frankthedev.manhuntcore.gui.component;

import me.frankthedev.manhuntcore.manhunt.Manhunt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class ManhuntGuiClickable implements GuiClickable {

	protected final Player sender;
	protected final Manhunt manhunt;
	protected final List<ItemStack> items;

	public ManhuntGuiClickable(Manhunt manhunt, Player sender, List<ItemStack> items) {
		this.sender = sender;
		this.manhunt = manhunt;
		this.items = items;
	}

	@Override
	public ItemStack getItemStack() {
		return this.items.remove(0);
	}
}
