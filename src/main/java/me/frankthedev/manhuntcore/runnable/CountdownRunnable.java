package me.frankthedev.manhuntcore.runnable;

import me.frankthedev.manhuntcore.manhunt.Manhunt;
import me.frankthedev.manhuntcore.manhunt.manager.ManhuntManager;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class CountdownRunnable extends BukkitRunnable {

	private final Manhunt manhunt;
	private int seconds;
	private final List<Integer> times = Arrays.asList(15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1);

	public CountdownRunnable(Manhunt manhunt) {
		this.manhunt = manhunt;
		this.seconds = 15;
	}

	@Override
	public void run() {
		if (this.times.contains(this.seconds)) {
			manhunt.broadcastEffectWithTitle(this.concatenateColor(this.seconds));
		}

		if (this.seconds <= 0) {
//			this.manhunt.startTeleport();
			this.manhunt.startManhunt();
			ManhuntManager.getInstance().addManhunt(this.manhunt);
			this.cancel();
		}

		this.seconds--;
	}

	public String concatenateColor(int time) {
		if (time > 9) {
			return ChatColor.GREEN.toString() + time;
		} else if (time > 5) {
			return ChatColor.YELLOW.toString() + time;
		} else if (time > 2) {
			return ChatColor.GOLD.toString() + time;
		} else {
			return ChatColor.RED.toString() + time;
		}
	}
}
