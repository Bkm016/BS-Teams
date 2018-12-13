package com.github.bkm016.bsteams.command;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.book.BookHandler;
import com.github.bkm016.bsteams.command.enums.CommandType;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.inventory.DropInventory;
import com.github.bkm016.bsteams.util.Config;
import com.github.bkm016.bsteams.util.Message;
import com.github.bkm016.bsteams.util.PlayerCommand;

import me.skymc.taboolib.message.ChatCatcher;
import me.skymc.taboolib.message.ChatCatcher.Catcher;
import me.skymc.taboolib.other.DateUtils;
import me.skymc.taboolib.other.NumberUtils;

/**
 * @author sky
 * @since 2018-03-07 23:20:22
 */
public class BSTeamsSubCommand {
	
	@PlayerCommand(cmd = "giveexp", permission = "bsteams.admin")
	void onEXPCommand(BSTeamsPlugin plugin,CommandSender sender, String[] args) {
		if (args.length != 3) {
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_FORMAT).send(sender);
			return;
		}
		
		Player player = Bukkit.getPlayerExact(args[1]);
		if (player == null) {
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_ONLINE).send(sender);
			return;
		}

		BSTeamsPlugin.getApi().setExperienceShare(player, false);
		player.giveExp(NumberUtils.getInteger(args[2]));
		BSTeamsPlugin.getApi().setExperienceShare(player, true);
	}
	
	/**
	 * 创建队伍
	 *
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "create", type = CommandType.PLAYER)
	void onCreateTeamCommand(BSTeamsPlugin plugin,CommandSender sender,String args[]) {
		// 提示信息
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_CREATE_TEAM).send(sender);
		// 打开界面
		BookHandler.getInst().openInfo((Player) sender, plugin.getTeamDataManager().createTeam((Player) sender));
	}

	/**
	 * 解散队伍
	 *
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "dissolve", type = CommandType.TEAM_LEADER)
	void onRemoveTeamCommand(BSTeamsPlugin plugin,CommandSender sender, String args[]){
		Player player = (Player) sender;
		TeamData teamData = plugin.getTeamDataManager().getTeam(player.getName());
		// 当背包内有物品时无法解散
		if (teamData.getTeamItems().size() > 0) {
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_HAS_TEAM_ITEMS).send(player);
			return;
		}
		// 删除队伍
		plugin.getTeamDataManager().removeTeam(teamData);
		// 提示信息
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_REMOVE_TEAM).send(player);
	}
	
	/**
	 * 加入队伍
	 *
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args  String[]
	 */
	@PlayerCommand(cmd = "join", arg="<leader>", type = CommandType.PLAYER)
	void onJoinTeamCommand(BSTeamsPlugin plugin,CommandSender sender, String args[]) {
		// 不符合规范 可以添加扩展功能 - 显示目前所有在线队伍的 json
		if (args.length < 2) {
			ChatCatcher.call((Player) sender, new Catcher() {
				
				@Override
				public void cancel() {
					BSTeamsPlugin.getLanguage().get("TEAM-GUIDE-QUIT").send(sender);
				}
				
				@Override
				public Catcher before() {
					BSTeamsPlugin.getLanguage().get("TEAM-GUIDE-JOIN").send(sender);
					return this;
				}
				
				@Override
				public boolean after(String message) {
					Bukkit.getScheduler().runTask(BSTeamsPlugin.getInst(), () -> onJoinTeamCommand(plugin, sender, new String[] { "join", message }));
					return false;
				}
			});
			return;
		}
		Player player = (Player) sender;
		Player leaderPlayer = Bukkit.getPlayerExact(args[1]);
		if (leaderPlayer == null){
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_LEADER_NO_ONLINE).send(sender);
			return;
		}
		TeamData teamData = plugin.getTeamDataManager().getTeam(args[1]);
		// 判断队伍名是否为空或者是不是队长
		if (teamData == null || !teamData.getTeamLeader().equals(leaderPlayer.getName()) || !teamData.getTeamOption("PUBLIC", true)){
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_TEAM).send(player);
			return;
		}
		// 判断队伍是否公开
		if (!teamData.getTeamOption("PUBLIC", true)){
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_PRIVATE_TEAM).send(sender);
			return;
		}
		// 判断冷却
		if (plugin.getTeamDataManager().getCooldownApply().isCooldown(sender.getName(), 0)) {
			BSTeamsPlugin.getLanguage().get("Player.CooldownJoin").send(player);
			return;
		}
		// 加入数据储存
		List<String> joinList = plugin.getTeamDataManager().getJoinList(args[1]);
		if (joinList.size() > 0){
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
	 * 清除玩家申请
	 *
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "clearjoin", hide = true, type = CommandType.TEAM_LEADER)
	void onCleraJoinCommand(BSTeamsPlugin plugin,CommandSender sender, String[] args) {
		plugin.getTeamDataManager().getJoinMap().remove(sender.getName());
		BSTeamsPlugin.getLanguage().get("Player.ClearJoin").send(sender);
	}
	
	/**
	 * 清除队伍邀请
	 *
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "clearinvite", hide = true, type = CommandType.PLAYER)
	void onCleraInviteCommand(BSTeamsPlugin plugin,CommandSender sender, String[] args) {
		plugin.getTeamDataManager().getInviteMap().remove(sender.getName());
		BSTeamsPlugin.getLanguage().get("Player.ClearInvite").send(sender);
	}
	
	/**
	 * 接受玩家申请
	 *
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "acceptJoin", arg="<player>", hide = true ,type = CommandType.TEAM_LEADER)
	void onAccpetJoinCommand(BSTeamsPlugin plugin,CommandSender sender, String args[]) {
		List<String> joinList = plugin.getTeamDataManager().getJoinList(sender.getName());
		if (args.length < 2) {
//			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_FORMAT).send(sender);
//			if (joinList.size() > 0){
//				String place = "";
//				for (String playerAndTime : joinList){
//					place += playerAndTime.split(":")[0] + "  ";
//				}
//				// 可接受队伍列表消息
//				BSTeamsPlugin.getLanguage().get(Message.PLAYER_JOIN_LIST_TO_LEADER)
//					.addPlaceholder("$JoinList", place)
//					.send(sender);
//			}
			// 打开界面
			BookHandler.getInst().openApply((Player) sender);
			return;
		}
		for (String playerAndTime : joinList) {
			// 检测邀请列表是否有这个玩家 并且检测邀请是否过期
			if (playerAndTime.split(":")[0].equals(args[1]) && System.currentTimeMillis() < Long.valueOf(playerAndTime.split(":")[1])){
				// 接受请求 清除列表
				joinList.clear();
				plugin.getTeamDataManager().getTeam(sender.getName()).addTeamMember(args[1]);
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
	 * 退出队伍
	 *
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "kick", type = CommandType.TEAM_LEADER)
	void onKickMemberCommand(BSTeamsPlugin plugin,CommandSender sender,String args[]){
		if (args.length < 2) {
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_FORMAT).send(sender);
			return;
		}
		TeamData teamData = plugin.getTeamDataManager().getTeam(sender.getName());
		// 玩家不在队伍里
		if (!teamData.getTeamMembers().contains(args[1])){
			BSTeamsPlugin.getLanguage().get("Player.KickNotfound")
				.addPlaceholder("$name", args[1])
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
						.addPlaceholder("$name", kickPlayer.getName())
						.send(member);
				}
			}
		}
	}

	/**
	 * 退出队伍
	 *
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "quit", type = CommandType.TEAM_MEMBER)
	void onQuitTeamCommand(BSTeamsPlugin plugin,CommandSender sender,String args[]){
		Player player = (Player) sender;
		TeamData teamData = plugin.getTeamDataManager().getTeam(player.getName());
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
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "invite", arg="<player>", type = CommandType.TEAM_LEADER)
	void onInviteCommand(BSTeamsPlugin plugin,CommandSender sender, String args[]) {
		if (args.length < 2) {
			ChatCatcher.call((Player) sender, new Catcher() {
				
				@Override
				public void cancel() {
					BSTeamsPlugin.getLanguage().get("TEAM-GUIDE-QUIT").send(sender);
				}
				
				@Override
				public Catcher before() {
					BSTeamsPlugin.getLanguage().get("TEAM-GUIDE-INVITE").send(sender);
					return this;
				}
				
				@Override
				public boolean after(String message) {
					Bukkit.getScheduler().runTask(BSTeamsPlugin.getInst(), () -> onInviteCommand(plugin, sender, new String[] { "invite", message }));
					return false;
				}
			});
			return;
		}
		Player invitePlayer = Bukkit.getPlayerExact(args[1]);
		if (invitePlayer == null){
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_ONLINE).send(sender);
			return;
		}
		// 识别玩家是否已经在队伍里
		if (plugin.getTeamDataManager().getTeam(invitePlayer.getName()) != null){
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_PLAYER_HAS_TEAM)
				.addPlaceholder("$Player", invitePlayer.getName())
				.send(sender);
			return;
		}
		// 判断冷却
		if (plugin.getTeamDataManager().getCooldownInvite().isCooldown(sender.getName(), 0)) {
			BSTeamsPlugin.getLanguage().get("Player.CooldownInvite").send(sender);
			return;
		}
		// 邀请数据储存 被邀请玩家 - 队长名 - 时间
		List<String> inviteList = plugin.getTeamDataManager().getInviteList(invitePlayer.getName());
		if (inviteList.size()>0){
			//清除上次的申请记录
			for(int i = inviteList.size() - 1 ; i >= 0 ; i--){
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
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "accept", arg="<leader>", hide = true ,type = CommandType.PLAYER)
	void onAccpetCommand(BSTeamsPlugin plugin,CommandSender sender, String args[]) {
		List<String> inviteList = plugin.getTeamDataManager().getInviteList(sender.getName());
		if (args.length < 2) {
//			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_FORMAT).send(sender);
//			if (inviteList.size() > 0){
//				String place = "";
//				for (String leaderAndTime : inviteList){
//					place += leaderAndTime.split(":")[0] + "  ";
//				}
//				// 可接受队伍列表消息
//				BSTeamsPlugin.getLanguage().get(Message.PLAYER_INVITE_LIST_TO_PLAYER)
//					.addPlaceholder("$InviteList", place)
//					.send(sender);
//			}
			// 打开界面
			BookHandler.getInst().openInvite((Player) sender);
			return;
		}
		for (String leaderAndTime : inviteList){
			// 检测邀请列表是否有这个玩家 并且检测邀请是否过期
			if (leaderAndTime.split(":")[0].equals(args[1]) && System.currentTimeMillis() < Long.valueOf(leaderAndTime.split(":")[1])){
				// 接受请求 清除列表
				inviteList.clear();
				plugin.getTeamDataManager().getTeam(args[1]).addTeamMember(sender.getName());
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
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "list")
	void onListTeamCommand(BSTeamsPlugin plugin,CommandSender sender,String args[]) {
		if (sender instanceof Player) {
			BookHandler.getInst().openList((Player) sender);
		}
		else {
			int i=0;
			for (TeamData teamData: plugin.getTeamDataManager().getTeamList()){
				i++;
				sender.sendMessage(i+"."+teamData.getTeamLeader() + "  人数: "+(teamData.getTeamMembers().size()+1)+"人");
			}
		}
	}
	
	/**
	 * 队伍背包
	 *
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "open", type = {CommandType.TEAM_LEADER, CommandType.TEAM_MEMBER})
	void onOpenCommand(BSTeamsPlugin plugin,CommandSender sender, String args[]){
		TeamData teamData = plugin.getTeamDataManager().getTeam(args.length > 1 && sender.isOp() ? args[1] : sender.getName());
		if (teamData == null || !teamData.getTeamOption("SHARE-DROPS", true)) {
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
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "clearnote", hide = true, type = {CommandType.TEAM_LEADER, CommandType.TEAM_MEMBER})
	void clearNoteCommand(BSTeamsPlugin plugin,CommandSender sender, String args[]) {
		// 清除日志
		TeamData teamData = plugin.getTeamDataManager().getTeam(args.length > 1 && sender.isOp() ? args[1] : sender.getName());
		if (teamData == null) {
			return;
		}
		teamData.getItemNotes().clear();
		// 提示信息
		BSTeamsPlugin.getLanguage().get("Command.clearnote").send(sender);
	}
	
	/**
	 * 重载插件
	 *
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "reload", permission = "bsteams.reload")
	void onReloadCommand(BSTeamsPlugin plugin,CommandSender sender,String args[]){
        Config.loadConfig();
		plugin.getTeamDataManager().registerCooldown();
        BSTeamsPlugin.getLanguage().reload();
		BSTeamsPlugin.getLanguage().get(Message.PLUGIN_RELOAD).send(sender);
	}
	
	/**
	 * 队伍信息
	 *
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "info", type = {CommandType.TEAM_LEADER, CommandType.TEAM_MEMBER})
	void onInfoCommand(BSTeamsPlugin plugin,CommandSender sender, String args[]) {
		Player player = (Player) sender;
		TeamData teamData = null;
		if (args.length == 1) {
			teamData = plugin.getTeamDataManager().getTeam(player.getName());
		}
		else {
			// 判断权限
			if (sender.hasPermission("bsteams.admin")) {
				teamData = plugin.getTeamDataManager().getTeam(args[1]);
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
	 * @param plugin BSTeamsPlugin
	 * @param sender CommandSender
	 * @param args String[]
	 */
	@PlayerCommand(cmd = "option", hide = true, type = CommandType.TEAM_LEADER)
	void onOptionsCommand(BSTeamsPlugin plugin,CommandSender sender, String[] args) {
		if (args.length == 3) {
			Player player = (Player) sender;
			TeamData teamData = plugin.getTeamDataManager().getTeam(player.getName());
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
			// 重新打开界面
			BookHandler.getInst().openInfo(player, teamData);
		}
	}
}
