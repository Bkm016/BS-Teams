package com.github.bkm016.bsteams.book;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

	private static BSTeamsPlugin plugin;
	
	@Getter
	private HashMap<String, Boolean> options = new LinkedHashMap<>();

	public static void setup(BSTeamsPlugin plugin){
		if (plugin != null) BookHandler.plugin = plugin;
	}


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
	 * 打开玩家界面
	 * 
	 * @param player
	 */
	public void openPlayer(Player player) {
		// 语言文件
		Language2 lang = BSTeamsPlugin.getLanguage();
		// 创建书本
		BookBuilder book = BookFormatter.writtenBook();
		
		// page 1
		PageBuilder page1 = new BookFormatter.PageBuilder().newLine().newLine();
		
		// 创建队伍
		page1.add(lang.get("TEAM-HELP-CREATE-1").asString());
		page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-HELP-CREATE-2").asString())
				.onClick(BookFormatter.ClickAction.runCommand("/bsteams create"))
				.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-HELP-CREATE-TEXT").asString()))
				.build());
		page1.add(lang.get("TEAM-HELP-CREATE-3").asString()).newLine().newLine();
		
		// 队伍邀请
		page1.add(lang.get("TEAM-HELP-INVITE-1").asString());
		page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-HELP-INVITE-2").asString())
				.onClick(BookFormatter.ClickAction.runCommand("/bsteams accept"))
				.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-HELP-INVITE-TEXT").asString()))
				.build());
		page1.add(lang.get("TEAM-HELP-INVITE-3").asString()).newLine().newLine();
		
		// 队伍列表
		page1.add(lang.get("TEAM-HELP-LIST-1").asString());
		page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-HELP-LIST-2").asString())
				.onClick(BookFormatter.ClickAction.runCommand("/bsteams list"))
				.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-HELP-LIST-TEXT").asString()))
				.build());
		page1.add(lang.get("TEAM-HELP-LIST-3").asString()).newLine().newLine();
		
		// 打开界面
		BookFormatter.forceOpen(player, book.addPages(page1.build()).build());
		// 音效
		player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1f, 1f);
	}
	
	
	/**
	 * 打开申请列表
	 * 
	 * @param player
	 */
	public void openApply(Player player) {
		// 语言文件
		Language2 lang = BSTeamsPlugin.getLanguage();
		// 创建书本
		BookBuilder book = BookFormatter.writtenBook();
		
		// page 1
		PageBuilder page1 = new BookFormatter.PageBuilder().add(lang.get("TEAM-APPLY-TITLE").asString()).endLine().newLine();
		
		// 邀请列表
		int i = 1;
		for (String name : plugin.getTeamDataManager().getJoinList(player.getName())) {
			// 显示信息
			String showText = lang.get("TEAM-APPLY-PLAYER")
					.addPlaceholder("$number", String.valueOf(i))
					.addPlaceholder("$name", name.split(":")[0]).toString();
			// 队伍信息
			page1.add(new BookFormatter.TextBuilder(showText)
					.onClick(BookFormatter.ClickAction.runCommand("/bsteams acceptJoin " + name.split(":")[0]))
					.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-APPLY-PLAYER-TEXT", name.split(":")[0]).asString()))
					.build()).endLine();
			i++;
		}
		
		// 如果没有队伍
		if (i == 1) {
			page1.add(lang.get("TEAM-APPLY-EMPTY").asString()).endLine();
		}
		
		// 换行
		page1.newLine();
		page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-APPLY-BACK").asString())
				.onClick(BookFormatter.ClickAction.runCommand("/bsteams info"))
				.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-APPLY-BACK-TEXT").asString()))
				.build());
		// 刷新
		page1.add(lang.get("TEAM-INFO-BUTTON-SPLIT").asString());
		page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-APPLY-REFRESH").asString())
				.onClick(BookFormatter.ClickAction.runCommand("/bsteams acceptJoin"))
				.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-APPLY-REFRESH-TEXT").asString()))
				.build());
		
		// 删除所有
		if (i > 1) {
			page1.add(lang.get("TEAM-INFO-BUTTON-SPLIT").asString());
			page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-APPLY-CLEAR").asString())
					.onClick(BookFormatter.ClickAction.runCommand("/bsteams clearjoin"))
					.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-APPLY-CLEAR-TEXT").asString()))
					.build()).endLine();
		}
		
		// 打开界面
		BookFormatter.forceOpen(player, book.addPages(page1.build()).build());
		// 音效
		player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1f, 1f);
	}
	
	/**
	 * 打开邀请列表
	 * 
	 * @param player 玩家
	 */
	public void openInvite(Player player) {
		// 语言文件
		Language2 lang = BSTeamsPlugin.getLanguage();
		// 创建书本
		BookBuilder book = BookFormatter.writtenBook();
		
		// page 1
		PageBuilder page1 = new BookFormatter.PageBuilder().add(lang.get("TEAM-INVITE-TITLE").asString()).endLine().newLine();
		
		// 邀请列表
		int i = 1;
		for (String name : plugin.getTeamDataManager().getInviteList(player.getName())) {
			TeamData team = plugin.getTeamDataManager().getTeam(name.split(":")[0]);
			if (team == null) {
				continue;
			}
			// 显示信息
			String showText = lang.get("TEAM-INVITE-TEAMS")
					.addPlaceholder("$number", String.valueOf(i))
					.addPlaceholder("$name", team.getTeamLeader())
					.addPlaceholder("$members", String.valueOf(team.getTeamMembers().size()))
					.addPlaceholder("$max", String.valueOf(Config.getConfig().getInt(Config.TEAM_SIZE))).toString();
			// 队伍信息
			page1.add(new BookFormatter.TextBuilder(showText)
					.onClick(BookFormatter.ClickAction.runCommand("/bsteams accept " + team.getTeamLeader()))
					.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INVITE-TEAMS-TEXT", team.getTeamLeader()).asString()))
					.build()).endLine();
			i++;
		}
		
		// 如果没有队伍
		if (i == 1) {
			page1.add(lang.get("TEAM-INVITE-EMPTY").asString()).endLine();
		} 
		
		// 换行
		page1.newLine();
		page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INVITE-BACK").asString())
				.onClick(BookFormatter.ClickAction.runCommand("/bsteams"))
				.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INVITE-BACK-TEXT").asString()))
				.build());
		// 刷新
		page1.add(lang.get("TEAM-INFO-BUTTON-SPLIT").asString());
		page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INVITE-REFRESH").asString())
				.onClick(BookFormatter.ClickAction.runCommand("/bsteams accept"))
				.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INVITE-REFRESH-TEXT").asString()))
				.build());
		
		// 删除所有
		if (i > 1) {
			page1.add(lang.get("TEAM-INFO-BUTTON-SPLIT").asString());
			page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INVITE-CLEAR").asString())
					.onClick(BookFormatter.ClickAction.runCommand("/bsteams clearinvite"))
					.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INVITE-CLEAR-TEXT").asString()))
					.build()).endLine();
		}
		
		// 打开界面
		BookFormatter.forceOpen(player, book.addPages(page1.build()).build());
		// 音效
		player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1f, 1f);
	}
	
	/**
	 * 打开队伍列表
	 */
	public void openList(Player player) {
		// 是否有队伍
		boolean isFormTeam = plugin.getTeamDataManager().getTeam(player.getName()) != null;
		// 语言文件
		Language2 lang = BSTeamsPlugin.getLanguage();
		// 创建书本
		BookBuilder book = BookFormatter.writtenBook();
		
		// page 1
		PageBuilder page1 = new BookFormatter.PageBuilder().add(lang.get("TEAM-LIST-TITLE").asString()).endLine().newLine();
		
		// 队伍列表
		int i = 1;
		for (TeamData team : plugin.getTeamDataManager().getTeamList()) {
			// 队长离线
			if (Bukkit.getPlayerExact(team.getTeamLeader()) == null) {
				continue;
			}
			// 成员已满
			if (team.getTeamMembers().size() >= Config.getConfig().getInt(Config.TEAM_SIZE)) {
				continue;
			}
			// 队伍公开
			if (!team.getTeamOption("PUBLIC", true)) {
				continue;
			}
			// 显示信息
			String showText = lang.get("TEAM-LIST-TEAMS")
					.addPlaceholder("$number", String.valueOf(i))
					.addPlaceholder("$name", team.getTeamLeader())
					.addPlaceholder("$members", String.valueOf(team.getTeamMembers().size()))
					.addPlaceholder("$max", String.valueOf(Config.getConfig().getInt(Config.TEAM_SIZE))).toString();
			// 队伍信息
			if (!isFormTeam) {
				page1.add(new BookFormatter.TextBuilder(showText)
						.onClick(BookFormatter.ClickAction.runCommand("/bsteams join " + team.getTeamLeader()))
						.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-LIST-TEAMS-TEXT", team.getTeamLeader()).asString()))
						.build()).endLine();
			} else {
				page1.add(showText).endLine();
			}
			i++;
		}
		// 如果没有队伍
		if (i == 1) {
			page1.add(lang.get("TEAM-LIST-EMPTY").asString()).endLine();
		}
		
		
		// 换行
		page1.newLine();
		page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-LIST-BACK").asString())
				.onClick(BookFormatter.ClickAction.runCommand("/bsteams"))
				.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-LIST-BACK-TEXT").asString()))
				.build());
		// 刷新
		page1.add(lang.get("TEAM-INFO-BUTTON-SPLIT").asString());
		page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-LIST-REFRESH").asString())
				.onClick(BookFormatter.ClickAction.runCommand("/bsteams list"))
				.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-LIST-REFRESH-TEXT").asString()))
				.build());
		
		// 打开界面
		BookFormatter.forceOpen(player, book.addPages(page1.build()).build());
		// 音效
		player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1f, 1f);
	}
	
	/**
	 * 打开队伍信息
	 * 
	 * @param player
	 */
	public void openInfo(Player player, TeamData teamData) {
		// 是否为队长
		boolean isLeader = teamData.getTeamLeader().equals(player.getName());
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
					.onClick(BookFormatter.ClickAction.runCommand("/bsteams open " + teamData.getTeamLeader()))
					.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INFO-BUTTON-INVENTORY-TEXT").asString()))
					.build()).add(lang.get("TEAM-INFO-BUTTON-SPLIT").asString());
		}
		// 如果是队长
		if (isLeader) {
			// 队伍申请
			page1.add(new BookFormatter.TextBuilder(lang.get("TEAM-INFO-BUTTON-APPLY").asString())
					.onClick(BookFormatter.ClickAction.runCommand("/bsteams acceptJoin"))
					.onHover(BookFormatter.HoverAction.showText(lang.get("TEAM-INFO-BUTTON-APPLY-TEXT").asString()))
					.build()).add(lang.get("TEAM-INFO-BUTTON-SPLIT").asString());
			// 解散队伍
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
