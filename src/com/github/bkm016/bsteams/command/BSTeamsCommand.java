package com.github.bkm016.bsteams.command;


import java.lang.reflect.Method;
import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.book.BookHandler;
import com.github.bkm016.bsteams.command.enums.CommandType;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.util.Message;
import com.github.bkm016.bsteams.util.PlayerCommand;

/**
 * @author sky
 * @since 2018-03-06 20:33:23
 */
public class BSTeamsCommand implements CommandExecutor {

	private BSTeamsPlugin plugin;

	public BSTeamsCommand(BSTeamsPlugin plugin){
		this.plugin = plugin;
	}
	
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
			TeamData teamData = plugin.getTeamDataManager().getTeam(sender.getName());
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
    	
    	if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
    		if (sender instanceof ConsoleCommandSender || (args.length > 0 && args[0].equalsIgnoreCase("help"))) {
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
        	}
        	else {
        		// 获取队伍信息
        		TeamData data = plugin.getTeamDataManager().getTeam(sender.getName());
        		if (data == null) {
        			BSTeamsPlugin.getLanguage().get("TEMA-HELP").send((Player) sender);
        		} else {
        			BookHandler.getInst().openInfo((Player) sender, data);
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
			                	method.invoke(BSTeamsSubCommand.class.newInstance(), plugin, sender, args);
			                } catch (Exception e) {
			                	//
			                	e.printStackTrace();
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
	private static boolean contains(CommandType[] type1, CommandType type2) {
		return Arrays.stream(type1).anyMatch(aType1 -> aType1.equals(CommandType.ALL) || aType1.equals(type2));
	}
}
