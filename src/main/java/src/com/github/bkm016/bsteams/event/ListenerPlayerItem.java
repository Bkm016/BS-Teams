package com.github.bkm016.bsteams.event;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.api.BSTeamsPluginAPI;
import com.github.bkm016.bsteams.database.TeamDataManager;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.util.Config;

import me.skymc.taboolib.inventory.InventoryUtil;
import me.skymc.taboolib.inventory.ItemUtils;

/**
 * @author Saukiya
 * @since 2018年3月7日
 */

public class ListenerPlayerItem implements Listener {
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		BSTeamsPluginAPI.getInst().setDropNoShare(e.getItemDrop());
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerPickupItemEvent(PlayerPickupItemEvent e) {
		if (e.isCancelled()) {
			return;
		}
		
		// 获取玩家
		Player player = e.getPlayer();
		// 获取物品
		ItemStack item = e.getItem().getItemStack();
		
		// 获取队伍数据
		TeamData teamData = TeamDataManager.getTeam(player.getName());
		if (teamData == null) {
			return;
		}
		
		// 插件启用功能
		if (Config.getConfig().getBoolean(Config.SHARE_DROPS_ENABLE) 
				// 队伍启用功能
				&& teamData.getTeamOption("SHARE-DROPS", true) 
				// 玩家启用功能
				&& BSTeamsPluginAPI.getInst().isDropsShare(e.getPlayer())
				// 物品启用功能
				&& !BSTeamsPluginAPI.getInst().isDropNoShare(e.getItem())) {
			
			// 执行任务
			new BukkitRunnable(){
				@Override
				public void run() {
					// 物品是否移除成功
					if (InventoryUtil.hasItem(e.getPlayer(), item, item.getAmount(), true)) {
						// 添加物品到队伍
						teamData.addTeamItems(item.clone());
						// 获取成员
						List<Player> players = new ArrayList<>();
						for (String name : teamData.getTeamMembersAll()) {
							Player member = Bukkit.getPlayerExact(name);
							if (member != null) {
								players.add(member);
							}
						}
						// 提示信息
						BSTeamsPlugin.getLanguage().get("TEAM-DROPS-MESSAGE")
							.addPlaceholder("$player", e.getPlayer().getName())
							.addPlaceholder("$item", ItemUtils.getCustomName(item) + "§f * " + item.getAmount())
							.send(players);
					}
				}
			}.runTask(BSTeamsPlugin.getInst());
		}
	}
}
