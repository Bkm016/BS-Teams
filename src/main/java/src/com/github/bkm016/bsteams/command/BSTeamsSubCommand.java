package com.github.bkm016.bsteams.command;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.book.BookHandler;
import com.github.bkm016.bsteams.command.enums.CommandType;
import com.github.bkm016.bsteams.database.TeamDataManager;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.inventory.DropInventory;
import com.github.bkm016.bsteams.util.Config;
import com.github.bkm016.bsteams.util.Message;
import com.github.bkm016.bsteams.util.PlayerCommand;

/**
 * @author sky
 * @since 2018-03-07 23:20:22
 */
public class BSTeamsSubCommand {
	
	/**
	 * 创建队伍
	 * 
	 * @param sender
	 * @param args
	 */
	@PlayerCommand(cmd = "create", type = CommandType.PLAYER)
	void onCreateTeamCommand(CommandSender sender,String args[]) {
		// 创建队伍
		TeamDataManager.createTeam((Player) sender);
		// 提示信息
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_CREATE_TEAM).send(sender);
	}

	/**
	 * 解散队伍
	 * 
	 * @param sender
	 * @param args
	 */
	@PlayerCommand(cmd = "dissolve", type = CommandType.TEAM_LEADER)
	void onRemoveTeamCommand(CommandSender sender, String args[]){
		Player player = (Player) sender;
		TeamData teamData = TeamDataManager.getTeam(player.getName());
		// 当背包内有物品时无法解散
		if (teamData.getTeamItems().size() > 0) {
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_HAS_TEAM_ITEMS).send(player);
			return;
		}
		// 删除队伍
		teamData.remove();
		// 提示信息
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_REMOVE_TEAM).send(player);
	}
	
	/**
	 * 加入队伍
	 * 
	 * @param sender
	 * @param args
	 */
	@PlayerCommand(cmd = "join", arg="<Player>", type = CommandType.PLAYER)
	void onJoinTeamCommand(CommandSender sender, String args[]) {
		// 不符合规范 可以添加扩展功能 - 显示目前所有在线队伍的json
		if (args.length < 2) {
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_FORMAT).send(sender);
			return;
		}
		Player player = (Player) sender;
		TeamData teamData = TeamDataManager.getTeam(args[1]);
		// 判断队伍名是否为空或者是不是队长
		if (teamData == null || !!teamData.getTeamLeader().equals(args[1])){
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_TEAM).send(player);
			return;
		}
		// 加入队伍
		teamData.addTeamMember(args[1]);
		// 提示信息
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_QUIT_TEAM)
			.addPlaceholder("$Team", args[1])
			.send(sender);
	}


	/**
	 * 退出队伍
	 * 
	 * @param sender
	 * @param args
	 */
	@PlayerCommand(cmd = "quit", type = CommandType.TEAM_MEMBER)
	void onQuitTeamCommand(CommandSender sender,String args[]){
		Player player = (Player) sender;
		TeamData teamData = TeamDataManager.getTeam(player.getName());
		// 退出队伍
		teamData.removeTeamMember(player.getName());
		// 提示
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_QUIT_TEAM)
			.addPlaceholder("$Team", teamData.getTeamLeader())
			.send(sender);
	}


	/**
	 * 队伍列表
	 * 
	 * @param sender
	 * @param args
	 */
	@PlayerCommand(cmd = "list")
	void onListTeamCommand(CommandSender sender,String args[]){
		int i=0;
		for (TeamData teamData: TeamDataManager.getTeamList()){
			i++;
			sender.sendMessage(i+"."+teamData.getTeamLeader() + "  人数: "+(teamData.getTeamMembers().size()+1)+"人");
		}
	}
	
	/**
	 * 队伍背包
	 * 
	 * @param sender
	 * @param args
	 */
	@PlayerCommand(cmd = "open", type = {CommandType.TEAM_LEADER, CommandType.TEAM_MEMBER})
	void onOpenCommand(CommandSender sender,String args[]){
		TeamData teamData = TeamDataManager.getTeam(sender.getName());
		if (!teamData.getTeamOption("SHARE-DROPS", true)) {
			return;
		}
		// 打开背包
		DropInventory.openInventory((Player) sender, 1, teamData);
		// 音效
		((Player) sender).playSound(((Player) sender).getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
	}
	
	/**
	 * 清除日志
	 * 
	 * @param sender
	 * @param args
	 */
	@PlayerCommand(cmd = "clearnote", hide = true, type = {CommandType.TEAM_LEADER, CommandType.TEAM_MEMBER})
	void clearNoteCommand(CommandSender sender, String args[]) {
		// 清除日志
		TeamDataManager.getTeam(sender.getName()).getItemNotes().clear();
		// 提示信息
		BSTeamsPlugin.getLanguage().get("Command.clearnote").send(sender);
	}
	
	/**
	 * 重载插件
	 * 
	 * @param sender
	 * @param args
	 */
	@PlayerCommand(cmd = "reload", permission = "bsteams.reload")
	void onReloadCommand(CommandSender sender,String args[]){
        Config.loadConfig();
        BSTeamsPlugin.getLanguage().reload();
		BSTeamsPlugin.getLanguage().get(Message.PLUGIN_RELOAD).send(sender);
	}
	
	/**
	 * 队伍信息
	 * 
	 * @param sender
	 * @param args
	 */
	@PlayerCommand(cmd = "info", type = {CommandType.TEAM_LEADER, CommandType.TEAM_MEMBER})
	void onInfoCommand(CommandSender sender, String args[]) {
		Player player = (Player) sender;
		TeamData teamData = null;
		if (args.length == 1) {
			teamData = TeamDataManager.getTeam(player.getName());
		}
		else {
			// 判断权限
			if (sender.hasPermission("bsteams.admin")) {
				teamData = TeamDataManager.getTeam(args[1]);
				// 判断队伍名是否为空或者是不是队长
				if (teamData == null) {
					BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_TEAM).send(player);
					return;
				}
			}
			return;
		}
		// 打开界面
		BookHandler.getInst().openInfo(player, teamData);
	}
	
	/**
	 * 队伍设置
	 * 
	 * @param sender
	 * @param args
	 */
	@PlayerCommand(cmd = "option", hide = true, type = CommandType.TEAM_LEADER)
	void onOptionsCommand(CommandSender sender, String[] args) {
		if (args.length == 3) {
			Player player = (Player) sender;
			TeamData teamData = TeamDataManager.getTeam(player.getName());
			try {
				teamData.setTeamOption(args[1], Boolean.valueOf(args[2]));
				// 提示
				BSTeamsPlugin.getLanguage().get("TEAM-OPTIONS-MESSAGE-SUCCESS").send(player);
				// 音效
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
			}
			catch (Exception e) {
				// 提示
				BSTeamsPlugin.getLanguage().get("TEAM-OPTIONS-MESSAGE-FALL").send(player);
				// 音效
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
			}
		}
	}
}
