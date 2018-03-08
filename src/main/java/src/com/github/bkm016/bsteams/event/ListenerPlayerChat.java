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

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.Data;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.util.Config;

/**
 * @author sky
 * @since 2018-03-08 20:17:09
 */
public class ListenerPlayerChat implements Listener {
	
	@EventHandler (priority = EventPriority.HIGH)
	public void chat(AsyncPlayerChatEvent e) {
		if (e.isCancelled() || !Config.getConfig().getBoolean(Config.TEAM_CHAT)) {
			return;
		}
		// 获取队伍
		TeamData teamData = Data.getTeam(e.getPlayer().getName());
		if (teamData == null || !e.getMessage().startsWith(BSTeamsPlugin.getLanguage().get("TEAM-CHAT-KEY").asString())) {
			return;
		}
		else {
			// 取消事件
			e.setCancelled(true);
			// 获取玩家
			List<Player> targets = new ArrayList<>();
			// 发送信息
			for (String name : teamData.getTeamMembersAll()) {
				Player player = Bukkit.getPlayerExact(name);
				if (player != null) {
					targets.add(player);
				}
			}
			
			// 音效
			for (Player player : targets) {
				String distance = "0";
				// 相同世界
				if (e.getPlayer().getWorld().equals(player.getWorld())) {
					distance = String.valueOf((int) e.getPlayer().getLocation().distance(player.getLocation()));
				}
				else {
					distance = "?";
				}
				
				// 信息
				BSTeamsPlugin.getLanguage().get("TEAM-CHAT-FORMAT")
					.addPlaceholder("$player", e.getPlayer().getName())
					.addPlaceholder("$distance", distance)
					.addPlaceholder("$message", e.getMessage().substring(BSTeamsPlugin.getLanguage().get("TEAM-CHAT-KEY").asString().length()))
					.send(targets);
				
				// 音效
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
			}
		}
	}

}
