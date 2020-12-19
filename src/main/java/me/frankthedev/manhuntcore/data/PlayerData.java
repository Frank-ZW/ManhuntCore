package me.frankthedev.manhuntcore.data;

import me.frankthedev.manhuntcore.manhunt.Manhunt;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerData {

	private final Player player;
	private final String name;
	private final UUID uniqueId;

	/**
	 * The active manhunt game if the player is in
	 * one or null.
	 */
	private Manhunt activeManhunt;

	/**
	 * The manhunt game the player is spectating
	 * or null.
	 */
	private Manhunt spectateManhunt;

	/**
	 * The manhunt game the player is currently
	 * queued in. A queued manhunt game is the
	 * period of time between when the player
	 * adds themselves to the game queue and
	 * when they get teleported into the worlds.
	 */
	private Manhunt queuedManhunt;

	public PlayerData(Player player) {
		this.player = player;
		this.name = player.getName();
		this.uniqueId = player.getUniqueId();
	}

	public Player getPlayer() {
		return this.player;
	}

	public String getName() {
		return this.name;
	}

	public UUID getUniqueId() {
		return this.uniqueId;
	}

	public World getWorld() {
		return this.player.getWorld();
	}

	public Manhunt getActiveManhunt() {
		return this.activeManhunt;
	}

	public void setActiveManhunt(Manhunt activeGame) {
		this.activeManhunt = activeGame;
	}

	public Manhunt getSpectateManhunt() {
		return this.spectateManhunt;
	}

	public void setSpectateManhunt(Manhunt spectateGame) {
		this.spectateManhunt = spectateGame;
	}

	public boolean isInActiveManhunt() {
		return this.activeManhunt != null;
	}

	public boolean isInSpectateManhunt() {
		return this.spectateManhunt != null;
	}

	public Manhunt getQueuedManhunt() {
		return this.queuedManhunt;
	}

	public void setQueuedManhunt(Manhunt queuedManhunt) {
		this.queuedManhunt = queuedManhunt;
	}

	public boolean isInQueuedManhunt() {
		return this.queuedManhunt != null;
	}

	@Override
	public int hashCode() {
		final int prime = 61;
		int result = 1;
		result = result * prime + this.player.hashCode();
		result = result * prime + this.name.hashCode();
		result = result * prime + this.uniqueId.hashCode();
		result = result * prime + (this.isInActiveManhunt() ? this.activeManhunt.hashCode() : 0);
		result = result * prime + (this.isInSpectateManhunt() ? this.spectateManhunt.hashCode() : 0);
		result = result * prime + (this.isInQueuedManhunt() ? this.queuedManhunt.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof PlayerData)) {
			return false;
		}

		PlayerData playerData = (PlayerData) obj;
		return this.player.equals(playerData.getPlayer()) &&
				this.name.equals(playerData.getName()) &&
				this.uniqueId.equals(playerData.getUniqueId()) &&
				(this.isInActiveManhunt() && this.getActiveManhunt().equals(playerData.getActiveManhunt())) &&
				(this.isInSpectateManhunt() && this.getSpectateManhunt().equals(playerData.getSpectateManhunt())) &&
				(this.isInQueuedManhunt() && this.getQueuedManhunt().equals(playerData.getQueuedManhunt()));
	}
}
