package com.github.bkm016.bsteams;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.bkm016.bsteams.command.BSTeamsCommand;
import com.github.bkm016.bsteams.database.Data;
import com.github.bkm016.bsteams.util.Config;
import com.github.bkm016.bsteams.util.Message;

import lombok.Getter;

/**
 * @author sky
 * @since 2018-03-06 19:41:07
 */
public class BSTeamsPlugin extends JavaPlugin {
	
	@Getter
	private static Plugin inst;
	
	@Override
	public void onLoad() {
		inst = this;
	}
	
	@Override
	public void onEnable() {
		// 注册命令
		Config.loadConfig();//加载Config
		Message.loadMessage();//加载Message
		Data.loadData();//加载Data
	}
	
	@Override
	public void onDisable() {
		
	}
}
