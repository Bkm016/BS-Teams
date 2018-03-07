package com.github.bkm016.bsteams.event;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.Data;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.inventory.DropInventory;
import com.github.bkm016.bsteams.util.Message;

/**
 * @author Saukiya
 * @since 2018年3月7日
 */

public class InventoryClick implements Listener {

	@EventHandler
	public void onPlayerPickupItemEvent(InventoryClickEvent e) {
		Inventory inv = e.getInventory();
		Player player = (Player) e.getView().getPlayer();
		int slot = e.getRawSlot();
		ItemStack item = e.getCurrentItem();
		if(item == null)return;
		Material material= item.getType();
		if (inv.getName().equals(BSTeamsPlugin.getLanguage().get(Message.INVENTORY_DROP_NAME).asString())){
			e.setCancelled(true);
			if (!e.getClick().equals(ClickType.LEFT)){
				return;
			}
			if (slot >=9 && slot <45 ){
				if (!material.equals(Material.AIR)){
					TeamData teamData = Data.getTeam(player.getName());
					if (teamData != null){
						//TODO 检测玩家背包是否已满
						Inventory playerInv = player.getInventory();
						for(int i=0;i<36;i++){
							ItemStack pInvItem = playerInv.getItem(i);
							if(pInvItem==null){
								teamData.removeTeamItems(item);
								playerInv.addItem(item);
								inv.setItem(slot, null);
								//TODO 给其他成员发送消息
								return;
							}
						}
						//TODO 清空背包再来的消息
					}
				}
			}
			else if (slot <= 53){
				if (material.equals(Material.ARROW)){
					DropInventory.openDropInventory(player, item.getAmount());
				}
			}
		}
	}
}
