package me.frankthedev.manhuntcore.gui.manager;

import me.frankthedev.manhuntcore.gui.GuiFolder;
import me.frankthedev.manhuntcore.gui.GuiPage;
import me.frankthedev.manhuntcore.gui.component.ManhuntGuiClickable;
import me.frankthedev.manhuntcore.manhunt.Manhunt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GuiManager {

	/**
	 * Stores Gui folders opened by specific players.
	 * The folders should be removed when the player closes
	 * a Gui folder.
	 */
	private final List<GuiFolder> folders;
	private static GuiManager instance;

	public GuiManager() {
		this.folders = new ArrayList<>();
	}

	public static void disable() {
		instance.folders.clear();
		instance = null;
	}

	public static GuiManager getInstance() {
		return instance == null ? instance = new GuiManager() : instance;
	}

	public GuiFolder createFolder(@NotNull Manhunt manhunt, @NotNull Player sender, @NotNull String name, int size, int startPadding, int endPadding, int increment, Class<? extends ManhuntGuiClickable> clazz, List<ItemStack> items) {
		if (startPadding + endPadding >= size || endPadding < 0 || startPadding < 0) {
			throw new UnsupportedOperationException("Invalid arguments for creating folder");
		}

		GuiFolder folder = new GuiFolder(name, size);
		GuiPage page = new GuiPage(folder);
		for (int i = startPadding; i < folder.getSize() - endPadding; i += increment) {
			try {
				page.addItem(i, clazz.asSubclass(ManhuntGuiClickable.class).getConstructor(Manhunt.class, Player.class, List.class).newInstance(manhunt, sender, items));
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}

		folder.setCurrentPage(page);
		this.folders.add(folder);
		return folder;
	}

	public List<GuiFolder> getFolders() {
		return this.folders;
	}

	public void removeFolder(GuiFolder folder) {
		this.folders.remove(folder);
	}

	public void addFolder(GuiFolder folder) {
		this.folders.add(folder);
	}
}
