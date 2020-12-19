package me.frankthedev.manhuntcore.packet;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.data.manager.PlayerManager;
import me.frankthedev.manhuntcore.packet.manager.NMSManager;
import me.frankthedev.manhuntcore.packet.manager.PacketManager;
import me.frankthedev.manhuntcore.util.java.ReflectionUtil;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class PacketHandler extends ChannelDuplexHandler {

	private final PlayerData playerData;
	private final PacketManager packetManager;

	public PacketHandler(PlayerData playerData, PacketManager packetManager) {
		this.playerData = playerData;
		this.packetManager = packetManager;
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg instanceof PacketPlayOutAnimation) {
			PacketPlayOutAnimation packet = (PacketPlayOutAnimation) msg;
			int entityId = ReflectionUtil.getLocalField(PacketPlayOutAnimation.class, packet, "a");
			Entity targetEntity = this.getEntity(this.playerData.getWorld(), entityId);
			if (targetEntity instanceof Player) {
				Player target = (Player) targetEntity;
				PlayerData targetData = PlayerManager.getInstance().getPlayerData(target);
				if (targetData != null && targetData.isInActiveManhunt()) {

				}

				target.sendMessage("Entity ID: " + entityId + ", B Field: " + ReflectionUtil.getLocalField(PacketPlayOutAnimation.class, packet, "b"));
			}
		} else if (msg instanceof PacketPlayOutBlockAction) {
			PacketPlayOutBlockAction packet = (PacketPlayOutBlockAction) msg;
			BlockPosition position = ReflectionUtil.getLocalField(PacketPlayOutBlockAction.class, packet, "a");
			int b = ReflectionUtil.getLocalField(PacketPlayOutBlockAction.class, packet, "b");
			int actionType = ReflectionUtil.getLocalField(PacketPlayOutBlockAction.class, packet, "c");
			if (actionType == 1) {
				this.playerData.getPlayer().sendMessage("Opened chest at (" + position.getX() + ", " + position.getY() + ", " + position.getZ() + ")");
			}

			/*
			 * C field: 1 means open chest, 0 means close chest
			 */
//			Bukkit.broadcastMessage("Position: (" + position.getX() + ", " + position.getY() + ", " + position.getZ() + ")" + ", B Field: " + b + ", C Field: " + actionType);
		} else if (msg instanceof PacketPlayOutNamedSoundEffect) {
			PacketPlayOutNamedSoundEffect packet = (PacketPlayOutNamedSoundEffect) msg;
			int x = ((int) ReflectionUtil.getLocalField(PacketPlayOutNamedSoundEffect.class, packet, "c")) / 8;
			int y = ((int) ReflectionUtil.getLocalField(PacketPlayOutNamedSoundEffect.class, packet, "d")) / 8;
			int z = ((int) ReflectionUtil.getLocalField(PacketPlayOutNamedSoundEffect.class, packet, "e")) / 8 - 1;
			SoundEffect soundEffect = ReflectionUtil.getLocalField(PacketPlayOutNamedSoundEffect.class, packet, "a");
			SoundCategory soundCategory = ReflectionUtil.getLocalField(PacketPlayOutNamedSoundEffect.class, packet, "b");
			this.playerData.getPlayer().sendMessage("Position: (" + x + ", " + y + ", " + z + "), Sound Category: " + soundCategory);
		}

		super.write(ctx, msg, promise);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof PacketPlayInUseItem) {
			PacketPlayInUseItem packet = (PacketPlayInUseItem) msg;
			BlockPosition position = packet.c().getBlockPosition();
			Material blockType = NMSManager.getInstance().getType(this.playerData.getWorld(), position);
			if (this.playerData.isInSpectateManhunt() && (blockType == Material.CHEST || blockType == Material.ENDER_CHEST || blockType == Material.TRAPPED_CHEST)) {
				this.packetManager.addChest(position, this.playerData.getSpectateManhunt());
			}
		}

		super.channelRead(ctx, msg);
	}

	@Nullable
	public Entity getEntity(World world, int entityId) {
		net.minecraft.server.v1_16_R3.Entity nmsEntity = ((CraftWorld) world).getHandle().getEntity(entityId);
		return nmsEntity == null ? null : nmsEntity.getBukkitEntity();
	}
}
