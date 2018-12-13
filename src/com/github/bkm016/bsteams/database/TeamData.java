package com.github.bkm016.bsteams.database;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.bkm016.bsteams.inventory.DropInventory;
import com.github.bkm016.bsteams.inventory.DropInventoryHolder;
import com.github.bkm016.bsteams.util.Config;

import lombok.Getter;
import me.skymc.taboolib.inventory.ItemUtils;

/**
 * @author Saukiya
 * @since 2018年3月6日
 */
public class TeamData {

    private String teamLeader;
    private List<String> teamMembers = new ArrayList<>();
    private List<ItemStack> teamItems = new ArrayList<>();
    private List<NoteData> itemNotes = new LinkedList<>();
    private Map<String, Boolean> teamOptions = new HashMap<>();
    private long teamTimes;

    public TeamData(String teamLeader, List<String> teamMembers, List<ItemStack> teamItems, Long times) {
        this.teamLeader = teamLeader;
        if (teamMembers != null) {
            this.teamMembers = teamMembers;
        }
        if (teamItems != null) {
            this.teamItems = teamItems;
        }
        if (times != null) {
            this.teamTimes = times;
        } else {
            this.teamTimes = System.currentTimeMillis();
        }
    }

    /**
     * 获取队伍所有成员，包括队长
     *
     * @return {@link List}
     */
    public List<String> getTeamMembersAll() {
        List<String> list = new ArrayList<>(teamMembers);
        list.add(teamLeader);
        return list;
    }

    /**
     * 获取所有在线成员
     *
     * @param leader 是否获取队长
     * @return {@link List}
     */
    public List<Player> getTeamMembersOnline(boolean leader) {
        List<Player> list = new ArrayList<>();
        for (String name : leader ? getTeamMembersAll() : getTeamMembers()) {
            Player member = Bukkit.getPlayerExact(name);
            if (member != null) {
                list.add(member);
            }
        }
        return list;
    }

    /**
     * 更换队长 注意开启此功能时，需要null原队伍，否则会造成背包复制刷物品
     *
     * @param teamLeader
     * @return
     */
    @Deprecated
    public TeamData setTeamLeader(String teamLeader) {
        this.teamLeader = teamLeader;
        return this;
    }

    /**
     * 增加队员
     *
     * @param teamMember
     * @return
     */
    public TeamData addTeamMember(String teamMember) {
        if (!this.teamMembers.contains(teamMember)) {
            this.teamMembers.add(teamMember);
        }
        return this;
    }

    /**
     * 减少队员
     *
     * @param teamMember
     * @return
     */
    public TeamData removeTeamMember(String teamMember) {
        if (this.teamMembers.contains(teamMember)) {
            this.teamMembers.remove(teamMember);
        }
        return this;
    }

    /**
     * 清除队员 可能用不到
     *
     * @return
     */
    public TeamData clearTeamMember() {
        this.teamMembers.clear();
        return this;
    }

    /**
     * 增加物品到列表
     *
     * @param item
     * @return
     */
    public TeamData addTeamItems(ItemStack item) {
        this.teamItems.add(item);
        return this;
    }

    /**
     * 从物品列表中删除物品
     *
     * @param item
     * @return
     */
    public TeamData removeTeamItems(ItemStack item) {
        this.teamItems.remove(item);
        return this;
    }

    /**
     * 设置物品列表
     *
     * @param teamItems
     * @return
     */
    public TeamData setTeamItems(List<ItemStack> teamItems) {
        this.teamItems = teamItems;
        return this;
    }

    /**
     * 更新队伍保留时间
     *
     * @return
     */
    public TeamData updateTeamTime() {
        this.teamTimes = System.currentTimeMillis();
        return this;
    }


    /**
     * 更新服务器内所有该队伍的物品背包
     *
     * @return {@link TeamData}
     */
    public TeamData updateInventory() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof DropInventoryHolder) {
                DropInventoryHolder holder = (DropInventoryHolder) player.getOpenInventory().getTopInventory().getHolder();
                if (holder.getTeamData().equals(this)) {
                    DropInventory.openInventory(player, holder.getPage(), holder.getTeamData());
                }
            }
        }
        return this;
    }

    /**
     * 增加一条物品取出记录
     *
     * @param player 玩家
     * @param item   物品
     * @return {@link TeamData}
     */
    public TeamData addItemNote(Player player, ItemStack item) {
        itemNotes.add(0, new NoteData(player.getName(), ItemUtils.getCustomName(item) + "§f * " + item.getAmount(), System.currentTimeMillis()));
        // 判断数量
        while (itemNotes.size() > Config.getConfig().getInt(Config.NOTE_SIZE)) {
            itemNotes.remove(itemNotes.size() - 1);
        }
        return this;
    }

    /**
     * 获取队伍设置
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return boolean
     */
    public boolean getTeamOption(String name, boolean defaultValue) {
        return teamOptions.getOrDefault(name, defaultValue);
    }

    /**
     * 更改队伍设置
     *
     * @param name  名称
     * @param value 值
     */
    public void setTeamOption(String name, boolean value) {
        teamOptions.put(name, value);
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public String getTeamLeader() {
        return teamLeader;
    }

    public List<String> getTeamMembers() {
        return teamMembers;
    }

    public List<ItemStack> getTeamItems() {
        return teamItems;
    }

    public List<NoteData> getItemNotes() {
        return itemNotes;
    }

    public Map<String, Boolean> getTeamOptions() {
        return teamOptions;
    }

    public long getTeamTimes() {
        return teamTimes;
    }
}
