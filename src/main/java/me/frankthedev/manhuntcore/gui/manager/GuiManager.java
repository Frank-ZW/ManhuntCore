package me.frankthedev.manhuntcore.gui.manager;

import com.google.common.collect.Maps;
import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.gui.GuiFolder;
import me.frankthedev.manhuntcore.gui.GuiPage;
import me.frankthedev.manhuntcore.gui.ModifyMenuClickable;
import me.frankthedev.manhuntcore.gui.component.ManhuntGuiClickable;
import me.frankthedev.manhuntcore.manhunt.Manhunt;
import me.frankthedev.manhuntcore.util.bukkit.ItemUtil;
import me.frankthedev.manhuntcore.util.java.TreeNode;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class GuiManager {

	public static final String MODIFY_TITLE = ChatColor.DARK_GREEN + "Manhunt Settings";

	private final Map<UUID, GuiFolder> folders;
	private final Map<UUID, TreeNode> playerNodes;
	private final Map<ItemStack, TreeNode> modifyTree;
	private static GuiManager instance;

	public GuiManager() {
		this.playerNodes = new HashMap<>();
		this.folders = new HashMap<>();
		this.modifyTree = new HashMap<>();
		this.loadGuiTree();
	}

	public static void disable() {
		instance.folders.clear();
		instance = null;
	}

	public void loadGuiTree() {
		Map<Integer, ItemStack> mutable = new LinkedHashMap<>();
		mutable.put(10, ItemUtil.createItemStack(Material.GRASS_BLOCK, ChatColor.GREEN + "Normal"));
		mutable.put(13, ItemUtil.createItemStack(Material.SADDLE, ChatColor.GREEN + "Large Biomes"));
		mutable.put(16, ItemUtil.createItemStack(Material.BEACON, ChatColor.GREEN + "Amplified"));
		mutable.put(22, ItemUtil.createItemStack(Material.ARROW, ChatColor.RED + "Go back"));
		TreeNode node = new TreeNode(Maps.newLinkedHashMap(mutable));
		this.modifyTree.put(ItemUtil.createItemStack(Material.GRASS_BLOCK, ChatColor.GREEN + "Terrain Generation"), node);

		mutable.clear();
		mutable.put(10, ItemUtil.createItemStack(Material.GRASS_BLOCK, ChatColor.GREEN + "Vanilla"));
		mutable.put(12, ItemUtil.createItemStack(Material.DIAMOND_HOE, ChatColor.GREEN + "Survival"));
		mutable.put(14, ItemUtil.createItemStack(Material.SHIELD, ChatColor.GREEN + "Practice"));
		mutable.put(16, ItemUtil.createItemStack(Material.DIAMOND_HELMET, ChatColor.GREEN + "Juggernaut"));
		mutable.put(22, ItemUtil.createItemStack(Material.ARROW, ChatColor.RED + "Go back"));
		node.setItems(Maps.newLinkedHashMap(mutable));
		this.modifyTree.put(ItemUtil.createItemStack(Material.DIAMOND_SWORD, ChatColor.GREEN + "Manhunt Gamemode"), node);

		mutable.clear();
		mutable.put(10, ItemUtil.createItemStack(Material.LINGERING_POTION, ChatColor.GREEN + "Potion Buffs"));
		mutable.put(12, ItemUtil.createItemStack(Material.DIRT, ChatColor.GREEN + "Blocks"));
		mutable.put(14, ItemUtil.createItemStack(Material.WOODEN_PICKAXE, ChatColor.GREEN + "Tools and Items", ChatColor.BLUE + "Spawn with items."));
		mutable.put(16, ItemUtil.createItemStack(Material.BONE, ChatColor.GREEN + "Wolf", ChatColor.BLUE + "Spawn with a tamed wolf"));
		mutable.put(22, ItemUtil.createItemStack(Material.ARROW, ChatColor.RED + "Go back"));
		node.setItems(Maps.newLinkedHashMap(mutable));
		this.modifyTree.put(ItemUtil.createItemStack(Material.DIAMOND_PICKAXE, ChatColor.GREEN + "Speedrunner Perks"), node);
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
		try {
			for (int i = startPadding; i < folder.getSize() - endPadding; i += increment) {
				page.addItem(i, clazz.asSubclass(ManhuntGuiClickable.class).getConstructor(Manhunt.class, Player.class, List.class).newInstance(manhunt, sender, items));

			}
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}

		folder.setCurrentPage(page);
		this.folders.put(sender.getUniqueId(), folder);
		return folder;
	}

	public Map<UUID, GuiFolder> getFolders() {
		return this.folders;
	}

	@Nullable
	public GuiFolder getFolder(UUID uniqueId) {
		return this.folders.get(uniqueId);
	}

	public void addFolder(UUID uniqueId, GuiFolder folder) {
		this.folders.put(uniqueId, folder);
	}

	public void removeFolder(@NotNull UUID uniqueId) {
		this.folders.remove(uniqueId);
	}

	public void openModifyGui(PlayerData senderData) {
		GuiFolder folder = new GuiFolder(GuiManager.MODIFY_TITLE, 27);
		GuiPage page = new GuiPage(folder);
		List<ItemStack> items = new ArrayList<>(this.modifyTree.keySet());
		for (int i = 0; i < 3; i++) {
			page.addItem(10 + 3 * i, new ModifyMenuClickable(senderData, items));
		}

		folder.setCurrentPage(page);
		folder.openGui(senderData.getPlayer());
		this.folders.put(senderData.getUniqueId(), folder);
	}

	public void onModifyClick(@NotNull Manhunt manhunt, @NotNull Player sender, @NotNull ItemStack clicked, @NotNull Class<? extends ManhuntGuiClickable> clazz) {
		GuiFolder folder = this.folders.get(sender.getUniqueId());
		if (folder == null) {
			return;
		}

		GuiPage page = folder.getCurrentPage();
		TreeNode parent = this.playerNodes.get(sender.getUniqueId());
		TreeNode child;
		if (parent == null) {
			/*
			 * The player is on the modify gui's main menu
			 */
			child = this.modifyTree.get(clicked);
		} else {
			child = parent.getChild(clicked.getType());
		}

		if (child == null) {
			return;
		}

		Map<Integer, ItemStack> items = child.getItems();
		List<ItemStack> itemList = items.values().stream().map(ItemStack::clone).collect(Collectors.toList());
		page.clearItems();
		try {
			TreeSet<Integer> sorted = new TreeSet<>(items.keySet());
			for (int index : sorted) {
				page.addItem(index, clazz.asSubclass(ManhuntGuiClickable.class).getConstructor(Manhunt.class, Player.class, List.class).newInstance(manhunt, sender.getPlayer(), itemList));
			}
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}

		folder.setCurrentPage(page);
		folder.openGui(sender.getPlayer(), 1);
		this.playerNodes.put(sender.getUniqueId(), child);
		this.folders.put(sender.getUniqueId(), folder);
	}

	public void previousGui(@NotNull Player sender) {
		GuiFolder folder = this.folders.get(sender.getUniqueId());
		if (folder == null) {
			return;
		}

		TreeNode child = this.playerNodes.get(sender.getUniqueId());
		if (child == null) {
			return;
		}

		TreeNode parent = child.getParent();
		Map<Material, TreeNode> children = parent.getChildren();
		for (Map.Entry<Material, TreeNode> entry : children.entrySet()) {

		}
	}
}
