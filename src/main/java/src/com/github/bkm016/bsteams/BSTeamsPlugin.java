package com.github.bkm016.bsteams;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.validation.Length;
import com.github.bkm016.bsteams.command.BSTeamsCommand;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.database.TeamDataManager;
import com.github.bkm016.bsteams.event.ListenerInventoryClick;
import com.github.bkm016.bsteams.event.ListenerPlayerChat;
import com.github.bkm016.bsteams.event.ListenerPlayerDamage;
import com.github.bkm016.bsteams.event.ListenerPlayerExperience;
import com.github.bkm016.bsteams.event.ListenerPlayerItem;
import com.github.bkm016.bsteams.event.ListenerPlayerQuit;
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
		TeamDataManager.loadData();
		// 注册冷却
		TeamDataManager.registerCooldown();
		
		// 监听器
		Bukkit.getPluginManager().registerEvents(new ListenerPlayerItem(), this);
		Bukkit.getPluginManager().registerEvents(new ListenerPlayerChat(), this);
		Bukkit.getPluginManager().registerEvents(new ListenerPlayerDamage(), this);
		Bukkit.getPluginManager().registerEvents(new ListenerPlayerExperience(), this);
		Bukkit.getPluginManager().registerEvents(new ListenerPlayerQuit(), this);
		Bukkit.getPluginManager().registerEvents(new ListenerInventoryClick(), this);
	}
	
	@Override
	public void onDisable() {
		// 循环玩家
		for (Player player : Bukkit.getOnlinePlayers()) {
			// 检查玩家是否是队长，是则更新最后登陆时间
			TeamData teamData = TeamDataManager.getTeam(player.getName());
			if (teamData != null && teamData.getTeamLeader().equals(player.getName())){
				teamData.updateTeamTime();
			}
			// 检查玩家是否打开了掉落物背包
			if (player.getOpenInventory().getTopInventory().getTitle().equals(getLanguage().get(Message.INVENTORY_DROP_NAME).asString())) {
				player.closeInventory();
			}
		}
		// 保存数据
		TeamDataManager.saveTeamList();
		// 停止任务
		Bukkit.getScheduler().cancelTasks(this);
	}
}
