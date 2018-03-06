package com.github.bkm016.bsteams;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.bkm016.bsteams.command.BSTeamsCommand;

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
		// 生成配置文件
		saveDefaultConfig();
	}
	
	@Override
	public void onEnable() {
		// 注册命令
		Bukkit.getPluginCommand("bsteams").setExecutor(new BSTeamsCommand());
	}
	
	@Override
	public void onDisable() {
		
	}
}
