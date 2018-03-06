package com.github.bkm016.bsteams.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.bkm016.bsteams.util.Config;

import lombok.Getter;
import me.skymc.taboolib.other.DateUtils;

public class Data {
	public static YamlConfiguration data;
	static File dataFile = new File("plugins" + File.separator + "BS-Teams" + File.separator + "Data.dat");
	@Getter //列表储存队伍
	static public ArrayList<TeamData> teamList = new ArrayList<TeamData>();
	//加载Data
	@SuppressWarnings("unchecked")
	static public void loadData(){
		teamList.clear();
		//检测data.dat是否存在
		data = new YamlConfiguration();
		if (!dataFile.exists()){
	        Bukkit.getConsoleSender().sendMessage("[BS-Teams] §cCreate Data.dat");
		}
		else {
	        Bukkit.getConsoleSender().sendMessage("[BS-Teams] §aFind Data.dat");
			try {
				data.load(dataFile);
			} 
			catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		for (String teamLeader : data.getKeys(false)){
			Long teamTimes = data.getLong(teamLeader+".Time");
			//当超时的时候
			if (teamTimes + DateUtils.formatDate(Config.getConfig(Config.TEAM_RETENTION_TIME)) < System.currentTimeMillis()){
				//TODO 删除队伍代码，测试
				data.set(teamLeader, null);
                continue;
			}
			List<String> teamMembers = data.getStringList(teamLeader+".Members");
			List<ItemStack> teamItems = (List<ItemStack>) data.getList(teamLeader+".Items");
			teamList.add(new TeamData(teamLeader, teamMembers, teamItems, teamTimes));
		}
		try {
			data.save(dataFile);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	//以下teamName就是队长名，也就是teamLeader
	//将Player添加到Team里 --示范测试代码
	static public void addTeamMember(Player player,String teamName){
		if (Data.isTeam(teamName)){
			Data.getTeam(teamName).addTeamMember(player.getName()).save();
		}
	}
	//将物品丢到Team物品列表 --示范测试代码
	static public void addItemToTeam(Player player,ItemStack item){
		if (Data.isTeam(player.getName())){
			Data.getTeam(player.getName()).addTeamItems(item).save();
		}
	}

	
	//创建队伍
	static public void createTeam(Player player){
		teamList.add(new TeamData(player.getName(), null, null, null).save());
	}
	
	//获取玩家所在队伍的物品列表 --示范测试代码
	static public List<ItemStack> getItemList(Player player){
		List<ItemStack> list = new ArrayList<ItemStack>();
		if (Data.isTeam(player.getName())){
			return Data.getTeam(player.getName()).getTeamItems();
		}
		return list;
	}
	
	//获取玩家所在的Team
	static public TeamData getTeam(String playerName){
		for (TeamData teamData : teamList){
			if (teamData.getTeamLeader().equals(playerName) || teamData.getTeamMembers().contains(playerName)){
				return teamData;
			}
		}
		return null;
	}
	
	//玩家是否在队伍内
	static public Boolean isTeam(String playerName){
		return getTeam(playerName) != null;
	}
	
	//玩家是否为队长
	static public Boolean isTeamLeader(String playerName){
		return getTeam(playerName) != null && getTeam(playerName).getTeamLeader().equals(playerName);
	}
	
	//删除队伍
	static public void removeTeam(TeamData teamData){
		data.set(teamData.getTeamLeader(), null);
		teamList.remove(teamData);
		try {
			data.save(dataFile);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//保存TeamData 根据情况视为定时保存/直接保存
	static public void saveTeam(TeamData teamData){
		String teamLeader = teamData.getTeamLeader();
		Long teamTimes = teamData.getTeamTimes();
		List<String> teamMembers = teamData.getTeamMembers();
		List<ItemStack> teamItems = teamData.getTeamItems();
		data.set(teamLeader+".Time", teamTimes);
		data.set(teamLeader+".Members", teamMembers);
		data.set(teamLeader+".Items", teamItems);
		try {
			data.save(dataFile);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	//保存TeamList 根据情况视为定时保存/直接保存
	static public void saveTeamList(){
		for (TeamData teamData : teamList){
			String teamLeader = teamData.getTeamLeader();
			Long teamTimes = teamData.getTeamTimes();
			List<String> teamMembers = teamData.getTeamMembers();
			List<ItemStack> teamItems = teamData.getTeamItems();
			data.set(teamLeader+".Time", teamTimes);
			data.set(teamLeader+".Members", teamMembers);
			data.set(teamLeader+".Items", teamItems);
		}
		try {
			data.save(dataFile);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
