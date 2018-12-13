package com.github.bkm016.bsteams.event;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.util.Config;

/**
 * @author sky
 * @since 2018-03-08 20:59:53
 */
public class ListenerPlayerExperience implements Listener {
	
	private final BSTeamsPlugin plugin;

	public ListenerPlayerExperience(BSTeamsPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void onExpericne(PlayerExpChangeEvent e) {
		if (e.getAmount() == 0) return;
		// 获取队伍
		TeamData teamData = plugin.getTeamDataManager().getTeam(e.getPlayer().getName());
		if (teamData == null) {
			return;
		}
		
		// 插件启用功能
		if (Config.getConfig().getBoolean(Config.SHARE_EXPERIENCE_ENABLE) 
				// 队伍启用功能
				&& teamData.getTeamOption("SHARE-EXPERIENCE", true) 
				// 玩家启用功能
				&& BSTeamsPlugin.getApi().isExperienceShare(e.getPlayer())) {
			// 获取数据
			int amount = e.getAmount();
			// 经验获取者
			Player experience_ownder = e.getPlayer();
			
			// 获取范围内的友方成员
			List<Player> players = new ArrayList<>();
			for (String name : teamData.getTeamMembersAll()) {
				Player player = Bukkit.getPlayerExact(name);
				if (player != null 
						// 相同世界
						&& player.getWorld().equals(e.getPlayer().getWorld())
						// 经验共享范围内
						&& player.getLocation().distance(e.getPlayer().getLocation()) < Config.getConfig().getInt(Config.SHARE_EXPERIENCE_RADIUS)
						// 不是经验持有者
						&& !player.equals(experience_ownder)) {
					// 添加到有效队员列表中
					players.add(player);
				}
			}
			
			// 如果经验数小于有效成员数
			if (amount < players.size() || players.size() == 0) {
				return;
			} else {
				e.setAmount(0);
			}
			
			// 多余经验
			int remainder = amount % (players.size() + 1);
			// 平均经验
			int average = (amount - remainder) / (players.size() + 1);
			
			// 增加经验（队友）
			for (Player member : players) {
				// 关闭共享
				BSTeamsPlugin.getApi().setExperienceShare(member, false);
				// 给予经验
				member.giveExp(average);
				// 启用共享
				BSTeamsPlugin.getApi().setExperienceShare(member, true);
				// 提示
				BSTeamsPlugin.getLanguage().get("TEAM-EXPERIENCE-MEMBER")
					.addPlaceholder("$player", experience_ownder.getName())
					.addPlaceholder("$amount", String.valueOf(average))
					.send(member);
			}
			
			// 关闭共享
			BSTeamsPlugin.getApi().setExperienceShare(e.getPlayer(), false);
			// 增加经验（队长）
			experience_ownder.giveExp(average + remainder);
			// 启用共享
			BSTeamsPlugin.getApi().setExperienceShare(e.getPlayer(), true);
			// 提示
			BSTeamsPlugin.getLanguage().get("TEAM-EXPERIENCE-LEADER")
				.addPlaceholder("$players", String.valueOf(players.size()))
				.addPlaceholder("$amount1", String.valueOf(average + remainder))
				.addPlaceholder("$amount2", String.valueOf(average))
				.addPlaceholder("$all", String.valueOf(amount))
				.send(experience_ownder);
		}
	}
}
