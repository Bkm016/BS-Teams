package com.github.bkm016.bsteams.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.github.bkm016.bsteams.BSTeamsPlugin;

import lombok.Getter;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.playerdata.DataUtils;

public class Config {
	
	@Getter
	private static File configFile;
	@Getter
	private static FileConfiguration config;
	
	public static final String ASYNCHRONOUSLY_SAVE = "AsynchronouslySave";
	public static final String TEAM_RETENTION_TIME = "TeamRetentionTime";
	public static final String PAGE_ARROW_NEXT = "Settings.page_next_arrow";
	public static final String PAGE_ARROW_BACK = "Settings.page_back_arrow";
	public static final String NOTE_ITEM = "Settings.note-item";
	public static final String NOTE_SIZE = "Settings.note-size";
	public static final String DATE_FORMAT = "Settings.date-format";
	public static final String TEAM_SIZE = "Settings.team-size";
	public static final String TEAM_CHAT = "Settings.team-chat";
	
	/**
	 * 重载配置文件
	 */
	public static void loadConfig(){
		// 配置文件
		configFile = new File(BSTeamsPlugin.getInst().getDataFolder(), "config.yml");
		// 检测文件
		if (!configFile.exists()) {
			// 默认配置
			createConfig();
		} else {
			Bukkit.getConsoleSender().sendMessage("[BS-Teams] §7载入配置文件...");
	        // 载入
	        config = ConfigUtils.load(BSTeamsPlugin.getInst(), configFile);
		}
	}

	/**
	 * 获取配置文件
	 * 
	 * @param loc 地址
	 * @return String
	 */
	public static String getConfig(String loc){
		String raw = config.getString(loc);
		if(raw == null || raw.isEmpty()){
			createConfig();
			raw = config.getString(loc);
			return raw;
		}
		raw = raw.replace("&", "§");
		return raw;
	}
	
	/**
	 * 获取文本集合
	 * 
	 * @param loc 地址
	 * @param args 参数
	 * @return {@link ArrayList}
	 */
	public static ArrayList<String> getList(String loc,String... args){
		ArrayList<String> list = (ArrayList<String>) config.getStringList(loc);
		if (list == null || list.isEmpty()) {
			createConfig();
			list = (ArrayList<String>) config.getStringList(loc);
		}
		for(int i=0;i<list.size();i++){
			list.set(i, list.get(i).replace("&", "§"));
		}
		return list;
	}
	
	/**
	 * 设置参数
	 * 
	 * @param loc 地址
	 * @param arg 参数
	 */
	public static void setConfig(String loc , Object arg){
		config.set(loc, arg);
		// 保存文件
		DataUtils.saveConfiguration(config, configFile);
	}
	
	/**
	 * 创建默认配置
	 */
	private static void createConfig(){
        Bukkit.getConsoleSender().sendMessage("[BS-Teams] §c配置不存在, 开始创建...");
        // 默认配置
		config = new YamlConfiguration();
		config.set(ASYNCHRONOUSLY_SAVE, true);
		config.set(TEAM_RETENTION_TIME, "1d");
		config.set(PAGE_ARROW_NEXT + ".material", "ARROW");
		config.set(PAGE_ARROW_NEXT + ".name", "&f下一页");
		config.set(PAGE_ARROW_NEXT + ".lore", Arrays.asList("", "&7点击翻页"));
		config.set(PAGE_ARROW_BACK + ".material", "ARROW");
		config.set(PAGE_ARROW_BACK + ".name", "&f上一页");
		config.set(PAGE_ARROW_BACK + ".lore", Arrays.asList("", "&7点击翻页"));
		config.set(NOTE_ITEM + ".material", "BOOK_AND_QUILL");
		config.set(NOTE_ITEM + ".name", "&f操作日志");
		config.set(NOTE_SIZE, 10);
		config.set(TEAM_SIZE, 5);
		config.set(TEAM_CHAT, true);
		config.set(DATE_FORMAT, "yyyy-MM-dd HH:mm:ss");
		// 保存文件
		DataUtils.saveConfiguration(config, configFile);
	}
}
