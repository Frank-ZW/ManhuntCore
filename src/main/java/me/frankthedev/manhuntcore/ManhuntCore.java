package me.frankthedev.manhuntcore;

import com.google.common.collect.ImmutableList;
import io.papermc.lib.PaperLib;
import me.frankthedev.manhuntcore.command.*;
import me.frankthedev.manhuntcore.gui.manager.GuiManager;
import me.frankthedev.manhuntcore.listener.GuiListeners;
import me.frankthedev.manhuntcore.listener.PlayerListeners;
import me.frankthedev.manhuntcore.data.manager.PlayerManager;
import me.frankthedev.manhuntcore.listener.SpectatorListeners;
import me.frankthedev.manhuntcore.manhunt.manager.ManhuntManager;
import me.frankthedev.manhuntcore.packet.manager.NMSManager;
import me.frankthedev.manhuntcore.packet.manager.PacketManager;
import me.frankthedev.manhuntcore.queue.manager.QueueManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

public final class ManhuntCore extends JavaPlugin {

	private static ManhuntCore instance;
	private PlayerListeners playerListeners;
	private GuiListeners guiListeners;
	private SpectatorListeners spectatorListeners;
	private Location lobbySpawn;
	private final List<CommandExecutor> commands = ImmutableList.of(
			new SpectateCommand(),
			new ManhuntCommand(),
			new UnspectateCommand(),
			new GameKeyCommand(),
			new ForceEndCommand(),
			new ModifyCommand()
	);

	@Override
	public void onEnable() {
		PaperLib.suggestPaper(this);
		instance = this;
		this.registerCommands();
		ManhuntManager.enable(this);
		boolean shutdown = this.readConfig(true);
		NMSManager.getInstance();
		PacketManager.getInstance();
		GuiManager.getInstance();
		PlayerManager.enable(this);
		QueueManager.enable();
		this.saveDefaultConfig();
		this.playerListeners = new PlayerListeners(this);
		this.guiListeners = new GuiListeners();
		this.spectatorListeners = new SpectatorListeners();
		this.getServer().getPluginManager().registerEvents(this.playerListeners, this);
		this.getServer().getPluginManager().registerEvents(this.guiListeners, this);
		this.getServer().getPluginManager().registerEvents(this.spectatorListeners, this);

		if (shutdown) {
			this.getLogger().log(Level.WARNING, "Incomplete startup whilst reading in data from config.yml... disabling ManhuntCore");
			this.getServer().getPluginManager().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		this.writeConfig();
		this.getServer().getScheduler().cancelTasks(this);
		this.spectatorListeners.clearMap();
		HandlerList.unregisterAll(this.playerListeners);
		HandlerList.unregisterAll(this.guiListeners);
		HandlerList.unregisterAll(this.spectatorListeners);
		QueueManager.disable();
		PlayerManager.disable();
		ManhuntManager.disable();
		GuiManager.disable();
		PacketManager.disable();
		instance = null;
	}

	public void writeConfig() {
		this.getConfig().set("lobby-location.world-name", this.lobbySpawn.getWorld().getName());
		this.getConfig().set("lobby-location.X", this.lobbySpawn.getX());
		this.getConfig().set("lobby-location.Y", this.lobbySpawn.getY());
		this.getConfig().set("lobby-location.Z", this.lobbySpawn.getZ());
		this.getConfig().set("manhunt-Id", ManhuntManager.getInstance().getGameKey());
		this.saveConfig();
	}

	public boolean readConfig(boolean readGameKey) {
		String lobbyName = this.getConfig().getString("lobby-location.world-name");
		if (lobbyName == null) {
			this.getLogger().log(Level.WARNING, "Failed to read lobby world name from config.yml.");
			return true;
		}

		World lobby = Bukkit.getWorld(lobbyName);
		if (lobby == null) {
			this.getLogger().log(Level.WARNING, "Failed to retrieve lobby world with name " + lobbyName + ".");
			return true;
		}

		try {
			double x = this.getConfig().getDouble("lobby-location.X");
			double y = this.getConfig().getDouble("lobby-location.Y");
			double z = this.getConfig().getDouble("lobby-location.Z");
			this.lobbySpawn = new Location(lobby, x, y, z);
			if (readGameKey) {
				int gameKey = this.getConfig().getInt("manhunt-Id");
				ManhuntManager.getInstance().setGameKey(gameKey);
			}

			return false;
		} catch (Throwable t) {
			this.getLogger().log(Level.WARNING, "Failed to retrieve X, Y, Z coordinates for the lobby spawn location.", t);
			return true;
		}
	}

	public void registerCommands() {
		for (CommandExecutor executor : this.commands) {
			if (executor instanceof SpectateCommand) {
				PluginCommand spectate = this.getCommand("spectate");
				if (spectate != null) {
					spectate.setExecutor(executor);
				}
			} else if (executor instanceof UnspectateCommand) {
				PluginCommand unspectate = this.getCommand("unspectate");
				if (unspectate != null) {
					unspectate.setExecutor(executor);
				}
			} else if (executor instanceof ManhuntCommand) {
				PluginCommand manhunt = this.getCommand("manhunt");
				if (manhunt != null) {
					manhunt.setExecutor(executor);
				}
			} else if (executor instanceof ForceEndCommand) {
				PluginCommand forceend = this.getCommand("forceend");
				if (forceend != null) {
					forceend.setExecutor(executor);
				}
			} else if (executor instanceof ModifyCommand) {
				PluginCommand modify = this.getCommand("modify");
				if (modify != null) {
					modify.setExecutor(executor);
				}
			} else if (executor instanceof GameKeyCommand) {
				PluginCommand gamekey = this.getCommand("gamekey");
				if (gamekey != null) {
					gamekey.setExecutor(executor);
				}
			} else {
				this.getLogger().warning("");
				this.getLogger().warning("Failed to create plugin command for " + executor.getClass().getName());
				this.getLogger().warning("");
			}
		}
	}

	public Location getLobbySpawn() {
		return this.lobbySpawn;
	}

	public void setLobbySpawn(Location lobbySpawn) {
		this.lobbySpawn = lobbySpawn;
	}

	public static ManhuntCore getInstance() {
		return instance;
	}
}
