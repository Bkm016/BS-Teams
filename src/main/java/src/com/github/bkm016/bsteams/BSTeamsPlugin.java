package com.github.bkm016.bsteams;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.validation.Length;
import com.github.bkm016.bsteams.command.BSTeamsCommand;
import com.github.bkm016.bsteams.database.Data;
import com.github.bkm016.bsteams.event.ListenerInventoryClick;
import com.github.bkm016.bsteams.event.ListenerPlayerChat;
import com.github.bkm016.bsteams.event.ListenerPlayerItem;
import com.github.bkm016.bsteams.inventory.DropInventoryHolder;
import com.github.bkm016.bsteams.util.Config;
import com.github.bkm016.bsteams.util.Message;

import lombok.Getter;
import me.skymc.taboolib.string.language2.Language2;

/**
 * @author sky
 * @since 2018-03-06 19:41:07
 */
public class BSTeamsPlugin extends JavaPlugin {
	
	@Getter
	private static Plugin inst;
	
	@Getter
	private static Language2 language;
	
	@Override
	public void onLoad() {
		inst = this;
	}
	
	@Override
	public void onEnable() {
		// 载入 Language2
		language = new Language2(this);
		
		// 注册命令
		Bukkit.getPluginCommand("bsteams").setExecutor(new BSTeamsCommand());
		// 载入配置
		Config.loadConfig();
		// 载入数据
		Data.loadData();
		
		// 监听器
		Bukkit.getPluginManager().registerEvents(new ListenerPlayerItem(), this);
		Bukkit.getPluginManager().registerEvents(new ListenerPlayerChat(), this);
		Bukkit.getPluginManager().registerEvents(new ListenerInventoryClick(), this);
	}
	
	@Override
	public void onDisable() {
		Data.saveTeamList();
		// 循环玩家
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getOpenInventory().getTopInventory().getHolder() instanceof DropInventoryHolder) {
				player.closeInventory();
			}
		}
	}
}
