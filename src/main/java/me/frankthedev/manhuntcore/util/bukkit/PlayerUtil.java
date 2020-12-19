package me.frankthedev.manhuntcore.util.bukkit;

import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.manhunt.Manhunt;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerUtil {

	public static void resetAttributes(Player player) {
		player.setHealth(20.0D);
		player.setFoodLevel(20);
		player.setSaturation(5.0F);
		player.setExhaustion(0.0F);
		player.setLevel(0);
		player.setExp(0.0F);
		player.setTotalExperience(0);
		player.setBedSpawnLocation(null);
		player.getInventory().clear();
		player.setGameMode(GameMode.SURVIVAL);
		if (player.getFireTicks() > 0) {
			player.setFireTicks(0);
		}

		for (PotionEffect potionEffect : player.getActivePotionEffects()) {
			player.removePotionEffect(potionEffect.getType());
		}
	}

	public static void setSpectator(Player spectate, PlayerData playerData, Manhunt manhunt) {
		playerData.setSpectateManhunt(manhunt);
		spectate.setHealth(20.0D);
		spectate.setFoodLevel(20);
		spectate.setSaturation(5.0F);
		spectate.setExhaustion(0.0F);
		spectate.setLevel(0);
		spectate.setExp(0.0F);
		spectate.setBedSpawnLocation(null);
		spectate.getInventory().clear();
		spectate.setGameMode(GameMode.ADVENTURE);
		spectate.setFallDistance(0.0F);
		if (spectate.getFireTicks() > 0) {
			spectate.setFireTicks(0);
		}

		ItemStack compass = new ItemStack(Material.COMPASS);
		CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
		if (compassMeta != null) {
			compassMeta.setDisplayName(ItemUtil.SPECTATOR_TRACKER);
			compass.setItemMeta(compassMeta);
		}

		spectate.getInventory().setItem(0, compass);
		ItemStack bed = new ItemStack(Material.RED_BED);
		ItemMeta bedMeta = bed.getItemMeta();
		if (bedMeta != null) {
			bedMeta.setDisplayName(ItemUtil.SPECTATOR_LEAVE);
			bed.setItemMeta(bedMeta);
		}

		spectate.getInventory().setItem(8, bed);
		spectate.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, true, true));
	}

	public static void unsetSpectator(Player player, PlayerData playerData) {
		playerData.setSpectateManhunt(null);
		player.setHealth(20.0D);
		player.setFoodLevel(20);
		player.setSaturation(5.0F);
		player.setExhaustion(0.0F);
		player.setLevel(0);
		player.setExp(0.0F);
		player.setBedSpawnLocation(null);
		player.getInventory().clear();
		player.setGameMode(GameMode.ADVENTURE);
		player.setFallDistance(0.0F);
		player.setFlying(false);
		player.setAllowFlight(false);
		if (player.getFireTicks() > 0) {
			player.setFireTicks(0);
		}

		player.removePotionEffect(PotionEffectType.INVISIBILITY);
	}

	public static void applyKit(Player player, boolean isSpeedrunner) {
		player.getInventory().setHelmet(ItemUtil.createItemStack(Material.IRON_HELMET, isSpeedrunner ? ChatColor.GREEN + "Speedrunner Helmet" : ChatColor.RED + "Hunter Helmet"));
		player.getInventory().setChestplate(ItemUtil.createItemStack(Material.IRON_CHESTPLATE, isSpeedrunner ? ChatColor.GREEN + "Speedrunner Chestplate" : ChatColor.RED + "Hunter Chestplate"));
		player.getInventory().setLeggings(ItemUtil.createItemStack(Material.IRON_LEGGINGS, isSpeedrunner ? ChatColor.GREEN + "Speedrunner Leggings" : ChatColor.RED + "Hunter Leggings"));
		player.getInventory().setBoots(ItemUtil.createItemStack(Material.IRON_BOOTS, isSpeedrunner ? ChatColor.GREEN + "Speedrunner Boots" : ChatColor.RED + "Hunter Boots"));
		player.getInventory().setItem(0, new ItemStack(Material.IRON_SWORD));
		player.getInventory().setItem(1, new ItemStack(Material.CROSSBOW));
		player.getInventory().setItem(2, new ItemStack(Material.BOW));
		player.getInventory().setItem(3, new ItemStack(Material.IRON_AXE));
		player.getInventory().setItem(4, new ItemStack(Material.ARROW, 20));
		player.getInventory().setItem(6, new ItemStack(Material.OAK_LEAVES, 64));
		player.getInventory().setItem(7, new ItemStack(Material.COOKED_BEEF, 32));
		player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
	}
}
