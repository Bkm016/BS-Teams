package com.github.bkm016.bsteams.command;


import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.bkm016.bsteams.util.Config;
import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.Data;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.inventory.DropInventory;
import com.github.bkm016.bsteams.util.Message;
import com.github.bkm016.bsteams.util.PlayerCommand;

/**
 * @author sky
 * @since 2018-03-06 20:33:23
 */
public class BSTeamsCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String label, String[] args) {
	    // 判断是否有权限
        if (!sender.hasPermission("bsteams.use")){
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_PER_CMD).send(sender);
            return false;
        }
		
        // 如果没有参数
        if (args.length == 0) {
			String blackList = "join || create || quit || remove || open"; //控制台不能执行的指令
			if (sender instanceof Player){
				TeamData teamData = Data.getTeam(sender.getName());
				if (teamData != null){//玩家有队伍时不能执行的指令
					if (teamData.getTeamLeader().equals(sender.getName())){
	    				blackList = "join || create || quit";//只允许 remove 解散队伍
					}
					else {
	    				blackList = "join || create || remove";//只允许 quit 退出队伍
					}
    			}
    			else {//当玩家没有队伍时不能执行的指令
    				blackList = "remove || quit || remove || open";
    			}
			}
        	BSTeamsPlugin.getLanguage().get("Command.title").send(sender);
        	for (java.lang.reflect.Method method : this.getClass().getDeclaredMethods()) {
        		if (method.isAnnotationPresent(PlayerCommand.class)){
        			PlayerCommand sub = method.getAnnotation(PlayerCommand.class);
        			if (blackList.contains(sub.cmd())){
        				continue;
        			}
        			if (sender.hasPermission("bsteams." + sub.cmd())) {
        				// 帮助
        				BSTeamsPlugin.getLanguage().get("Command.label")
        					.addPlaceholder("$command", label + " " + sub.cmd() + " " + sub.arg())
        					.addPlaceholder("$desc", BSTeamsPlugin.getLanguage().get("Command." + sub.cmd()).asString())
        					.send(sender);
        			}
                }
        	}
        	return true;
        }
        else if (!sender.hasPermission("bsteams." + args[0])) {
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_PER_CMD).send(sender);
            return true;
		}
        else {
        	for (java.lang.reflect.Method method : this.getClass().getDeclaredMethods()){
                if (method.isAnnotationPresent(PlayerCommand.class)){
	                PlayerCommand sub = method.getAnnotation(PlayerCommand.class);
	                if (sub.cmd().equalsIgnoreCase(args[0])){
		                try {
		                	method.invoke(this, sender,args);
		                } catch (Exception e) {
		                	//
		                }
		                return true;
	                }
                }
        	}
        	// 未知命令
        	BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_CMD).addPlaceholder("$SubCmd", args[0]).send(sender);
        	return false;
        }
	}
	//注意 以下都是测试指令，因考虑到书本的操作，这里只用作统计data是否有效
	@PlayerCommand(cmd = "create")//创建队伍
	void onCreateTeamCommand(CommandSender sender,String args[]){
		if (!(sender instanceof Player)){
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_CONSOLE).send(sender);
			return;
		}
		Player player = (Player) sender;
		if (Data.getTeam(player.getName()) != null){//玩家有队伍
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_HAS_TEAM).send(player);
			return;
		}
		Data.createTeam(player);//创建队伍
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_CREATE_TEAM).send(player);
	}

	@PlayerCommand(cmd = "remove")//解散队伍
	void onRemoveTeamCommand(CommandSender sender,String args[]){
		if (!(sender instanceof Player)){
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_CONSOLE).send(sender);
			return;
		}
		Player player = (Player) sender;
		TeamData teamData = Data.getTeam(player.getName());
		if (teamData == null){//玩家没有队伍
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_HAS_TEAM).send(player);
			return;
		}
		if (!teamData.getTeamLeader().equals(player.getName())){//玩家不是队长
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_TEAM_LEADER).send(player);
			return;
		}
		if (teamData.getTeamItems().size()>0){//当背包内有物品时无法解散
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_HAS_TEAM_ITEMS).send(player);
			return;
		}
		teamData.remove();
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_REMOVE_TEAM).send(player);
	}
	
	@PlayerCommand(cmd = "join",arg="<TeamName>")//加入队伍
	void onJoinTeamCommand(CommandSender sender,String args[]){
		if (!(sender instanceof Player)){
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_CONSOLE).send(sender);
			return;
		}
		if (args.length<2){//不符合规范 可以添加扩展功能 - 显示目前所有在线队伍的json
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_FORMAT).send(sender);
			return;
		}
		String teamLeader = args[1];
		Player player = (Player) sender;
		if (Data.getTeam(player.getName()) != null){//玩家有队伍
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_HAS_TEAM).send(sender);
			return;
		}
		TeamData teamData = Data.getTeam(teamLeader);
		//判断队伍名是否为空或者是不是队长
		if (teamData == null || !!teamData.getTeamLeader().equals(teamLeader)){
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_TEAM).send(player);
			return;
		}
		teamData.addTeamMember(teamLeader);
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_QUIT_TEAM).send(sender);
	}


	@PlayerCommand(cmd = "quit")//退出队伍
	void onQuitTeamCommand(CommandSender sender,String args[]){
		if (!(sender instanceof Player)){
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_CONSOLE).send(sender);
			return;
		}
		Player player = (Player) sender;
		TeamData teamData = Data.getTeam(player.getName());
		if (teamData == null){//玩家没有队伍
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_HAS_TEAM).send(sender);
			return;
		}
		if (!teamData.getTeamMembers().contains(player.getName())){//玩家不是队员
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_TEAM_MEMBER).send(sender);
			return;
		}
		teamData.removeTeamMember(player.getName());
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_QUIT_TEAM).send(sender);
	}


	@PlayerCommand(cmd = "list")//队伍列表
	void onListTeamCommand(CommandSender sender,String args[]){
		int i=0;
		for (TeamData teamData: Data.getTeamList()){
			i++;
			sender.sendMessage(i+"."+teamData.getTeamLeader() + "  人数: "+(teamData.getTeamMembers().size()+1)+"人");
		}
	}
	

	
	@PlayerCommand(cmd = "open")//打开简单的队伍菜单，测试用 没有事件监听
	void onOpenCommand(CommandSender sender,String args[]){
		if (!(sender instanceof Player)){
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_CONSOLE).send(sender);
			return;
		}
		Player player = (Player) sender;
		TeamData teamData = Data.getTeam(player.getName());
		if (teamData == null){
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_HAS_TEAM).send(sender);
			return;
		}
		DropInventory.openDropInventory(player, 1, teamData);
	}
	
	
        
	@PlayerCommand(cmd = "reload")//重载插件
	void onReloadCommand(CommandSender sender,String args[]){
        Config.loadConfig();
        BSTeamsPlugin.getLanguage().reload();
		BSTeamsPlugin.getLanguage().get(Message.PLUGIN_RELOAD).send(sender);
	}
}
