package me.frankthedev.manhuntcore.packet.manager;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;

public class NMSManager {

	private static NMSManager instance;

	public static NMSManager getInstance() {
		return instance == null ? instance = new NMSManager() : instance;
	}

	public Material getType(World world, BlockPosition position) {
		WorldServer server = ((CraftWorld) world).getHandle();
		if (server.isChunkLoaded(position.getX() >> 4, position.getZ() >> 4)) {
			return CraftMagicNumbers.getMaterial(server.getType(position).getBlock());
		}

		return Material.AIR;
	}
}
