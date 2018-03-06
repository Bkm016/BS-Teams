package com.github.bkm016.bsteams.command;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import com.github.bkm016.bsteams.util.Message;
import com.github.bkm016.bsteams.util.PlayerCommand;

import me.skymc.taboolib.other.DateUtils;

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
        	BSTeamsPlugin.getLanguage().get("Command.title").send(sender);
        	for (java.lang.reflect.Method method : this.getClass().getDeclaredMethods()) {
        		if (method.isAnnotationPresent(PlayerCommand.class)){
        			PlayerCommand sub = method.getAnnotation(PlayerCommand.class);
        			if (Data.isTeam(sender.getName())){
        				if ("join|create".contains(sub.cmd())){
                            continue;
        				}
        			}
        			else {
        				if ("quit|remove".contains(sub.cmd())){
                            continue;
        				}
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
	public void onCreateTeamCommand(CommandSender sender,String args[]){
		if (!(sender instanceof Player)){
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_CONSOLE).send(sender);
			return;
		}
		Player player = (Player) sender;
		if (Data.isTeam(player.getName())){
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_HAS_TEAM).send(sender);
			return;
		}
		Data.createTeam(player);
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_CREATE_TEAM).send(sender);
	}

	@PlayerCommand(cmd = "remove")//解散队伍
	public void onRemoveTeamCommand(CommandSender sender,String args[]){
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
		if (!teamData.getTeamLeader().equals(player.getName())){//玩家不是队长
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_TEAM_LEADER).send(sender);
			return;
		}
		//TODO 防物品损失 
		//1.当背包内有物品时无法解散(不存在的)
		//2.先解散 然后创建一个<里面有东西就关不掉>的Inventory
//		List<ItemStack> teamItems = teamData.getTeamItems();
		teamData.remove();
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_REMOVE_TEAM).send(sender);
	}
	
	@PlayerCommand(cmd = "join",arg="<TeamName>")//加入队伍
	public void onJoinTeamCommand(CommandSender sender,String args[]){
		if (!(sender instanceof Player)){
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_CONSOLE).send(sender);
			return;
		}
		if (args.length<2){
			BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_FORMAT).send(sender);
			return;
		}
		String teamLeader = args[1];
		Player player = (Player) sender;
		if (Data.isTeam(player.getName())){//玩家有队伍
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_HAS_TEAM).send(sender);
			return;
		}
		TeamData teamData = Data.getTeam(teamLeader);
		//判断队伍名是否为空或者是不是队长
		if(teamData == null || !!teamData.getTeamLeader().equals(teamLeader)){
			BSTeamsPlugin.getLanguage().get(Message.PLAYER_NO_TEAM).send(player);
			return;
		}
		teamData.addTeamMember(teamLeader).save();
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_QUIT_TEAM).send(sender);
	}


	@PlayerCommand(cmd = "quit")//退出队伍
	public void onQuitTeamCommand(CommandSender sender,String args[]){
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
		teamData.removeTeamMember(player.getName()).save();
		BSTeamsPlugin.getLanguage().get(Message.PLAYER_QUIT_TEAM).send(sender);
	}


	@PlayerCommand(cmd = "list")//队伍列表
	public void onListTeamCommand(CommandSender sender,String args[]){
		int i=0;
		for(TeamData teamData: Data.getTeamList()){
			i++;
			sender.sendMessage(i+"."+teamData.getTeamLeader() + "  人数: "+(teamData.getTeamMembers().size()+1)+"人");
		}
	}
	

	
	@PlayerCommand(cmd = "open")//打开简单的队伍菜单，测试用 没有事件监听
	public void onOpenCommand(CommandSender sender,String args[]){
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
		Inventory inv = Bukkit.createInventory(null, 54, "233");
		for (ItemStack item : teamData.getTeamItems()){
			inv.addItem(item);
		}
		player.openInventory(inv);
	}
	
	
        
	@PlayerCommand(cmd = "reload")//重载插件
	public void onReloadCommand(CommandSender sender,String args[]){
        Config.loadConfig();
        Data.loadData();
        BSTeamsPlugin.getLanguage().reload();
		BSTeamsPlugin.getLanguage().get(Message.PLUGIN_RELOAD).send(sender);
	}
}
