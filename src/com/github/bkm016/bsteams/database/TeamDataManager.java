package com.github.bkm016.bsteams.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.util.Config;

import lombok.Getter;
import me.skymc.taboolib.cooldown.seconds.CooldownPack2;
import me.skymc.taboolib.cooldown.seconds.CooldownUtils2;
import me.skymc.taboolib.other.DateUtils;
import me.skymc.taboolib.playerdata.DataUtils;

public class TeamDataManager {

    private final File file = new File(BSTeamsPlugin.getInst().getDataFolder(), "Data.dat");

    private YamlConfiguration data = new YamlConfiguration();
    private ArrayList<TeamData> teamList = new ArrayList<>();
    private ConcurrentHashMap<String, List<String>> inviteMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<String>> joinMap = new ConcurrentHashMap<>();
    private CooldownPack2 cooldownInvite;
    private CooldownPack2 cooldownApply;

    private final BSTeamsPlugin plugin;

    public TeamDataManager(BSTeamsPlugin plugin) {
        this.plugin = plugin;
        loadData();
        registerCooldown();
    }

    /**
     * 注册冷却
     */
    public void registerCooldown() {
        // 注销冷却
        CooldownUtils2.unregister("bsteams|cooldown-invite");
        CooldownUtils2.unregister("bsteams|cooldown-apply");
        // 注册冷却
        CooldownUtils2.register(cooldownInvite = new CooldownPack2("bsteams|cooldown-invite", (int) DateUtils.formatDate(Config.getConfig(Config.COOLDOWN_INVITE))), BSTeamsPlugin.getInst());
        CooldownUtils2.register(cooldownApply = new CooldownPack2("bsteams|cooldown-apply", (int) DateUtils.formatDate(Config.getConfig(Config.COOLDOWN_APPLY))), BSTeamsPlugin.getInst());
    }

    //获取加入列表
    public List<String> getJoinList(String playerName) {
        List<String> joinList = new CopyOnWriteArrayList<>();
        if (joinMap.containsKey(playerName)) {
            return joinMap.get(playerName);
        } else {
            joinMap.put(playerName, joinList);
        }
        return joinList;
    }

    //获取邀请列表
    public List<String> getInviteList(String playerName) {
        List<String> inviteList = new CopyOnWriteArrayList<String>();
        if (inviteMap.containsKey(playerName)) {
            return inviteMap.get(playerName);
        } else {
            inviteMap.put(playerName, inviteList);
        }
        return inviteList;
    }

    // 定时清理joinMap
    void ClearOverdueJoin() {
        for (String key : joinMap.keySet()) {
            List<String> joinList = joinMap.get(key);
            for (String name : joinList) {
                if (System.currentTimeMillis() > Long.valueOf(name.split(":")[1])) {
                    joinList.remove(name);
                }
            }
            // 删除数据
            if (joinList.size() == 0) {
                joinMap.remove(key);
            }
        }
    }

