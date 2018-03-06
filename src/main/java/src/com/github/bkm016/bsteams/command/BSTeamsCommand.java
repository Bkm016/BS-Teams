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

/**
 * @author sky
 * @since 2018-03-06 20:33:23
 */
public class BSTeamsCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String label, String[] args) {
        if(label.equalsIgnoreCase("bst") || label.equalsIgnoreCase("teams") || label.equalsIgnoreCase("bsteams")){
                //判断是否是玩家
                if((sender instanceof Player)){
                    //判断是否有权限
                    if(!sender.hasPermission("bsteams"+ ".use")){
        				BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_PER_CMD).send(sender);
                        return true;
                    }
                }
                //无参数
                if(args.length==0){
                	sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6==========[&b "+ "bsteams" +"&6 ]=========="));
                        for(java.lang.reflect.Method method : this.getClass().getDeclaredMethods()){
                                if(!method.isAnnotationPresent(PlayerCommand.class)){
                                        continue;
                                }
                                PlayerCommand sub=method.getAnnotation(PlayerCommand.class);
                                if(sender.hasPermission("bsteams"+"." + sub.cmd())){
                                	sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/"+ label + " " +sub.cmd()+"&6"+sub.arg()+"&7-:&3 "+BSTeamsPlugin.getLanguage().get("Command."+sub.cmd())));
                                }
                        }
                        return true;
                }
        		if(sender instanceof Player){
        			if(!sender.hasPermission("bsteams." + args[0])) {
        				BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_PER_CMD).send(sender);
        	            return true;
        			}
        		}
                for(java.lang.reflect.Method method:this.getClass().getDeclaredMethods()){
                        if(!method.isAnnotationPresent(PlayerCommand.class)){
                                continue;
                        }
                        PlayerCommand sub=method.getAnnotation(PlayerCommand.class);
                        if(!sub.cmd().equalsIgnoreCase(args[0])){
                                continue;
                        }
                        try {
                                method.invoke(this, sender,args);
                        } catch (IllegalAccessException e) {
                                e.printStackTrace();
                        } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                        } catch (InvocationTargetException e) {
                                e.printStackTrace();
                        }
                        return true;
                }
                BSTeamsPlugin.getLanguage().get(Message.ADMIN_NO_CMD).addPlaceholder("$SubCmd", args[0]).send(sender);
            return true;
        }
        return false;
	}
	@PlayerCommand(cmd="reload")
	public void onReloadCommand(CommandSender sender,String args[]){
        Config.loadConfig();
        Data.loadData();
        BSTeamsPlugin.getLanguage().reload();
		BSTeamsPlugin.getLanguage().get(Message.PLUGIN_RELOAD).send(sender);
	}

}
