package com.github.bkm016.bsteams.util;

import org.bukkit.entity.Player;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.TeamDataManager;

import me.clip.placeholderapi.external.EZPlaceholderHook;

public class Placeholders  extends EZPlaceholderHook{
	
	@SuppressWarnings("unused")
	private BSTeamsPlugin ourPlugin;

	public Placeholders(BSTeamsPlugin ourPlugin) {
		super(ourPlugin, "bst");
		this.ourPlugin = ourPlugin;
	}

	@Override
	public String onPlaceholderRequest(Player player, String string) {
		if (string.equals("team")) {
			if (TeamDataManager.getTeam(player.getName()) != null){
				return TeamDataManager.getTeam(player.getName()).getTeamLeader();
			}
			else {
				return "N/A";
			}
		}
		return "§c请核对你的变量是否正确!";
	}

}
