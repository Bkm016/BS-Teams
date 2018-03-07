package com.github.bkm016.bsteams.book;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.github.bkm016.bsteams.BSTeamsPlugin;
import com.github.bkm016.bsteams.database.TeamData;
import com.github.bkm016.bsteams.util.Config;
import com.github.bkm016.spigot.book.BookFormatter;
import com.github.bkm016.spigot.book.BookFormatter.BookBuilder;
import com.github.bkm016.spigot.book.BookFormatter.PageBuilder;

import me.skymc.taboolib.string.language2.Language2;

/**
 * @author sky
 * @since 2018-03-07 23:46:23
 */
public class BookHandler {
	
	private static BookHandler inst;
	
	private BookHandler() {}
	
	public static BookHandler getInst() {
		if (inst == null) {
			synchronized (BookHandler.class) {
				if (inst == null) {
					return new BookHandler();
				}
			}
		}
		return inst;
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
		PageBuilder page1 = new BookFormatter.PageBuilder()
				.add(lang.get("TEAM-INFO-TITLE").asString()).endLine()
				.newLine()
				.add(lang.get("TEAM-INFO-CAPTAIN").asString());
		
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
		
		// 打开界面
		BookFormatter.forceOpen(player, book.addPages(page1.build()).build());
		// 音效
		player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1f, 1f);
	}
}
