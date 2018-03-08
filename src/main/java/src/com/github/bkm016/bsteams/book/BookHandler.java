package com.github.bkm016.bsteams.book;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.util.Config;
import com.github.bkm016.spigot.book.BookFormatter;
import com.github.bkm016.spigot.book.BookFormatter.BookBuilder;
import com.github.bkm016.spigot.book.BookFormatter.PageBuilder;

import lombok.Getter;
import me.skymc.taboolib.string.language2.Language2;

/**
 * @author sky
 * @since 2018-03-07 23:46:23
 */
public class BookHandler {
	
	private static BookHandler inst;
	
	@Getter
	private HashMap<String, Boolean> options = new LinkedHashMap<>();
	
	private BookHandler() {
		reloadOptions();
	}
	
	public static BookHandler getInst() {
		if (inst == null) {
			synchronized (BookHandler.class) {
				if (inst == null) {
					inst = new BookHandler();
				}
			}
		}
		return inst;
	}
	
	/**
	 * 重载队伍设置
	 */
	public void reloadOptions() {
		options.clear();
		// 公开
		options.put("PUBLIC", true);
		// 友方伤害
		options.put("FRIENDLY-FIRE", false);
		// 分享经验
		if (Config.getConfig().getBoolean(Config.SHARE_EXPERIENCE_ENABLE)) {
			options.put("SHARE-EXPERIENCE", true);
		}
		// 分享掉落
		if (Config.getConfig().getBoolean(Config.SHARE_DROPS_ENABLE)) {
			options.put("SHARE-DROPS", true);
		}
	}
	
	/**
	 * 打开队伍信息
	 * 
	 * @param player
	 */
	public void openInfo(Player player, TeamData teamData) {
		// 是否为队长
		boolean isLeader = teamData.getTeamLeader().equals(player.getName()) || player.isOp();
		// 语言文件
		Language2 lang = BSTeamsPlugin.getLanguage();
		// 创建书本
		BookBuilder book = BookFormatter.writtenBook();
		
		// page 1
		PageBuilder page1 = new BookFormatter.PageBuilder().add(lang.get("TEAM-INFO-TITLE").asString()).endLine().newLine().add(lang.get("TEAM-INFO-CAPTAIN").asString());
		// 获取队长
		Player leader = Bukkit.getPlayerExact(teamData.getTeamLeader());
		if (leader == null) {
			page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INFO-CAPTAIN-OFFLINE").addPlaceholder("$captain", teamData.getTeamLeader()).asString())
					.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INFO-STATE-OFFLINE").asString()))
					.build());
		}
		else if (leader.isDead()) {
			page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INFO-CAPTAIN-DEATH").addPlaceholder("$captain", teamData.getTeamLeader()).asString())
					.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INFO-STATE-DEATH").asString()))
					.build());
		}
		else {
			page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INFO-CAPTAIN-ONLINE").addPlaceholder("$captain", teamData.getTeamLeader()).asString())
					.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INFO-STATE-ONLINE").asString()))
					.build());
		}
		
		// 队伍成员
		page1.newLine().add(lang.get("TEAM-INFO-MEMBERS").asString()).endLine();
		for (String member : teamData.getTeamMembers()) {
			Player memberPlayer = Bukkit.getPlayerExact(member);
			if (memberPlayer == null) {
				page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INFO-MEMBERS-OFFLINE").addPlaceholder("$member", member).asString())
						.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INFO-STATE-OFFLINE").asString()))
						.build());
			}
			else if (memberPlayer.isDead()) {
				page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INFO-MEMBERS-DEATH").addPlaceholder("$member", member).asString())
						.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INFO-STATE-DEATH").asString()))
						.build());
			}
			else {
				page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INFO-MEMBERS-ONLINE").addPlaceholder("$member", member).asString())
						.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INFO-STATE-ONLINE").asString()))
						.build());
			}
			// 如果是队长
			if (isLeader) {
				page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INFO-MEMBERS-KICK").asString())
						.onClick(BookFormatter.ClickAction.runCommand("/bsteams kick " + member))
						.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INFO-MEMBERS-KICK-TEXT").asString()))
						.build());
			}
			// 换行
			page1.newLine();
		}
		
		// 补全成员
		for (int i = teamData.getTeamMembers().size() ; i < Config.getConfig().getInt(Config.TEAM_SIZE) ; i++) {
			page1.add(lang.get("TEAM-INFO-MEMBERS-EMPTY").asString());
			// 如果是队长
			if (isLeader) {
				page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INFO-MEMBERS-INVITE").asString())
						.onClick(BookFormatter.ClickAction.runCommand("/bsteams invite"))
						.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INFO-MEMBERS-INVITE-TEXT").asString()))
						.build());
			}
			// 换行
			page1.newLine();
		}
		
		// 队伍背包
		page1.newLine();
		// 是否启用队伍背包
		if (teamData.getTeamOption("SHARE-DROPS", true)) {
			page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INFO-BUTTON-INVENTORY").asString())
					.onClick(BookFormatter.ClickAction.runCommand("/bsteams open"))
					.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INFO-BUTTON-INVENTORY-TEXT").asString()))
					.build()).add(lang.get("TEAM-INFO-BUTTON-SPLIT").asString());
		}
		// 解散队伍
		if (isLeader) {
			page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INFO-BUTTON-DISSOLVE").asString())
					.onClick(BookFormatter.ClickAction.runCommand("/bsteams dissolve"))
					.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INFO-BUTTON-DISSOLVE-TEXT").asString()))
					.build());
		} 
		// 退出队伍
		else {
			page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INFO-BUTTON-QUIT").asString())
					.onClick(BookFormatter.ClickAction.runCommand("/bsteams quit"))
					.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INFO-BUTTON-QUIT-TEXT").asString()))
					.build());
		}
		
		// 添加页数
		book.addPages(page1.build());
		
		// 如果是队长
		if (isLeader) {
			// page 2
			PageBuilder page2 = new BookFormatter.PageBuilder().add(lang.get("TEAM-OPTIONS-TITLE").asString()).endLine().newLine();
			// 获取所有注册设置
			for (Entry<String, Boolean> entry : BookHandler.getInst().getOptions().entrySet()) {
				// 设置名称
				page2.add(new BookFormatter.TextBuilder(lang.get("TEAM-OPTIONS-" + entry.getKey() + "-NAME").asString())
						.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-OPTIONS-" + entry.getKey() + "-NAME-TEXT").asString()))
						.build());
				// 设置参数
				if (teamData.getTeamOption(entry.getKey(), entry.getValue())) {
					page2.add(new BookFormatter.TextBuilder(lang.get("TEAM-OPTIONS-" + entry.getKey() + "-ENABLE").asString())
							.onClick(BookFormatter.ClickAction.runCommand("/bsteams option " + entry.getKey() + " false"))
							.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-OPTIONS-" + entry.getKey() + "-ENABLE-TEXT").asString()))
							.build());
				}
				else {
					page2.add(new BookFormatter.TextBuilder(lang.get("TEAM-OPTIONS-" + entry.getKey() + "-DISABLE").asString())
							.onClick(BookFormatter.ClickAction.runCommand("/bsteams option " + entry.getKey() + " true"))
							.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-OPTIONS-" + entry.getKey() + "-DISABLE-TEXT").asString()))
							.build());
				}
				// 换行
				page2.newLine();
			}
			// 添加页数
			book.addPages(page2.build());
		}
		
		// 打开界面
		BookFormatter.forceOpen(player, book.build());
		// 音效
		player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1f, 1f);
	}
}
