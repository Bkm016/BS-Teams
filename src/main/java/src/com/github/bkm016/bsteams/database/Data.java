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
import org.bukkit.scheduler.BukkitRunnable;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.util.Config;

import lombok.Getter;
import me.skymc.taboolib.other.DateUtils;

public class Data {
	public static YamlConfiguration data;
	static final File DATA_FILE = new File("plugins" + File.separator + "BS-Teams" + File.separator + "Data.dat");
	@Getter //列表储存队伍
	public static ArrayList<TeamData> teamList = new ArrayList<TeamData>();
	private static BukkitRunnable runnable = null;
	//加载Data
	@SuppressWarnings("unchecked")
	public static void loadData(){
		teamList.clear();
		//检测data.dat是否存在
		data = new YamlConfiguration();
		if (!DATA_FILE.exists()){
	        Bukkit.getConsoleSender().sendMessage("[BS-Teams] §cCreate Data.dat");
		}
		else {
	        Bukkit.getConsoleSender().sendMessage("[BS-Teams] §aFind Data.dat");
			try {
				data.load(DATA_FILE);
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
			data.save(DATA_FILE);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		new BukkitRunnable(){
			@Override
			public void run() {
				saveTeamList();
			}
		}.runTaskTimerAsynchronously(BSTeamsPlugin.getInst(), 600, 600);//测试 - 每分钟
	}
	
	//创建队伍
	public static void createTeam(Player player){
		teamList.add(new TeamData(player.getName(), null, null, null));
	}
	
	//获取玩家所在的Team
	public static TeamData getTeam(String playerName){
		for (TeamData teamData : teamList){
			if (teamData.getTeamLeader().equals(playerName) || teamData.getTeamMembers().contains(playerName)){
				return teamData;
			}
		}
		return null;
	}
	
	//玩家是否为队长
	public static Boolean isTeamLeader(String playerName){
		return getTeam(playerName) != null && getTeam(playerName).getTeamLeader().equals(playerName);
	}
	
	//删除队伍
	public static void removeTeam(TeamData teamData){
		if (teamList.contains(teamData)){
			teamList.remove(teamData);
		}
	}
	

	//保存TeamList 根据情况视为定时保存/直接保存
	public static void saveTeamList(){
		Long oldTimes = System.currentTimeMillis();
		data = new YamlConfiguration();
		for (TeamData teamData : teamList){
			String teamLeader = teamData.getTeamLeader();
			Long teamTimes = teamData.getTeamTimes();
			List<String> teamMembers = teamData.getTeamMembers();
			List<ItemStack> teamItems = teamData.getTeamItems();
			data.set(teamLeader+".Time", teamTimes);
			data.set(teamLeader+".Members", teamMembers);
			data.set(teamLeader+".Items", teamItems);
			
			List<String> notes = new ArrayList<>();
			for (NoteData note : teamData.getItemNotes()) {
				
			}
		}
		try {
			data.save(DATA_FILE);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		Bukkit.getConsoleSender().sendMessage("[BS-Teams] 保存 §b"+teamList.size()+" §r条队伍数据，耗时: §b" + (System.currentTimeMillis()-oldTimes)+" §r(ms)");
	}

}
