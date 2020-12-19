package me.frankthedev.manhuntcore.command;

import me.frankthedev.manhuntcore.command.impl.*;
import me.frankthedev.manhuntcore.data.PlayerData;
import me.frankthedev.manhuntcore.data.manager.PlayerManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ManhuntCommand implements CommandExecutor {

	private final Map<String, ManhuntSubcommand> subcommands = new HashMap<>();

	public ManhuntCommand() {
		this.subcommands.put("setspawn", new ManhuntSpawnCommand());
		this.subcommands.put("start", new ManhuntStartCommand());
		this.subcommands.put("reload", new ManhuntReloadCommand());
		this.subcommands.put("list", new ManhuntListCommand());
		this.subcommands.put("queue", new ManhuntQueueCommand());
		this.subcommands.put("leave", new ManhuntLeaveCommand());
		this.subcommands.put("unqueue", new ManhuntUnqueueCommand());
	}

	public ManhuntSubcommand getExecutor(String name) {
		return this.subcommands.get(name);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You must be a player to run this command.");
			return true;
		}

		Player player = (Player) sender;
		PlayerData playerData = PlayerManager.getInstance().getPlayerData(player);
		if (playerData == null) {
			player.sendMessage(ChatColor.RED + "Failed to retrieve your player data information.");
			return true;
		}

		if (args.length == 0) {
			player.sendMessage(ChatColor.RED + "To start a manhunt game, type /manhunt start <hunters>");
			return true;
		}

		switch (StringUtils.lowerCase(args[0])) {
			case "start":
				this.getExecutor("start").execute(playerData, args);
				break;
			case "setspawn":
				this.getExecutor("setspawn").execute(playerData, args);
				break;
			case "rl":
			case "reload":
				this.getExecutor("reload").execute(playerData, args);
				break;
			case "ls":
			case "list":
				this.getExecutor("list").execute(playerData, args);
				break;
			case "queue":
				this.getExecutor("queue").execute(playerData, args);
				break;
			case "leave":
				this.getExecutor("leave").execute(playerData, args);
				break;
			case "unqueue":
				this.getExecutor("unqueue").execute(playerData, args);
				break;
			default:
				player.sendMessage(ChatColor.RED + "To start a new manhunt game, type /manhunt start <hunters>");
		}

		return true;
	}
}
