package com.github.bkm016.bsteams.database;

import lombok.Getter;

/**
 * @author sky
 * @since 2018-03-07 22:02:14
 */
public class NoteData {
	
	@Getter
	private String player;
	
	@Getter
	private String itemName;
	
	@Getter
	private long date;

	public NoteData(String player, String itemName, long date) {
		this.player = player;
		this.itemName = itemName;
		this.date = date;
	}
}
