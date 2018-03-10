package com.github.bkm016.bsteams.command;

import java.util.List;

import org.bukkit.Bukkit;
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

import me.skymc.taboolib.other.DateUtils;

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
	@PlayerCommand(cmd = "join", arg="<leader>", type = CommandType.PLAYER)
	void onJoinTeamCommand(CommandSender sender, String args[]) {
		// 不符合规范 可以添加扩展功能 - 显示目前所有在线队伍的 json
		if (args.length < 2) {
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_FORMAT).send(sender);
			return;
		}
		Player player = (Player) sender;
		Player leaderPlayer = Bukkit.getPlayerExact(args[1]);
		if (leaderPlayer == null){
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_LEADER_NO_ONLINE).send(sender);
			return;
		}
		TeamData teamData = TeamDataManager.getTeam(args[1]);
		// 判断队伍名是否为空或者是不是队长
		if (teamData == null || !teamData.getTeamLeader().equals(leaderPlayer.getName())){
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_TEAM).send(player);
			return;
		}
		// 判断是否满人
		if (teamData.getTeamMembers().size() >= Integer.valueOf(Config.getConfig(Config.TEAM_SIZE))){
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_TEAM_MEMBER_SIZE_MAX).send(sender);
			return;
		}
		// 加入数据储存
		List<String> joinList = TeamDataManager.getjoinList(args[1]);
		if (joinList.size()>0){
			//清除上次的申请记录
			for(int i = joinList.size() - 1 ; i >= 0 ; i--) {
				String playerAndTime = joinList.get(i);
				if (playerAndTime.contains(sender.getName())){
					joinList.remove(i);
				}
			}
		}
		// 申请数据
		joinList.add(sender.getName() 
				+ ":" 
				+ (System.currentTimeMillis() + DateUtils.formatDate(Config.getConfig().getString(Config.VALIDITY_APPLY))));
		// 信息发送
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_JOIN_TO_PLAYER).send(sender);
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_JOIN_TO_lEADER)
			.addPlaceholder("$Player", player.getName())
			.send(leaderPlayer);
	}
	
	/**
	 * 接受玩家邀请
	 * 
	 * @param sender
	 * @param args
	 */
	@PlayerCommand(cmd = "acceptJoin", arg="<player>", hide = true ,type = CommandType.TEAM_LEADER)
	void onAccpetJoinCommand(CommandSender sender, String args[]) {
		List<String> joinList = TeamDataManager.getjoinList(sender.getName());
		if (args.length < 2) {
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_FORMAT).send(sender);
			if (joinList.size() > 0){
				String place = "";
				for (String playerAndTime : joinList){
					place += playerAndTime.split(":")[0] + "  ";
				}
				// 可接受队伍列表消息
				BSTeamsPlugin.getLanguage().get(Message.PLAYER_JOIN_LIST_TO_LEADER)
					.addPlaceholder("$JoinList", place)
					.send(sender);
			}
			return;
		}
		for (String playerAndTime : joinList) {
			// 检测邀请列表是否有这个玩家 并且检测邀请是否过期
			if (playerAndTime.split(":")[0].equals(args[1]) && System.currentTimeMillis() < Long.valueOf(playerAndTime.split(":")[1])){
				TeamData teamData = TeamDataManager.getTeam(sender.getName());
				// 判断是否被解散
				if (teamData == null){
					BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_TEAM).send(sender);
					return;
				}
				// 判断是否满人
				if (teamData.getTeamMembers().size() >= Integer.valueOf(Config.getConfig(Config.TEAM_SIZE))){
					BSTeamsPlugin.getLanguage().get(Message.PLAYER_TEAM_MEMBER_SIZE_MAX).send(sender);
					return;
				}
				// 增加队员
				teamData.addTeamMember(args[1]);
				// 提示队长
				BSTeamsPlugin.getLanguage().get(Message.PLAYER_ACCPET_TO_lEADER)
					.addPlaceholder("$Player", args[1]).send(sender);
				// 提示目标
				if (Bukkit.getPlayerExact(args[1]) != null){
					BSTeamsPlugin.getLanguage().get(Message.PLAYER_ACCPET_TO_PLAYER)
						.addPlaceholder("$Player", sender.getName()).send(Bukkit.getPlayerExact(args[1]));
				}
				return;
			}
		}
		// 邀请不存在提示 sender
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_ACCPET_TO_PLAYER).send(sender);
	}

	/**
	 * 踢出队伍
	 * 
	 * @param sender
	 * @param args
	 */
	@PlayerCommand(cmd = "kick", type = CommandType.TEAM_LEADER)
	void onKickMemberCommand(CommandSender sender,String args[]){
		if (args.length < 2) {
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_FORMAT).send(sender);
			return;
		}
		TeamData teamData = TeamDataManager.getTeam(sender.getName());
		// 玩家不在队伍里
		if (!teamData.getTeamMembers().contains(args[1])){
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_FORMAT)
				.addPlaceholder("Player.KickNotfound", args[1])
				.send(sender);
		}
		else {
			// 踢出玩家
			teamData.removeTeamMember(args[1]);
			Player kickPlayer = Bukkit.getPlayerExact(args[1]);
			if (kickPlayer != null){
				BSTeamsPlugin.getLanguage().get("Player.KickMemberPlayer").send(kickPlayer);
			}
			// 提示队员
			for (Player member : teamData.getTeamMembersOnline(true)) {
				if (!member.equals(kickPlayer)) {
					// 提示玩家
					BSTeamsPlugin.getLanguage().get("Player.KickMemberNotify")
						.addPlaceholder("$Player", kickPlayer.getName())
						.send(member);
				}
			}
		}
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
		// 提示玩家
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_QUIT_TEAM)
			.addPlaceholder("$Team", teamData.getTeamLeader())
			.send(sender);
		
		// 提示队员 + 队长
		for (String name : teamData.getTeamMembersAll()) {
			Player member = Bukkit.getPlayerExact(name);
			if (member != null && !member.equals(player)) {
				// 提示玩家
				BSTeamsPlugin.getLanguage().get(Message.PLAYER_QUIT_TEAM_MEMBER)
					.addPlaceholder("$Player", player.getName())
					.send(member);
			}
		}
	}
	
	/**
	 * 邀请队员
	 * 
	 * @param sender
	 * @param args
	 */
	@PlayerCommand(cmd = "invite", arg="<player>", type = CommandType.TEAM_LEADER)
	void onInviteCommand(CommandSender sender, String args[]) {
		if (args.length < 2) {
			// 当没有 <player> 时，可以展示在线玩家列表
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_FORMAT).send(sender);
			return;
		}
		Player invitePlayer = Bukkit.getPlayerExact(args[1]);
		if (invitePlayer == null){
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_ONLINE).send(sender);
			return;
		}
		// 识别队伍是否满人
		if (TeamDataManager.getTeam(sender.getName()).getTeamMembers().size() >= Integer.valueOf(Config.getConfig(Config.TEAM_SIZE))){
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_TEAM_MEMBER_SIZE_MAX).send(sender);
			return;
		}
		// 识别玩家是否已经在队伍里
		if (TeamDataManager.getTeam(invitePlayer.getName()) != null){
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_PLAYER_HAS_TEAM)
				.addPlaceholder("$Player", invitePlayer.getName())
				.send(sender);
			return;
		}
		// 邀请数据储存 被邀请玩家 - 队长名 - 时间
		List<String> inviteList = TeamDataManager.getinviteList(invitePlayer.getName());
		if (inviteList.size()>0){
			//清除上次的申请记录
			for(int i=inviteList.size()-1;i>=0;i--){
				String leaderAndTime = inviteList.get(i);
				if (leaderAndTime.contains(sender.getName())){
					inviteList.remove(i);
				}
			}
		}
		// 申请数据
		inviteList.add(sender.getName() 
				+ ":" 
				+ (System.currentTimeMillis() + DateUtils.formatDate(Config.getConfig().getString(Config.VALIDITY_INVITE))));
		// 信息发送
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_INVITE_TO_lEADER).send(sender);
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_INVITE_TO_PLAYER)
			.addPlaceholder("$Player", sender.getName())
			.send(invitePlayer);
	}
	
	/**
	 * 接受队伍邀请
	 * 
	 * @param sender
	 * @param args
	 */
	@PlayerCommand(cmd = "accept", arg="<leader>", hide = true ,type = CommandType.PLAYER)
	void onAccpetCommand(CommandSender sender, String args[]) {
		List<String> inviteList = TeamDataManager.getinviteList(sender.getName());
		if (args.length < 2) {
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_FORMAT).send(sender);
			if (inviteList.size() > 0){
				String place = "";
				for (String leaderAndTime : inviteList){
					place += leaderAndTime.split(":")[0] + "  ";
				}
				// 可接受队伍列表消息
				BSTeamsPlugin.getLanguage().get(Message.PLAYER_INVITE_LIST_TO_PLAYER)
					.addPlaceholder("$InviteList", place)
					.send(sender);
			}
			return;
		}
		for (String leaderAndTime : inviteList){
			// 检测邀请列表是否有这个玩家 并且检测邀请是否过期
			if (leaderAndTime.split(":")[0].equals(args[1]) && System.currentTimeMillis() < Long.valueOf(leaderAndTime.split(":")[1])){
				TeamData teamData = TeamDataManager.getTeam(args[1]);
				// 判断是否被解散
				if (teamData == null){
					BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_TEAM).send(sender);
					return;
				}
				// 识别队伍是否满人
				if (teamData.getTeamMembers().size() >= Integer.valueOf(Config.getConfig(Config.TEAM_SIZE))){
					BSTeamsPlugin.getLanguage().get(Message.PLAYER_TEAM_MEMBER_SIZE_MAX).send(sender);
					return;
				}
				// 接受请求 清除列表
				inviteList.clear();
				// 增加队员
				teamData.addTeamMember(sender.getName());
				// 输出消息
				BSTeamsPlugin.getLanguage().get(Message.PLAYER_ACCPET_TO_PLAYER)
					.addPlaceholder("$Player", args[1]).send(sender);
				if (Bukkit.getPlayerExact(args[1]) != null){
					BSTeamsPlugin.getLanguage().get(Message.PLAYER_ACCPET_TO_lEADER)
						.addPlaceholder("$Player", sender.getName()).send(Bukkit.getPlayerExact(args[1]));
				}
				return;
			}
		}
		// 邀请不存在提示 sender
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_ACCPET_TO_PLAYER).send(sender);
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
		TeamDataManager.registerCooldown();
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
