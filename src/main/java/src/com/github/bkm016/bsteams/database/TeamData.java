package com.github.bkm016.bsteams.database;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.inventory.DropInventory;
import com.github.bkm016.bsteams.inventory.DropInventoryHolder;
import com.github.bkm016.bsteams.util.Config;

import lombok.Getter;
import me.skymc.taboolib.inventory.ItemUtils;

/**
 * @author Saukiya
 * @since 2018年3月6日
 */

public class TeamData {
	@Getter// 队长
	private String teamLeader;
	
	@Getter // 队员
	private List<String> teamMembers = new ArrayList<>();
	
	@Getter// 队伍物品
	private List<ItemStack> teamItems = new ArrayList<>();
	
	@Getter // 物品提取记录
	private List<NoteData> itemNotes = new LinkedList<>();
	
	@Getter// 队长在的时候更新时间
	private long teamTimes;
	
	@Getter// 队长在的时候更新时间
	private Boolean pickup = true;
	
	public TeamData(String teamLeader,List<String> teamMembers,List<ItemStack> teamItems,Long times){
		this.teamLeader = teamLeader;
		if (teamMembers != null) {
			this.teamMembers = teamMembers;
		}
		if (teamItems != null) {
			this.teamItems = teamItems;
		}
		if (times != null) {
			this.teamTimes = times;
		}
		else {
			this.teamTimes = System.currentTimeMillis();
		}
	}
	
	@Deprecated
	/*
	 *更换队长 注意开启此功能时，需要null原队伍，否则会造成背包复制刷物品
	 */
	public TeamData setTeamLeader(String teamLeader){
		this.teamLeader = teamLeader;
		return this;
	}
	
	//增加队员
	public TeamData addTeamMember(String teamMember){
		if (!this.teamMembers.contains(teamMember)){
			this.teamMembers.add(teamMember);
		}
		return this;
	}
	
	//减少队员
	public TeamData removeTeamMember(String teamMember){
		if (this.teamMembers.contains(teamMember)){
			this.teamMembers.remove(teamMember);
		}
		return this;
	}
	
	//清除队员 可能用不到
	public TeamData clearTeamMember(){
		this.teamMembers.clear();
		return this;
	}
	
	//增加物品到列表
	public TeamData addTeamItems(ItemStack item){
		this.teamItems.add(item);
		return this;
	}
	
	//从物品列表中删除物品
	public TeamData removeTeamItems(ItemStack item){
		this.teamItems.remove(item);
		return this;
	}
	
	//设置物品列表
	public TeamData setTeamItems(List<ItemStack> teamItems){
		this.teamItems = teamItems;
		return this;
	}
	
	//更新队伍保留时间
	public TeamData updateTeamTime(){
		this.teamTimes = System.currentTimeMillis();
		return this;
	}
	
	//快捷删除 
	public void remove(){
		Data.removeTeam(this);
	}
	
	/**
	 * 更新服务器内所有该队伍的物品背包
	 * 
	 * @return {@link TeamData}
	 */
	public TeamData updateInventory() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getOpenInventory().getTopInventory().getHolder() instanceof DropInventoryHolder) {
				DropInventoryHolder holder = (DropInventoryHolder) player.getOpenInventory().getTopInventory().getHolder();
				if (holder.getTeamData().equals(this)) {
					DropInventory.openInventory(player, holder.getPage(), holder.getTeamData());
				}
			}
		}
		return this;
	}
	
	/**
	 * 增加一条物品取出记录
	 * 
	 * @param player 玩家
	 * @param item 物品
	 * @return {@link TeamData}
	 */
	public TeamData addItemNote(Player player, ItemStack item) {
		itemNotes.add(0, new NoteData(player.getName(), ItemUtils.getCustomName(item), System.currentTimeMillis()));
		// 判断数量
		while (itemNotes.size() > Config.getConfig().getInt(Config.NOTE_SIZE)) {
			itemNotes.remove(itemNotes.size() - 1);
		}
		return this;
	}
}
