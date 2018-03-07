package com.github.bkm016.bsteams.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.github.bkm016.bsteams.database.TeamData;

import lombok.Getter;

/**
 * @author sky
 * @since 2018-03-07 21:08:23
 */
public class DropInventoryHolder implements InventoryHolder {
	
	@Getter
	private TeamData teamData;
	
	@Getter
	private int page;
	
	public DropInventoryHolder(TeamData teamData, int page) {
		this.teamData = teamData;
		this.page = page;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}
}
