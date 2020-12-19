package me.frankthedev.manhuntcore.util.java;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TreeNode<K, V> {

	private K key;
	private V value;
	private TreeNode<K, V> parent;
	private final Map<K, TreeNode<K, V>> children;

	public TreeNode(K key, V value) {
		this.key = key;
		this.value = value;
		this.children = new HashMap<>();
	}

	public TreeNode<K, V> put(K key, V value) {
		TreeNode<K, V> child = new TreeNode<>(key, value);
		child.setParent(this);
		this.children.put(key, child);
		return child;
	}

	public TreeNode<K, V> remove(K key) {
		return this.children.remove(key);
	}

	@Nullable
	public TreeNode<K, V> get(K key) {
		return this.children.get(key);
	}

	public K getKey() {
		return this.key;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public V getValue() {
		return this.value;
	}

	public void setValue(V value) {
		this.value = value;
	}

	public TreeNode<K, V> getParent() {
		return this.parent;
	}

	public void setParent(TreeNode<K, V> parent) {
		this.parent = parent;
	}

	@Override
	public int hashCode() {
		final int prime = 17;
		int result = 1;
		result = result * prime + this.key.hashCode();
		result = result * prime + this.value.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof TreeNode)) {
			return false;
		}

		TreeNode<?, ?> node = (TreeNode<?, ?>) obj;
		return this.key.equals(node.getKey()) && this.value.equals(node.getValue());
	}
}
