package me.frankthedev.manhuntcore.util.java;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TreeNode {

	private Map<Integer, ItemStack> items;              // Holds index and items for the current Gui
	private TreeNode parent;                            // Holds the node for the parent Gui
	private final Map<Material, TreeNode> children;     // Holds the List of children nodes

	public TreeNode(Map<Integer, ItemStack> items) {
		this.items = items;
		this.parent = null;
		this.children = new HashMap<>();
	}

	public TreeNode getParent() {
		return this.parent;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	public void addChild(Material type, TreeNode child) {
		this.children.put(type, child);
	}

	public void removeChild(Material material) {
		this.children.remove(material);
	}

	public Map<Integer, ItemStack> getItems() {
		return this.items;
	}

	public void setItems(Map<Integer, ItemStack> items) {
		this.items = items;
	}

	@Nullable
	public TreeNode getChild(Material material) {
		return this.children.get(material);
	}

	public Map<Material, TreeNode> getChildren() {
		return this.children;
	}
}
