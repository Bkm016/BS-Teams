package com.github.bkm016.bsteams.command;


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.bkm016.bsteams.util.Config;
import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.command.enums.CommandType;
import com.github.bkm016.bsteams.database.TeamDataManager;
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
        
    	// 指令类型
    	CommandType type = CommandType.CONSOLE;
    	if (sender instanceof Player){
			TeamData teamData = TeamDataManager.getTeam(sender.getName());
			if (teamData != null){
				if (teamData.getTeamLeader().equals(sender.getName())){
					type = CommandType.TEAM_LEADER;
				} else {
					type = CommandType.TEAM_MEMBER;
				}
			} else {
				type = CommandType.PLAYER;
			}
		}
    	
        // 如果没有参数
        if (args.length == 0) {
			// 帮助
        	BSTeamsPlugin.getLanguage().get("Command.title").send(sender);
        	for (Method method : BSTeamsSubCommand.class.getDeclaredMethods()) {
        		if (method.isAnnotationPresent(PlayerCommand.class)){
        			PlayerCommand sub = method.getAnnotation(PlayerCommand.class);
        			/*
        			 * 判断各项条件:
        			 * 
        			 * 1. 指令目标
        			 * 2. 指令是否隐藏
        			 * 3. 指令权限
        			 */
        			if (!contains(sub.type(), type) || sub.hide() || !sender.hasPermission(sub.permission())){
        				continue;
        			}
        			// 帮助
    				BSTeamsPlugin.getLanguage().get("Command.label")
    					.addPlaceholder("$command", label + " " + sub.cmd() + " " + sub.arg())
    					.addPlaceholder("$desc", BSTeamsPlugin.getLanguage().get("Command." + sub.cmd()).asString())
    					.send(sender);
                }
        	}
        	return true;
        }
        else {
        	// 获取指令
        	for (Method method : BSTeamsSubCommand.class.getDeclaredMethods()){
                if (method.isAnnotationPresent(PlayerCommand.class)){
	                PlayerCommand sub = method.getAnnotation(PlayerCommand.class);
	                // 指令符合
	                if (sub.cmd().equalsIgnoreCase(args[0])){
	                	// 条件不符
	                	if (!contains(sub.type(), type) || !sender.hasPermission(sub.permission())) {
	                		BSTeamsPlugin.getLanguage().get("Command.blacklist").send(sender);
	                	} else {
			                try {
			                	method.invoke(BSTeamsSubCommand.class.newInstance(), sender, args);
			                } catch (Exception e) {
			                	//
			                }
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
	
	/**
	 * 判断条件
	 * 
	 * @param type1 条件1
	 * @param type2 条件2
	 * @return boolean
	 */
	private boolean contains(CommandType[] type1, CommandType type2) {
		for (int i = 0 ; i < type1.length ; i++) {
			if (type1[i].equals(CommandType.ALL) || type1[i].equals(type2)) {
				return true;
			}
		}
		return false;
	}
}
