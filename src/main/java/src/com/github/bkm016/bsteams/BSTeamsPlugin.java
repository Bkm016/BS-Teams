package com.github.bkm016.bsteams;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

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
		// ±£´æÅäÖÃÎÄ¼ş
		saveDefaultConfig();
	}
	
	@Override
	public void onEnable() {
		
	}
	
	@Override
	public void onDisable() {
		
	}
}
