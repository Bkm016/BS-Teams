package com.github.bkm016.bsteams.api;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import com.github.bkm016.bsteams.BSTeamsPlugin;

/**
 * @author sky
 * @since 2018-03-08 21:03:20
 */
public class BSTeamsPluginAPI {
	
	private static BSTeamsPluginAPI inst;
	
	private BSTeamsPluginAPI() {}
	
	public static BSTeamsPluginAPI getInst() {
		if (inst == null) {
			synchronized (BSTeamsPluginAPI.class) {
				if (inst == null) {
					inst = new BSTeamsPluginAPI();
				}
			}
		}
		return inst;
	}
	
	/**
	 * 设置玩家是否跳过经验共享
	 * 
	 * @param player 玩家
	 * @param share 值
	 */
	public void setExperienceShare(Player player, boolean share) {
		if (!share) {
			player.setMetadata("bsteams|share-experience", new FixedMetadataValue(BSTeamsPlugin.getInst(), 1));
		} else {
			player.removeMetadata("bsteams|share-experience", BSTeamsPlugin.getInst());
		}
	}
	
	/**
	 * 是否共享经验
	 * 
	 * @param player 玩家
	 * @return boolean
	 */
	public boolean isExperienceShare(Player player) {
		return !player.hasMetadata("bsteams|share-experience");
	}
	
	/**
	 * 设置玩家是否跳过战利品
	 * 
	 * @param player 玩家
	 * @param share 值
	 */
	public void setDropsShare(Player player, boolean share) {
		if (!share) {
			player.setMetadata("bsteams|share-drops", new FixedMetadataValue(BSTeamsPlugin.getInst(), 1));
		} else {
			player.removeMetadata("bsteams|share-drops", BSTeamsPlugin.getInst());
		}
	}
	
	/**
	 * 是否共享战利品
	 * 
	 * @param player 玩家
	 * @return boolean
	 */
	public boolean isDropsShare(Player player) {
		return !player.hasMetadata("bsteams|share-drops");
	}
	
	/**
	 * 设置掉落物不被队伍共享
	 * 
	 * @param item
	 */
	public void setDropNoShare(Item item) {
		item.setMetadata("bsteams|share-drops", new FixedMetadataValue(BSTeamsPlugin.getInst(), 1));
	}
	
	/**
	 * 物品是否不被共享
	 * 
	 * @return
	 */
	public boolean isDropNoShare(Item item) {
		return item.hasMetadata("bsteams|share-drops");
	}
}
