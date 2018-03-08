package com.github.bkm016.bsteams.event;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.TeamDataManager;
import com.github.bkm016.bsteams.database.TeamData;

import me.skymc.taboolib.damage.GetDamager;

/**
 * @author sky
 * @since 2018-03-08 20:54:04
 */
public class ListenerPlayerDamage implements Listener {
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		Player attacker = GetDamager.get(e);
		if (attacker == null || !(e.getEntity() instanceof Player)) {
			return;
		}
		
		// 获取队伍
		TeamData data1 = TeamDataManager.getTeam(attacker.getName());
		TeamData data2 = TeamDataManager.getTeam(e.getEntity().getName());
		
		// 判断队伍
		if (data1 == null || data2 == null || !data1.equals(data2)) {
			return;
		}
		
		// 如果禁用友方伤害
		if (!data1.getTeamOption("FRIENDLY-FIRE", false)) {
			e.setCancelled(true);
			// 提示
			BSTeamsPlugin.getLanguage().get("TEAM-DAMAGE-CANCEL").send(attacker);
			// 音效
			attacker.playSound(attacker.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
		}
	}
}
