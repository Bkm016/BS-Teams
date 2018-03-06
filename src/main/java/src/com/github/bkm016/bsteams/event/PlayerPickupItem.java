package com.github.bkm016.bsteams.event;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.Data;
import com.github.bkm016.bsteams.database.TeamData;

/**
 * @author Saukiya
 * @since 2018年3月7日
 */

public class PlayerPickupItem implements Listener {

	@EventHandler
	public void onPlayerPickupItemEvent(PlayerPickupItemEvent e) {
		Player player = e.getPlayer();
		ItemStack item = e.getItem().getItemStack();
		//TODO 判断item是否为怪物掉落
		TeamData teamData = Data.getTeam(player.getName());
		if (teamData == null || !teamData.getPickup())return;
//		不行！！
//		item.setType(Material.AIR);
//		e.getItem().setItemStack(item);
		
//		可行方案一是 缺点是没有动画效果
//		e.setCancelled(true);
//		e.getItem().remove();
//		可行方案二 当前tick 搜索玩家背包 失败
		
//		可行方案三 下一个tick 搜索玩家背包 成功
		new BukkitRunnable(){
			@Override
			public void run() {
				//这里有个问题，是否取消我要不要放在第一行，还是放在Runnable?
				if(e.isCancelled())return;
				teamData.addTeamItems(item.clone()).save();
				Inventory inv  = player.getInventory();
				for (ItemStack invItem : inv){
					if (invItem == null || !invItem.getType().equals(item.getType()) || !invItem.getItemMeta().equals(item.getItemMeta()) || invItem.getAmount() < item.getAmount()){
		                continue;
					}
					invItem.setAmount(invItem.getAmount()-item.getAmount());
					break;
				}
			}
			
		}.runTask(BSTeamsPlugin.getInst());

		player.sendMessage("§d§o被队伍没收了啦 QAQ!!!");
	}

}
