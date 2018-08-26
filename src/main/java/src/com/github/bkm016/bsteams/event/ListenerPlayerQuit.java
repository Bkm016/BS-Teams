package com.github.bkm016.bsteams.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.TeamDataManager;
import com.github.bkm016.bsteams.database.TeamData;

/**
 * @author sky
 * @since 2018-03-08 20:17:09
 */
public class ListenerPlayerQuit implements Listener {

	private final BSTeamsPlugin plugin;

	public ListenerPlayerQuit(BSTeamsPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void quit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		//更新最后时间
		TeamData teamData = plugin.getTeamDataManager().getTeam(player.getName());
		if (teamData != null && teamData.getTeamLeader().equals(player.getName())){
			teamData.updateTeamTime();
		}
	}
}
