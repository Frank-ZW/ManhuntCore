package me.frankthedev.manhuntcore.command.impl;

import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.manhunt.manager.ManhuntManager;
import me.frankthedev.manhuntcore.util.bukkit.ManhuntPermissions;
import me.frankthedev.manhuntcore.util.java.StringUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ManhuntListCommand implements ManhuntSubcommand {

	@Override
	public void execute(PlayerData senderData, String[] args) {
		Player sender = senderData.getPlayer();
		if (!sender.hasPermission(ManhuntPermissions.LIST)) {
			sender.sendMessage(StringUtil.NO_PERMISSION);
			return;
		}

		for (Integer gameKey : ManhuntManager.getInstance().getGameKeys()) {
			sender.sendMessage(this.generateInteractiveGameKey(gameKey));
		}
	}

	private BaseComponent[] generateInteractiveGameKey(int gamekey) {
		return new ComponentBuilder()
				.color(ChatColor.GREEN.asBungee())
				.append(String.valueOf(gamekey))
				.event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(gamekey)))
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN.asBungee() + "Click to copy!")))
				.create();
	}
}
