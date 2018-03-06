package com.github.bkm016.bsteams.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Message {
	private static YamlConfiguration messages;
	final static File messageFile = new File("plugins" + File.separator + "BS-Teams" + File.separator + "Message.yml");
	final public static String ADMIN_NO_PER_CMD = "Admin.NoPermissionCommand";
	final public static String ADMIN_NO_CMD = "Admin.NoCommand";
	final public static String ADMIN_NO_FORMAT = "Admin.NoFormat";
	final public static String ADMIN_NO_ONLINE = "Admin.NoOnline";
	final public static String ADMIN_NO_CONSOLE = "Admin.NoConsole";
	final public static String PLUGIN_RELOAD = "Admin.PluginReload";
	final public static String COMMAND_RELOAD = "Command.reload";
	
	static public void createMessage(){
        Bukkit.getConsoleSender().sendMessage("[BS-Teams] §cCreate Message.yml");
		messages = new YamlConfiguration();
		messages.set(ADMIN_NO_PER_CMD, String.valueOf("&8[&d座椅&8] &c你没有权限执行此指令"));
		messages.set(ADMIN_NO_CMD, String.valueOf("&8[&d座椅&8] &c未找到此子指令:{0}"));
		messages.set(ADMIN_NO_FORMAT, String.valueOf("&8[&d座椅&8] &c格式错误!"));
		messages.set(ADMIN_NO_ONLINE, String.valueOf("&8[&d座椅&8] &c玩家不在线或玩家不存在!"));
		messages.set(ADMIN_NO_CONSOLE, String.valueOf("&8[&d座椅&8] &c控制台不允许执行此指令!"));
		messages.set(PLUGIN_RELOAD, String.valueOf("&8[&d座椅&8] §c插件已重载"));
		messages.set(COMMAND_RELOAD, String.valueOf("重新加载这个插件的配置"));
		try {messages.save(messageFile);} catch (IOException e) {e.printStackTrace();}
	}
	
	static public void loadMessage(){
		if(!messageFile.exists()){
				createMessage();
		}else{
	        Bukkit.getConsoleSender().sendMessage("[BS-Teams] §aFind Message.yml");
		}
		messages = new YamlConfiguration();
		try {messages.load(messageFile);} catch (IOException | InvalidConfigurationException e) {e.printStackTrace();Bukkit.getConsoleSender().sendMessage("§8[§6BS-Teams§8] §a读取message时发生错误");}
	}
	

	public static String getMsg(String loc,Object... args){
		String raw = messages.getString(loc);
		if (raw == null || raw.isEmpty()) {
			return "Null Message: " + loc;
		}
		raw = raw.replaceAll("&", "§");
		if (args == null) {
			return raw;
		}
		for (int i = 0; i < args.length; i++) {
			raw = raw.replace("{" + i + "}", String.valueOf(args[i])==null ? "null" : String.valueOf(args[i]));
		}
		return raw;
	}
	
	public static ArrayList<String> getList(String loc,String... args){
		ArrayList<String> list = (ArrayList<String>) messages.getStringList(loc);
		ArrayList<String> elist = new ArrayList<String>();
		if (list == null || list.isEmpty()) {
			list.add("Null Message: " + loc);
			return list;
		}
		if (args == null) {
			for(int e= 0;e <list.size();){
				elist.add(list.get(e).replace("&", "§"));
				e++;
			}
			return elist;
		}else{
			
		}
		//循环lore
		for(int e= 0;e <list.size();){
			String lore = list.get(e);
			for (int i= 0; i < args.length;i++){
				lore = lore.replace("&", "§").replace("{" + i + "}", args[i]==null ? "null" : args[i]);
			}
			elist.add(lore);
			e++;
		}
		return elist;
	}
	
	public static void playerTitle(Player player,String loc){
		String str = messages.getString(loc).replace("&", "§");
		if(str.contains(":")){
			player.sendTitle(str.split(":")[0], str.split(":")[1], 2, 30, 3);
		}else{
			player.sendTitle(str, "", 2, 30, 3);
		}
	}
}
