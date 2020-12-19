package me.frankthedev.manhuntcore.packet.manager;

import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.manhunt.Manhunt;
import me.frankthedev.manhuntcore.packet.PacketHandler;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.jetbrains.annotations.Nullable;

public class PacketManager {

	private final Object2ObjectOpenHashMap<BlockPosition, Manhunt> weakChestMap;
	private static PacketManager instance;

	public PacketManager() {
		this.weakChestMap = new Object2ObjectOpenHashMap<>();
	}

	public static void disable() {
		instance.weakChestMap.clear();
		instance = null;
	}

	public static PacketManager getInstance() {
		return instance == null ? instance = new PacketManager() : instance;
	}

	public EntityPlayer getEntityPlayer(PlayerData playerData) {
		return ((CraftPlayer) playerData.getPlayer()).getHandle();
	}

	public void injectPlayerData(PlayerData playerData) {
		Channel channel = this.getEntityPlayer(playerData).playerConnection.networkManager.channel;
		if (channel != null) {
			channel.pipeline().addBefore("packet_handler", "ManhuntCore-Handler", new PacketHandler(playerData, this));
		}
	}

	public void uninjectPlayerData(PlayerData playerData) {
		PlayerConnection connection = this.getEntityPlayer(playerData).playerConnection;
		if (connection != null && !connection.isDisconnected()) {
			Channel channel = connection.networkManager.channel;
			try {
				channel.pipeline().remove("ManhuntCore-Handler");
			} catch (Throwable ignored) {}
		}
	}

	public void addChest(BlockPosition position, Manhunt manhunt) {
		this.weakChestMap.put(position, manhunt);
	}

	public void removeChest(BlockPosition position) {
		this.weakChestMap.remove(position);
	}

	@Nullable
	public Manhunt getManhunt(BlockPosition position) {
		return this.weakChestMap.get(position);
	}
}
