package me.frankthedev.manhuntcore.util.bukkit;

import me.frankthedev.manhuntcore.data.AdvancementData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AdvancementUtil {

	private static final Map<String, AdvancementData> ADVANCEMENTS = new HashMap<>();

	static {
		ADVANCEMENTS.put("story/mine_stone", new AdvancementData("Stone Age", "Mine stone with your new pickaxe"));
		ADVANCEMENTS.put("story/upgrade_tools", new AdvancementData("Getting an Upgrade", "Construct a better pickaxe"));
		ADVANCEMENTS.put("story/smelt_iron", new AdvancementData("Acquire Hardware", "Smelt an iron ingot"));
		ADVANCEMENTS.put("story/obtain_armor", new AdvancementData("Suit Up", "Protect yourself with a piece of iron armor"));
		ADVANCEMENTS.put("story/lava_bucket", new AdvancementData("Hot Stuff", "Fill a bucket with lava"));
		ADVANCEMENTS.put("story/iron_tools", new AdvancementData("Isn't It Iron Pick", "Upgrade your pickaxe"));
		ADVANCEMENTS.put("story/deflect_arrow", new AdvancementData("Not Today, Thank You", "Deflect a projectile with a shield"));
		ADVANCEMENTS.put("story/form_obsidian", new AdvancementData("Ice Bucket Challenge", "Obtain a block of obsidian"));
		ADVANCEMENTS.put("story/mine_diamond", new AdvancementData("Diamonds!", "Acquire diamonds"));
		ADVANCEMENTS.put("story/enter_the_nether", new AdvancementData("We Need to Go Deeper", "Build, light and enter a Nether Portal"));
		ADVANCEMENTS.put("story/shiny_gear", new AdvancementData("Cover Me With Diamonds", "Diamond armor saves lives"));
		ADVANCEMENTS.put("story/enchant_item", new AdvancementData("Enchanter", "Enchant an item at an Enchanting Table"));
		ADVANCEMENTS.put("story/cure_zombie_villager", new AdvancementData("Zombie Doctor", "Weaken and then cure a Zombie Villager", AdvancementData.AdvancementType.GOAL));
		ADVANCEMENTS.put("story/follow_ender_eye", new AdvancementData("Eye Spy", "Follow an Eye of Ender", 0));
		ADVANCEMENTS.put("story/enter_the_end", new AdvancementData("The End?", "Enter the End Portal", 0));
		ADVANCEMENTS.put("nether/return_to_sender", new AdvancementData("Return to Sender", "Destroy a Ghast with a fireball", 50));
		ADVANCEMENTS.put("nether/find_bastion", new AdvancementData("Those Were the Days", "Enter a Bastion Remnant", 0));
		ADVANCEMENTS.put("nether/obtain_ancient_debris", new AdvancementData("Hidden in the Depths", "Obtain Ancient Debris", 0));
		ADVANCEMENTS.put("nether/fast_travel", new AdvancementData("Subspace Bubble", "Use the Nether to travel 7 km in the Overworld", 100));
		ADVANCEMENTS.put("nether/find_fortress", new AdvancementData("A Terrible Fortress", "Break your way into a Nether Fortress"));
		ADVANCEMENTS.put("nether/obtain_crying_obsidian", new AdvancementData("Who is Cutting Onions?", "Obtain Crying Obsidian"));
		ADVANCEMENTS.put("nether/distract_piglin", new AdvancementData("Oh Shiny", "Distract Piglins with gold"));
		ADVANCEMENTS.put("nether/ride_strider", new AdvancementData("This Boat Has Legs", "Ride a Strider with a Warped Fungus on a Stick"));
		ADVANCEMENTS.put("nether/uneasy_alliance", new AdvancementData("Uneasy Alliance", "Rescue a Ghast from the Nether, bring it safely home to the Oveworld... and then kill it", 100));
		ADVANCEMENTS.put("nether/loot_bastion", new AdvancementData("War Pigs", "Loot a chest in a Bastion Remnant"));
		ADVANCEMENTS.put("nether/use_lodestone", new AdvancementData("Country Lode, Take Me Home", "Use a compass on Lodestone"));
		ADVANCEMENTS.put("nether/netherite_armor", new AdvancementData("Cover Me in Debris", "Get a full suit of Netherite armor", 100));
		ADVANCEMENTS.put("nether/get_wither_skull", new AdvancementData("Spooky Scary Skeleton", "Obtain a Wither Skeleton's skull"));
		ADVANCEMENTS.put("nether/obtain_blaze_rod", new AdvancementData("Into Fire", "Relieve a Blaze of its rod"));
		ADVANCEMENTS.put("nether/charge_respawn_anchor", new AdvancementData("Not Quite \"Nine\" Lives", "Charge a Respawn Anchor to the maximum"));
		ADVANCEMENTS.put("nether/explore_nether", new AdvancementData("Hot Tourist Destinations", "Explore all Nether biomes", 500));
		ADVANCEMENTS.put("nether/summon_wither", new AdvancementData("Withering Heights", "Summon the Wither"));
		ADVANCEMENTS.put("nether/brew_potion", new AdvancementData("Local Brewery", "Brew a potion"));
		ADVANCEMENTS.put("nether/create_beacon", new AdvancementData("Bring Home the Beacon", "Construct and place a beacon"));
		ADVANCEMENTS.put("nether/all_potions", new AdvancementData("A Furious Cocktail", "Have every potion effect applied at the same time", 100));
		ADVANCEMENTS.put("nether/create_full_beacon", new AdvancementData("Beaconator", "Bring a beacon to full power", AdvancementData.AdvancementType.GOAL));
		ADVANCEMENTS.put("nether/all_effects", new AdvancementData("How Did We Get Here?", "Have every effect applied at the same time", 1000));
		ADVANCEMENTS.put("end/kill_dragon", new AdvancementData("Free the End", "Good luck"));
		ADVANCEMENTS.put("end/dragon_egg", new AdvancementData("The Next Generation", "Hold the Dragon Egg", AdvancementData.AdvancementType.GOAL));
		ADVANCEMENTS.put("end/enter_end_gateway", new AdvancementData("Remote Gateway", "Escape the island"));
		ADVANCEMENTS.put("end/respawn_dragon", new AdvancementData("The End... Again...", "Respawn the Ender Dragon", AdvancementData.AdvancementType.GOAL));
		ADVANCEMENTS.put("end/dragon_breath", new AdvancementData("You Need a Mint", "Collect dragon's breath in a glass bottle", AdvancementData.AdvancementType.GOAL));
		ADVANCEMENTS.put("end/find_end_city", new AdvancementData("The City at the End of the Game", "Go on in, what could happen?"));
		ADVANCEMENTS.put("end/elytra", new AdvancementData("Sky's the Limit", "Find elytra", AdvancementData.AdvancementType.GOAL));
		ADVANCEMENTS.put("end/levitate", new AdvancementData("Great View From Up Here", "Levitate up 50 blocks from the attacks of a Shulker", 50));
		ADVANCEMENTS.put("adventure/voluntary_exile", new AdvancementData("Voluntary Exile", "Kill a raid captain. Maybe consider staying away from villages for the time being..."));
		ADVANCEMENTS.put("adventure/kill_a_mob", new AdvancementData("Monster Hunter", "Kill any hostile monster"));
		ADVANCEMENTS.put("adventure/trade", new AdvancementData("What a Deal!", "Successfully trade with a Villager"));
		ADVANCEMENTS.put("adventure/honey_block_slide", new AdvancementData("Sticky Situation", "Jump into a Honey Block to break your fall"));
		ADVANCEMENTS.put("adventure/ol_betsy", new AdvancementData("Ol' Betsy", "Shoot a crossbow"));
		ADVANCEMENTS.put("adventure/sleep_in_bed", new AdvancementData("Sweet Dreams", "Sleep in a bed to change your respawn point"));
		ADVANCEMENTS.put("adventure/hero_of_the_village", new AdvancementData("Hero of the Village", "Successfully defend a village from a raid", 100));
		ADVANCEMENTS.put("adventure/throw_trident", new AdvancementData("A Throwaway Joke", "Throw a trident at something. Note: Throwing away your only weapon is not a good idea."));
		ADVANCEMENTS.put("adventure/shoot_arrow", new AdvancementData("Take Aim", "Shoot something with an arrow"));
		ADVANCEMENTS.put("adventure/kill_all_mobs", new AdvancementData("Monsters Hunted", "Kill one of every hostile monster", 100));
		ADVANCEMENTS.put("adventure/totem_of_undying", new AdvancementData("Postmortal", "Use a Totem of Undying to cheat death", AdvancementData.AdvancementType.GOAL));
		ADVANCEMENTS.put("adventure/summon_iron_golem", new AdvancementData("Hired Help", "Summon an Iron Golem to help defend a village", AdvancementData.AdvancementType.GOAL));
		ADVANCEMENTS.put("adventure/two_birds_one_arrow", new AdvancementData("Two Birds, One Arrow", "Kill two Phantoms with a piercing arrow", 65));
		ADVANCEMENTS.put("adventure/whos_the_pillager_now", new AdvancementData("Who's the Pillager Now?", "Give a Pillager a taste of their own medicine"));
		ADVANCEMENTS.put("adventure/arbalistic", new AdvancementData("Arbalistic", "Kill five unique mobs with one crossbow shot", 85));
		ADVANCEMENTS.put("adventure/adventuring_time", new AdvancementData("Adventuring Time", "Discover every biome", 500));
		ADVANCEMENTS.put("adventure/very_very_frightening", new AdvancementData("Very Very Frightening", "Strike a Villager with lightning"));
		ADVANCEMENTS.put("adventure/sniper_duel", new AdvancementData("Sniper Duel", "Kill a skeleton from at least 50 meters away", 50));
		ADVANCEMENTS.put("adventure/bullseye", new AdvancementData("Bullseye", "Hit the bullseye of a Target block from at least 30 meters away", 50));
		ADVANCEMENTS.put("husbandry/safely_harvest_honey", new AdvancementData("Bee Our Guest", "Use a Campfire to collect Honey from a Beehive using a Bottle without aggravating the bees"));
		ADVANCEMENTS.put("husbandry/breed_an_animal", new AdvancementData("The Parrots and the Bats", "Breed two animals together"));
		ADVANCEMENTS.put("husbandry/tame_an_animal", new AdvancementData("Best Friends Forever", "Tame an animal"));
		ADVANCEMENTS.put("husbandry/fishy_business", new AdvancementData("Fishy Business", "Catch a fish"));
		ADVANCEMENTS.put("husbandry/silk_touch_nest", new AdvancementData("Total Beelocation", "Move a Bee Nest, with 3 bees inside, using Silk Touch"));
		ADVANCEMENTS.put("husbandry/plant_seed", new AdvancementData("A Seedy Place", "Plant a seed and watch it grow"));
		ADVANCEMENTS.put("husbandry/bred_all_animals", new AdvancementData("Two by Two", "Breed all the animals", 100));
		ADVANCEMENTS.put("husbandry/complete_catalogue", new AdvancementData("A Complete Catalogue", "Tame all cat variants", 50));
		ADVANCEMENTS.put("husbandry/tactical_fishing", new AdvancementData("Tactical Fishing", "Catch a fish... without a fishing rod!"));
		ADVANCEMENTS.put("husbandry/balanced_diet", new AdvancementData("A Balanced Diet", "Eat everything that is edible, even if it's not good for you", 100));
		ADVANCEMENTS.put("husbandry/obtain_netherite_hoe", new AdvancementData("Serious Dedication", "Use a Netherite ingot to upgrade a hoe, and then reevaluate your life choices", 100));
	}

	@Nullable
	public static AdvancementData getAdvancementData(@NotNull String namespaced) {
		return AdvancementUtil.ADVANCEMENTS.get(namespaced);
	}
}
