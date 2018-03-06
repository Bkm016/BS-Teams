package com.github.bkm016.spigot.book;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.github.bkm016.spigot.book.BookFormatter.BookBuilder;

/**
 * @author sky
 * @since 2018-03-06 21:31:55
 */
public class aa implements Listener{
	
	@EventHandler
	public void command(PlayerCommandPreprocessEvent e) {
		if (e.getMessage().equals("/booktest")) {
			e.setCancelled(true);
			
			long time = System.nanoTime();
			
			// 打开界面
			openBook(e.getPlayer());
			
			// 耗时
			e.getPlayer().sendMessage("测试界面运算耗时: " + ((System.nanoTime() - time)/1000000D) + "ms");
		}
	}
	
	public void openBook(Player player) {
		BookBuilder book = BookFormatter.writtenBook();
		
		// page 1
		book.addPages(
				new BookFormatter.PageBuilder()
					.add("§0§l§nFirst page!\n")
					.newLine()
					.add("§obalabalabal!\n")
					.build()
		);
		
		// page 2
		book.addPages(
				new BookFormatter.PageBuilder()
					.add(new BookFormatter.TextBuilder("Kill yasself\n")
								.onHover(BookFormatter.HoverAction.showText("Do it!"))
								.onClick(BookFormatter.ClickAction.runCommand("/kill"))
								.build())
					.newLine()
					.add(new BookFormatter.TextBuilder("Say 666")
							.onHover(BookFormatter.HoverAction.showText("Useful!"))
							.onClick(BookFormatter.ClickAction.runCommand("/say 666"))
							.build())
					.build()
		);
		
		BookFormatter.forceOpen(player, book.build());
	}

}
