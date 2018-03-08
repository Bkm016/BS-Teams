package com.github.bkm016.bsteams.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.TeamDataManager;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.inventory.DropInventory;
import com.github.bkm016.bsteams.inventory.DropInventoryHolder;
import com.github.bkm016.bsteams.util.Message;

import me.skymc.taboolib.inventory.InventoryUtil;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.itemnbtapi.NBTItem;

/**
 * @author Saukiya
 * @since 2018年3月7日
 */

public class ListenerInventoryClick implements Listener {
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (e.getInventory().getHolder() instanceof DropInventoryHolder) {
			e.setCancelled(true);
			// 获取玩家
			Player player = (Player) e.getWhoClicked();
			// 获取背包
			DropInventoryHolder holder = (DropInventoryHolder) e.getInventory().getHolder();
			// 判断点击位置
			if (e.getRawSlot() < 0 || e.getRawSlot() >= e.getInventory().getSize() || ItemUtils.isNull(e.getCurrentItem())) {
				return;
			}
			// 下一页
			if (e.getRawSlot() == 51) {
				DropInventory.openInventory(player, holder.getPage() + 1, holder.getTeamData());
			}
			// 上一页
			else if (e.getRawSlot() == 47) {
				DropInventory.openInventory(player, holder.getPage() - 1, holder.getTeamData());
			}
			// 物品
			else if (e.getRawSlot() != 49) {
				// 获取物品数据
				NBTItem nbt = new NBTItem(e.getCurrentItem());
				if (nbt.hasKey("not_drop_item")) {
					return;
				}
				// 背包已满
				if (!InventoryUtil.isEmpty(player, 0)) {
					// 提示信息
					BSTeamsPlugin.getLanguage().get("Inventory.Drop.Full").send(player);
				}
				else {
					// 给予物品
					player.getInventory().addItem(e.getCurrentItem());
					// 删除物品
					holder.getTeamData().removeTeamItems(e.getCurrentItem());
					// 添加日志
					holder.getTeamData().addItemNote(player, e.getCurrentItem());
					// 刷新界面
					holder.getTeamData().updateInventory();
					
					// 获取成员
					List<Player> players = new ArrayList<>();
					for (String name : holder.getTeamData().getTeamMembersAll()) {
						Player member = Bukkit.getPlayerExact(name);
						if (member != null) {
							players.add(member);
						}
					}
					
					// 提示信息
					BSTeamsPlugin.getLanguage().get("TEAM-DROPS-MESSAGE-TAKE")
						.addPlaceholder("$player", player.getName())
						.addPlaceholder("$item", ItemUtils.getCustomName(e.getCurrentItem()) + "§f * " + e.getCurrentItem().getAmount())
						.send(players);
				}
			}
		}
	}

//	@EventHandler
//	public void onPlayerPickupItemEvent(InventoryClickEvent e) {
//		Inventory inv = e.getInventory();
//		Player player = (Player) e.getView().getPlayer();
//		int slot = e.getRawSlot();
//		ItemStack item = e.getCurrentItem();
//		if(item == null)return;
//		Material material= item.getType();
//		if (inv.getName().equals(BSTeamsPlugin.getLanguage().get(Message.INVENTORY_DROP_NAME).asString())){
//			e.setCancelled(true);
//			if (!e.getClick().equals(ClickType.LEFT)){
//				return;
//			}
//			if (slot >=9 && slot <45 ){
//				if (!material.equals(Material.AIR)){
//					TeamData teamData = Data.getTeam(player.getName());
//					if (teamData != null){
//						//TODO 检测玩家背包是否已满
//						Inventory playerInv = player.getInventory();
//						for(int i=0;i<36;i++){
//							ItemStack pInvItem = playerInv.getItem(i);
//							if(pInvItem==null){
//								teamData.removeTeamItems(item);
//								playerInv.addItem(item);
//								inv.setItem(slot, null);
//								//TODO 给其他成员发送消息
//								return;
//							}
//						}
//						//TODO 清空背包再来的消息
//					}
//				}
//			}
//			else if (slot <= 53){
//				if (material.equals(Material.ARROW)){
//					DropInventory.openDropInventory(player, item.getAmount());
//				}
//			}
//		}
//	}
}
