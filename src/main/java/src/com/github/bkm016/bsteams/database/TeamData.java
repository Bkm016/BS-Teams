package com.github.bkm016.bsteams.database;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.util.Config;

import lombok.Getter;

/**
 * @author Saukiya
 * @since 2018年3月6日
 */

public class TeamData {
	@Getter//队长
	private String teamLeader;
	@Getter //队员
	private List<String> teamMembers = new ArrayList<String>();
	@Getter//队伍物品
	private List<ItemStack> teamItems = new ArrayList<ItemStack>();
	@Getter//队长在的时候更新时间
	private long teamTimes;
	@Getter//队长在的时候更新时间
	private Boolean pickup = true;
	
	public TeamData(String teamLeader,List<String> teamMembers,List<ItemStack> teamItems,Long times){
		this.teamLeader = teamLeader;
		if (teamMembers != null)this.teamMembers = teamMembers;
		if (teamItems != null)this.teamItems = teamItems;
		if (times != null){
			this.teamTimes = times;
		}
		else {
			this.teamTimes = System.currentTimeMillis();
		}
	}
	
	//更换队长
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
	
	//快捷保存
	public TeamData save(){
		if (Config.getConfig(Config.ASYNCHRONOUSLY_SAVE).equalsIgnoreCase("true")){
			TeamData teamData = this;
			new BukkitRunnable(){
				@Override
				public void run() {
					Data.saveTeam(teamData);
				}
				
			}.runTaskAsynchronously(BSTeamsPlugin.getInst());
		}else{
			Data.saveTeam(this);
		}
		return this;
	}
	//快捷删除 
	public void remove(){
		if (Config.getConfig(Config.ASYNCHRONOUSLY_SAVE).equalsIgnoreCase("true")){
			TeamData teamData = this;
			new BukkitRunnable(){
				@Override
				public void run() {
					Data.removeTeam(teamData);
				}
				
			}.runTaskAsynchronously(BSTeamsPlugin.getInst());
		}else{
			Data.removeTeam(this);
		}
	}
}
