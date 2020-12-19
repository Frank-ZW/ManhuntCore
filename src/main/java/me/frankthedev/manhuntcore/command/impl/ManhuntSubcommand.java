package me.frankthedev.manhuntcore.command.impl;

import me.frankthedev.manhuntcore.data.PlayerData;

public interface ManhuntSubcommand {

	void execute(PlayerData senderData, String[] args);
}
