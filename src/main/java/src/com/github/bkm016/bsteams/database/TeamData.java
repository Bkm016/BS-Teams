package com.github.bkm016.bsteams.database;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;

import lombok.Getter;

/*本类由 Saukiya 在 2018年3月6日 下午10:42:23 时创建
 *TIM:admin@Saukiya.cn
 *GitHub:https://github.com/Saukiya
**/

public class TeamData {
	@Getter
	private String teamLeader;
	@Getter 
	private ArrayList<String> teamMembers = new ArrayList<String>();
	@Getter
	private ArrayList<ItemStack> teamItems = new ArrayList<ItemStack>();
	
	public TeamData(String teamLeader,ArrayList<String> teamMembers,ArrayList<ItemStack> teamItems){
		this.teamLeader = teamLeader;
		if(teamMembers != null)this.teamMembers = teamMembers;
		if(teamItems != null)this.teamItems = teamItems;
	}
	
	//更换队长
	public void setTeamLeader(String teamLeader){
		this.teamLeader = teamLeader;
	}
	
	//增加队员
	public void addTeamMember(String teamMember){
		if(!this.teamMembers.contains(teamMember)){
			this.teamMembers.add(teamMember);
		}
	}
	
	//减少队员
	public void removeTeamMember(String teamMember){
		if(this.teamMembers.contains(teamMember)){
			this.teamMembers.remove(teamMember);
		}
	}
	
	//清除队员 可能用不到
	public void clearTeamMember(){
		this.teamMembers.clear();
	}
	
	//增加物品到列表
	public void addTeamItems(ItemStack item){
		this.teamItems.add(item);
	}
	
	//设置物品列表
	public void setTeamItems(ArrayList<ItemStack> teamItems){
		this.teamItems = teamItems;
	}

}