    // 定时清理inviteMap
    void ClearOverdueInvite() {
        for (String key : inviteMap.keySet()) {
            List<String> inviteList = inviteMap.get(key);
            for (String name : inviteList) {
                if (System.currentTimeMillis() > Long.valueOf(name.split(":")[1])) {
                    inviteList.remove(name);
                }
            }
            // 删除数据
            if (inviteList.size() == 0) {
                inviteList.remove(key);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        // 清空数据
        teamList.clear();

        // 检测数据库是否存在
        if (!file.exists()) {
            Bukkit.getConsoleSender().sendMessage("[BS-Teams] §c数据不存在，创建数据文件");
        } else {
            Bukkit.getConsoleSender().sendMessage("[BS-Teams] §7正在载入队伍数据...");
            try {
                data.load(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int removeTeamSize = 0;
        for (String teamLeader : data.getKeys(false)) {
            Long teamTimes = data.getLong(teamLeader + ".Time");
            //当超时的时候
            if (teamTimes + DateUtils.formatDate(Config.getConfig(Config.TEAM_RETENTION_TIME)) < System.currentTimeMillis()) {
                //删除队伍
                removeTeamSize++;
                data.set(teamLeader, null);
                continue;
            }
            // 载入成员
            List<String> teamMembers = data.getStringList(teamLeader + ".Members");
            // 载入物品
            List<ItemStack> teamItems = (List<ItemStack>) data.getList(teamLeader + ".Items");
            // 队伍数据
            TeamData teamData = new TeamData(teamLeader, teamMembers, teamItems, teamTimes);
            // 载入日志
            if (data.contains(teamLeader + ".Notes")) {
                for (String id : data.getConfigurationSection(teamLeader + ".Notes").getKeys(false)) {
                    teamData.getItemNotes().add(new NoteData(
                            data.getString(teamLeader + ".Notes." + id + ".Name"),
                            data.getString(teamLeader + ".Notes." + id + ".Item"),
                            data.getLong(teamLeader + ".Notes." + id + ".Date")));
                }
            }
            // 载入设置
            if (data.contains(teamLeader + ".Options")) {
                for (String id : data.getConfigurationSection(teamLeader + ".Options").getKeys(false)) {
                    teamData.setTeamOption(id, data.getBoolean(teamLeader + ".Options." + id));
                }
            }
            teamList.add(teamData);
        }
        Bukkit.getConsoleSender().sendMessage("[BS-Teams] §7已载入 §6" + teamList.size() + " §7条队伍数据");
        if (removeTeamSize > 0) {
            Bukkit.getConsoleSender().sendMessage("[BS-Teams] §7已清除 §c" + removeTeamSize + " §7条过时队伍数据");
        }


        // 数据保存
        new BukkitRunnable() {
            @Override
            public void run() {
                saveTeamList();
            }
        }.runTaskTimerAsynchronously(BSTeamsPlugin.getInst(), 1200, 1200);

        // 邀请有效期检查
        new BukkitRunnable() {
            @Override
            public void run() {
                ClearOverdueInvite();
            }
        }.runTaskTimerAsynchronously(BSTeamsPlugin.getInst(), 20, 20);

        // 申请有效期检查
        new BukkitRunnable() {
            @Override
            public void run() {
                ClearOverdueJoin();
            }
        }.runTaskTimerAsynchronously(BSTeamsPlugin.getInst(), 20, 20);
    }

    /**
     * 创建队伍
     *
     * @param player 玩家
     */
    public TeamData createTeam(Player player) {
        TeamData data = new TeamData(player.getName(), null, null, null);
        teamList.add(data);
        return data;
    }

    /**
     * 获取玩家队伍
     *
     * @param playerName 玩家名
     * @return boolean
     */
    public TeamData getTeam(String playerName) {
        for (TeamData teamData : teamList) {
            if (teamData.getTeamLeader().equals(playerName) || teamData.getTeamMembers().contains(playerName)) {
                return teamData;
            }
        }
        return null;
    }

    /**
     * 是否为队长
     *
     * @param playerName 玩家名
     * @return boolean
     */
    public boolean isTeamLeader(String playerName) {
        return getTeam(playerName) != null && getTeam(playerName).getTeamLeader().equals(playerName);
    }

    /**
     * 删除队伍
     *
     * @param teamData 队伍
     */
    public void removeTeam(TeamData teamData) {
        if (teamList.contains(teamData)) {
            teamList.remove(teamData);
        }
    }

    /**
     * 保存队伍列表
     */
    public void saveTeamList() {
        Long oldTimes = System.nanoTime();
        // 清空数据
        data = new YamlConfiguration();
        // 遍历队伍
        for (TeamData teamData : teamList) {
            String teamLeader = teamData.getTeamLeader();
            Long teamTimes = teamData.getTeamTimes();
            List<String> teamMembers = teamData.getTeamMembers();
            List<ItemStack> teamItems = teamData.getTeamItems();
            data.set(teamLeader + ".Time", teamTimes);
            data.set(teamLeader + ".Members", teamMembers);
            data.set(teamLeader + ".Items", teamItems);
            // 提取日志
            int i = 0;
            for (NoteData note : teamData.getItemNotes()) {
                data.set(teamLeader + ".Notes." + i + ".Name", note.getPlayer());
                data.set(teamLeader + ".Notes." + i + ".Item", note.getItemName());
                data.set(teamLeader + ".Notes." + i + ".Date", note.getDate());
                i++;
            }
            for (Entry<String, Boolean> value : teamData.getTeamOptions().entrySet()) {
                data.set(teamLeader + ".Options." + value.getKey(), value.getValue());
            }
        }
        // 保存
        DataUtils.saveConfiguration(data, file);
        // 时间
        double endTimes = ((System.nanoTime() - oldTimes) / 1000000D);
        // 提示
        if (Config.getConfig().getBoolean(Config.SAVE_MESSAGE)) {
            BSTeamsPlugin.getLanguage().get("Admin.DataSaved")
                    .addPlaceholder("$teams", String.valueOf(teamList.size()))
                    .addPlaceholder("$time", String.valueOf(endTimes))
                    .console();
        }
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public YamlConfiguration getData() {
        return data;
    }

    public ArrayList<TeamData> getTeamList() {
        return teamList;
    }

    public ConcurrentHashMap<String, List<String>> getInviteMap() {
        return inviteMap;
    }

    public ConcurrentHashMap<String, List<String>> getJoinMap() {
        return joinMap;
    }

    public CooldownPack2 getCooldownInvite() {
        return cooldownInvite;
    }

    public CooldownPack2 getCooldownApply() {
        return cooldownApply;
    }
}
