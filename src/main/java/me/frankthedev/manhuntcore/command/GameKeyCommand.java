package me.frankthedev.manhuntcore.command;

import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.data.manager.PlayerManager;
import me.frankthedev.manhuntcore.manhunt.Manhunt;
import me.frankthedev.manhuntcore.util.java.StringUtil;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GameKeyCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(StringUtil.PLAYER_ONLY);
			return true;
		}

		Player player = (Player) sender;
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData == null) {
			return true;
		}

		if (playerData.isInActiveManhunt()) {
			Manhunt manhunt = playerData.getActiveManhunt();
			player.sendMessage(new ComponentBuilder()
					.color(ChatColor.GREEN.asBungee())
					.append("Your Manhunt game key is ")
					.append(String.valueOf(manhunt.getGameKey()))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + "Click to copy!")))
					.event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(manhunt.getGameKey())))
					.create());
		} else if (playerData.isInSpectateManhunt()) {
			Manhunt manhunt = playerData.getSpectateManhunt();
			player.sendMessage(new ComponentBuilder()
					.color(ChatColor.GREEN.asBungee())
					.append("The game key of the Manhunt game being spectated is ")
					.append(String.valueOf(manhunt.getGameKey()))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + "Click to copy!")))
					.event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(manhunt.getGameKey())))
					.create());
		} else {
			player.sendMessage(ChatColor.RED + "You must be in an active Manhunt game or spectating one to use this command.");
		}

		return true;
	}
}
