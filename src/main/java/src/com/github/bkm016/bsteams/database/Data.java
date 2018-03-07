package com.github.bkm016.bsteams.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.util.Config;

import lombok.Getter;
import me.skymc.taboolib.other.DateUtils;
import me.skymc.taboolib.playerdata.DataUtils;

public class Data {
	
	public final static File DATA_FILE = new File("plugins" + File.separator + "BS-Teams" + File.separator + "Data.dat");
	
	@Getter
	private static YamlConfiguration data = new YamlConfiguration();
	
	@Getter
	private static ArrayList<TeamData> teamList = new ArrayList<TeamData>();
	
	@Getter
	private static BukkitRunnable runnable = null;
	
	@SuppressWarnings("unchecked")
	public static void loadData() {
		// 清空数据
		teamList.clear();
		
		// 检测数据库是否存在
		if (!DATA_FILE.exists()){
	        Bukkit.getConsoleSender().sendMessage("[BS-Teams] §cCreate Data.dat");
		}
		else {
	        Bukkit.getConsoleSender().sendMessage("[BS-Teams] §aFind Data.dat");
			try {
				data.load(DATA_FILE);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for (String teamLeader : data.getKeys(false)){
			Long teamTimes = data.getLong(teamLeader + ".Time");
			//当超时的时候
			if (teamTimes + DateUtils.formatDate(Config.getConfig(Config.TEAM_RETENTION_TIME)) < System.currentTimeMillis()){
				//TODO 删除队伍代码，测试
				data.set(teamLeader, null);
                continue;
			}
			// 载入成员
			List<String> teamMembers = data.getStringList(teamLeader + ".Members");
			// 载入物品
			List<ItemStack> teamItems = (List<ItemStack>) data.getList(teamLeader + ".Items");
			// 队伍数据
			TeamData teamData = new TeamData(teamLeader, teamMembers, teamItems, teamTimes);
			// 载入日志
			if (data.contains(teamLeader + ".Notes")) {
				for (String id : data.getConfigurationSection(teamLeader + ".Notes").getKeys(false)) {
					teamData.getItemNotes().add(new NoteData(
							data.getString(teamLeader + ".Notes." + id + ".Name"), 
							data.getString(teamLeader + ".Notes." + id + ".Item"),
							data.getLong(teamLeader + ".Notes." + id + ".Date")));
				}
			}
			teamList.add(teamData);
		}
		
		// 保存任务
		new BukkitRunnable(){
			@Override
			public void run() {
				saveTeamList();
			}
		}.runTaskTimerAsynchronously(BSTeamsPlugin.getInst(), 600, 600);//测试 - 每分钟
	}
	
	/**
	 * 创建队伍
	 * 
	 * @param player 玩家
	 */
	public static void createTeam(Player player){
		teamList.add(new TeamData(player.getName(), null, null, null));
	}
	
	/**
	 * 获取玩家队伍
	 * 
	 * @param playerName 玩家名
	 * @return boolean
	 */
	public static TeamData getTeam(String playerName){
		for (TeamData teamData : teamList){
			if (teamData.getTeamLeader().equals(playerName) || teamData.getTeamMembers().contains(playerName)){
				return teamData;
			}
		}
		return null;
	}
	
	/**
	 * 是否为队长
	 * 
	 * @param playerName 玩家名
	 * @return boolean
	 */
	public static boolean isTeamLeader(String playerName){
		return getTeam(playerName) != null && getTeam(playerName).getTeamLeader().equals(playerName);
	}
	
	/**
	 * 删除队伍
	 * 
	 * @param teamData 队伍
	 */
	public static void removeTeam(TeamData teamData){
		if (teamList.contains(teamData)){
			teamList.remove(teamData);
		}
	}
	
	/**
	 * 保存队伍列表
	 */
	public static void saveTeamList(){
		Long oldTimes = System.nanoTime();
		// 清空数据
		try {
			data.loadFromString("");
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
		}
		// 遍历队伍
		for (TeamData teamData : teamList){
			String teamLeader = teamData.getTeamLeader();
			Long teamTimes = teamData.getTeamTimes();
			List<String> teamMembers = teamData.getTeamMembers();
			List<ItemStack> teamItems = teamData.getTeamItems();
			data.set(teamLeader+".Time", teamTimes);
			data.set(teamLeader+".Members", teamMembers);
			data.set(teamLeader+".Items", teamItems);
			// 提取日志
			int i = 0;
			for (NoteData note : teamData.getItemNotes()) {
				data.set(teamLeader + ".Notes." + i + ".Name", note.getPlayer());
				data.set(teamLeader + ".Notes." + i + ".Item", note.getItemName());
				data.set(teamLeader + ".Notes." + i + ".Date", note.getDate());
				i++;
			}
		}
		// 保存配置
		DataUtils.saveConfiguration(data, DATA_FILE);
		Bukkit.getConsoleSender().sendMessage("[BS-Teams] 保存 §b"+teamList.size()+" §r条队伍数据，耗时: §b" + ((System.nanoTime() - oldTimes)/1000000D)+" §r(ms)");
	}

}
