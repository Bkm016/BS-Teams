package com.github.bkm016.bsteams.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import lombok.Getter;

public class Config {
	
	@Getter
	private static YamlConfiguration config;
	private static File configFile = new File("plugins" + File.separator + "BS-Teams" + File.separator + "Config.yml");

	public static final String ASYNCHRONOUSLY_SAVE = "AsynchronouslySave";
	public static final String TEAM_RETENTION_TIME = "TeamRetentionTime";
	
	public static final String PAGE_ARROW_NEXT = "Settings.page_next_arrow";
	public static final String PAGE_ARROW_BACK = "Settings.page_back_arrow";
	public static final String NOTE_ITEM = "Settings.note-item";
	public static final String NOTE_SIZE = "Settings.note-size";
	public static final String DATE_FORMAT = "Settings.date-format";
	
	static public void createConfig(){
        Bukkit.getConsoleSender().sendMessage("[BS-Teams] §cCreate Config.yml");
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
		config.set(DATE_FORMAT, "yyyy-MM-dd HH:mm:ss");
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static public void loadConfig(){
		//检测Config.yml是否存在
		if (!configFile.exists()) {
			//创建Config.yml
			createConfig();
			return;
		} else {
	        Bukkit.getConsoleSender().sendMessage("[BS-Teams] §aFind Config.yml");
		}
		config = new YamlConfiguration();
		//读取config并存储
		try {config.load(configFile);} catch (IOException | InvalidConfigurationException e) {e.printStackTrace();Bukkit.getConsoleSender().sendMessage("[BS-Teams] §c读取config时发生错误");}
	}

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
	
	public static void setConfig(String loc , Object arg){
		config.set(loc, arg);
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
