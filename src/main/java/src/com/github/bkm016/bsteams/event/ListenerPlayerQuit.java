package com.github.bkm016.bsteams.event;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.TeamDataManager;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.util.Config;

/**
 * @author sky
 * @since 2018-03-08 20:17:09
 */
public class ListenerPlayerQuit implements Listener {
	
	@EventHandler (priority = EventPriority.HIGH)
	public void quit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		//更新最后时间
		TeamData teamData = TeamDataManager.getTeam(player.getName());
		if (teamData != null && teamData.getTeamLeader().equals(player.getName())){
			teamData.updateTeamTime();
		}
	}
}
