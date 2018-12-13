package com.github.bkm016.bsteams;

import com.github.bkm016.bsteams.api.BSTeamsPluginAPI;
import com.github.bkm016.bsteams.book.BookHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.bkm016.bsteams.command.BSTeamsCommand;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.database.TeamDataManager;
import com.github.bkm016.bsteams.event.ListenerInventoryClick;
import com.github.bkm016.bsteams.event.ListenerPlayerChat;
import com.github.bkm016.bsteams.event.ListenerPlayerDamage;
import com.github.bkm016.bsteams.event.ListenerPlayerExperience;
import com.github.bkm016.bsteams.event.ListenerPlayerItem;
import com.github.bkm016.bsteams.event.ListenerPlayerQuit;
import com.github.bkm016.bsteams.util.Config;
import com.github.bkm016.bsteams.util.Message;
import com.github.bkm016.bsteams.util.Placeholders;

import lombok.Getter;
import me.skymc.taboolib.string.language2.Language2;

/**
 * @author sky
 * @since 2018-03-06 19:41:07
 */
public class BSTeamsPlugin extends JavaPlugin {

    private static Plugin inst;
    private static Language2 language;
    private static BSTeamsPluginAPI api;
    private TeamDataManager teamDataManager;

    @Override
    public void onLoad() {
        inst = this;
        api = new BSTeamsPluginAPI(this);
        BookHandler.setup(this);
    }

    @Override
    public void onEnable() {
        // 载入 Language2
        language = new Language2(this);

        // 注册命令
        Bukkit.getPluginCommand("bsteams").setExecutor(new BSTeamsCommand(this));
        // 载入配置
        Config.loadConfig();
        teamDataManager = new TeamDataManager(this);
        // 载入PlaceholderAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new Placeholders(this).hook();
            Bukkit.getConsoleSender().sendMessage("[BS-Teams] §7启用 PlacholderAPI");
        } else {
            Bukkit.getConsoleSender().sendMessage("[BS-Teams] §7未找到 PlacholderAPI");
        }

        // 监听器
        Bukkit.getPluginManager().registerEvents(new ListenerPlayerItem(this), this);
        Bukkit.getPluginManager().registerEvents(new ListenerPlayerChat(this), this);
        Bukkit.getPluginManager().registerEvents(new ListenerPlayerDamage(this), this);
        Bukkit.getPluginManager().registerEvents(new ListenerPlayerExperience(this), this);
        Bukkit.getPluginManager().registerEvents(new ListenerPlayerQuit(this), this);
        Bukkit.getPluginManager().registerEvents(new ListenerInventoryClick(), this);
    }

    @Override
    public void onDisable() {
        // 循环玩家
        for (Player player : Bukkit.getOnlinePlayers()) {
            // 检查玩家是否是队长，是则更新最后登陆时间
            TeamData teamData = teamDataManager.getTeam(player.getName());
            if (teamData != null && teamData.getTeamLeader().equals(player.getName())) {
                teamData.updateTeamTime();
            }
            // 检查玩家是否打开了掉落物背包
            if (player.getOpenInventory().getTopInventory().getTitle().equals(getLanguage().get(Message.INVENTORY_DROP_NAME).asString())) {
                player.closeInventory();
            }
        }
        // 保存数据
        teamDataManager.saveTeamList();
        // 停止任务
        Bukkit.getScheduler().cancelTasks(this);
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static Plugin getInst() {
        return inst;
    }

    public static Language2 getLanguage() {
        return language;
    }

    public static BSTeamsPluginAPI getApi() {
        return api;
    }

    public TeamDataManager getTeamDataManager() {
        return teamDataManager;
    }
}
