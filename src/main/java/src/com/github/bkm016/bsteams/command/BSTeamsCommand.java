package com.github.bkm016.bsteams.command;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.bkm016.bsteams.util.Config;
import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.Data;
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
                if(method.isAnnotationPresent(PlayerCommand.class)){
	                PlayerCommand sub = method.getAnnotation(PlayerCommand.class);
	                if(sub.cmd().equalsIgnoreCase(args[0])){
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
        
	@PlayerCommand(cmd = "reload")
	public void onReloadCommand(CommandSender sender,String args[]){
        Config.loadConfig();
        Data.loadData();
        BSTeamsPlugin.getLanguage().reload();
		BSTeamsPlugin.getLanguage().get(Message.PLUGIN_RELOAD).send(sender);
	}
}
