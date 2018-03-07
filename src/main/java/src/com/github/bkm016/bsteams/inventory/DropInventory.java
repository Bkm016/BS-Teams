package com.github.bkm016.bsteams.inventory;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.Data;
import com.github.bkm016.bsteams.database.TeamData;

import com.github.bkm016.bsteams.util.Message;

/**
 * @author Saukiya
 * @since 2018年3月7日
 */

public class DropInventory {
	public static void openDropInventory(Player player,int page,TeamData... teamDatas){
		TeamData teamData = null;
		if (teamDatas.length > 0){
			teamData = teamDatas[0];
		}
		else {
			teamData = Data.getTeam(player.getName());
		}
		List<ItemStack> teamItems = teamData.getTeamItems();
		int maxPage = teamItems.size()/36+1;//最大页数
		Inventory inv = Bukkit.createInventory(null, 54,BSTeamsPlugin.getLanguage().get(Message.INVENTORY_DROP_NAME).asString());
		ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE,1,(short) 15);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§r");
		item.setItemMeta(meta);
		for (int i=0;i<9;i++){//设置玻璃
			inv.setItem(i, item);
		}
		for (int i=45;i<54;i++){
			inv.setItem(i, item);
		}
		item.setDurability((short) 0);
		
		item.setType(Material.ARROW);
		if (page<maxPage){//下一页
			meta.setDisplayName(BSTeamsPlugin.getLanguage().get(Message.INVENTORY_DROP_PAGE_DOWN).asString());
			item.setItemMeta(meta);
			item.setAmount(page+1);
			inv.setItem(53, item);
		}
		if (page>1){//上一页
			meta.setDisplayName(BSTeamsPlugin.getLanguage().get(Message.INVENTORY_DROP_PAGE_UP).asString());
			item.setItemMeta(meta);
			item.setAmount(page-1);
			inv.setItem(45, item);
		}
		item.setAmount(1);
		for (int i=0,j=page*36-36;i < 36 && j < teamItems.size();i++,j++){
			inv.setItem(i+9, teamItems.get(j));
		}
		player.openInventory(inv);
	}
	
}
