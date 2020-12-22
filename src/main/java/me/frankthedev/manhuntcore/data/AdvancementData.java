package me.frankthedev.manhuntcore.data;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;

public class AdvancementData {

	private final String friendlyName;
	private final ChatColor nameColor;
	private final String description;
	private final int reward;
	private final AdvancementType advancementType;

	public AdvancementData(String friendlyName, String description, AdvancementType advancementType) {
		this(friendlyName, description, 0, advancementType);
	}

	public AdvancementData(String friendlyName, String description) {
		this(friendlyName, description, 0, AdvancementType.ADVANCEMENT);
	}

	public AdvancementData(String friendlyName, String description, int reward) {
		this(friendlyName, description, reward, AdvancementType.CHALLENGE);
	}

	public AdvancementData(String friendlyName, String description, int reward, AdvancementType advancementType) {
		this.friendlyName = friendlyName;
		this.description = description;
		this.reward = reward;
		this.nameColor = reward == 0 ? ChatColor.GREEN : ChatColor.DARK_PURPLE;
		this.advancementType = advancementType;
	}

	public boolean isExperience() {
		return this.reward != 0;
	}

	public int getReward() {
		return this.reward;
	}

	public BaseComponent[] getFormattedMessage(String playerName) {
		String pretext;
		switch (this.advancementType) {
			case GOAL:
				pretext = playerName + " has reached the goal ";
				break;
			case CHALLENGE:
				pretext = playerName + " has completed the challenge ";
				break;
			default:
				pretext = playerName + " has made the advancement ";
		}

		return new ComponentBuilder()
				.append(pretext)
				.color(ChatColor.WHITE.asBungee())
				.append("[" + this.friendlyName + "]")
				.color(this.nameColor.asBungee())
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.nameColor + this.friendlyName + "\n" + this.description)))
				.create();
	}

	public enum AdvancementType {
		ADVANCEMENT,
		GOAL,
		CHALLENGE;

		AdvancementType() {

		}
	}
}
