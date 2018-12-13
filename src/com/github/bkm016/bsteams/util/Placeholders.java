package com.github.bkm016.bsteams.util;

import org.bukkit.entity.Player;

import com.github.bkm016.bsteams.BSTeamsPlugin;

import me.clip.placeholderapi.external.EZPlaceholderHook;

public class Placeholders  extends EZPlaceholderHook{
	
	@SuppressWarnings("unused")
	private BSTeamsPlugin plugin;

	public Placeholders(BSTeamsPlugin ourPlugin) {
		super(ourPlugin, "bst");
		this.plugin = ourPlugin;
	}

	@Override
	public String onPlaceholderRequest(Player player, String string) {
		if (string.equals("team")) {
			if (plugin.getTeamDataManager().getTeam(player.getName()) != null){
				return plugin.getTeamDataManager().getTeam(player.getName()).getTeamLeader();
			}
			else {
				return "N/A";
			}
		}
		return "§c请核对你的变量是否正确!";
	}

}
