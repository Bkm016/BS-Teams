package com.github.bkm016.bsteams.inventory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.TeamDataManager;
import com.github.bkm016.bsteams.database.NoteData;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.util.Config;
import com.github.bkm016.bsteams.util.Message;

import me.skymc.taboolib.inventory.InventoryUtil;
import me.skymc.taboolib.inventory.ItemUtils;

/**
 * @author Saukiya
 * @since 2018年3月7日
 */

public class DropInventory {
	
	/**
	 * 打开掉落物背包
	 * 
	 * @param player 玩家
	 * @param page 页数
	 * @param teamData 队伍信息
	 */
	public static void openInventory(Player player, int page, TeamData teamData) {
		// 创建背包
		Inventory inventory = Bukkit.createInventory(new DropInventoryHolder(teamData, page), 54, BSTeamsPlugin.getLanguage().get(Message.INVENTORY_DROP_NAME).asString());
		// 最大页数
		int maxPage = teamData.getTeamItems().size() / 28 + 1;
		// 放置物品
		for (int i = 0, j = page * 28 - 28 ; i < 28 && j < teamData.getTeamItems().size() ; i++ , j++){
			inventory.setItem(InventoryUtil.SLOT_OF_CENTENTS.get(i), teamData.getTeamItems().get(j));
		}
		// 下一页
		if (page < maxPage) {
			inventory.setItem(51, ItemUtils.loadItem(Config.getConfig(), Config.PAGE_ARROW_NEXT));
		}
		// 上一页
		if (page > 1) {
			inventory.setItem(47, ItemUtils.loadItem(Config.getConfig(), Config.PAGE_ARROW_BACK));
		}
		// 日志
		ItemStack noteItem = ItemUtils.loadItem(Config.getConfig(), Config.NOTE_ITEM); {
			ItemMeta meta = noteItem.getItemMeta();
			// 获取日志
			if (teamData.getItemNotes().size() == 0) {
				meta.setLore(Arrays.asList("", BSTeamsPlugin.getLanguage().get("Inventory.Drop.Note-Empty").asString()));
			}
			else {
				SimpleDateFormat format = new SimpleDateFormat(Config.getConfig(Config.DATE_FORMAT));
				List<String> notes = new ArrayList<>();
				notes.add("");
				// 遍历日志
				int i = 1;
				for (NoteData noteData : teamData.getItemNotes()) {
					notes.add(BSTeamsPlugin.getLanguage().get("Inventory.Drop.Note")
							.addPlaceholder("$id", String.valueOf(i))
							.addPlaceholder("$player", noteData.getPlayer())
							.addPlaceholder("$item", noteData.getItemName())
							.addPlaceholder("$date", format.format(noteData.getDate()))
							.asString());
					i++;
				}
				meta.setLore(notes);
			}
			noteItem.setItemMeta(meta);
			inventory.setItem(49, noteItem);
		}
		// 打开界面
		player.openInventory(inventory);
	}
	
//	public static void openDropInventory(Player player,int page,TeamData... teamDatas){
//		TeamData teamData = null;
//		if (teamDatas.length > 0){
//			teamData = teamDatas[0];
//		}
//		else {
//			teamData = Data.getTeam(player.getName());
//		}
//		List<ItemStack> teamItems = teamData.getTeamItems();
//		int maxPage = teamItems.size()/36+1;//最大页数
//		Inventory inv = Bukkit.createInventory(null, 54,BSTeamsPlugin.getLanguage().get(Message.INVENTORY_DROP_NAME).asString());
//		ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE,1,(short) 15);
//		ItemMeta meta = item.getItemMeta();
//		meta.setDisplayName("§r");
//		item.setItemMeta(meta);
//		for (int i=0;i<9;i++){//设置玻璃
//			inv.setItem(i, item);
//		}
//		for (int i=45;i<54;i++){
//			inv.setItem(i, item);
//		}
//		item.setDurability((short) 0);
//		
//		item.setType(Material.ARROW);
//		if (page<maxPage){//下一页
//			meta.setDisplayName(BSTeamsPlugin.getLanguage().get(Message.INVENTORY_DROP_PAGE_DOWN).asString());
//			item.setItemMeta(meta);
//			item.setAmount(page+1);
//			inv.setItem(53, item);
//		}
//		if (page>1){//上一页
//			meta.setDisplayName(BSTeamsPlugin.getLanguage().get(Message.INVENTORY_DROP_PAGE_UP).asString());
//			item.setItemMeta(meta);
//			item.setAmount(page-1);
//			inv.setItem(45, item);
//		}
//		item.setAmount(1);
//		for (int i=0,j=page*28-28;i < 28 && j < teamItems.size();i++,j++){
//			inv.setItem(i+9, teamItems.get(j));
//		}
//		player.openInventory(inv);
//	}
}
