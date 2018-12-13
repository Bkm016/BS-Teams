package com.github.bkm016.bsteams.database;

/**
 * @author sky
 * @since 2018-03-07 22:02:14
 */
public class NoteData {

    private String player;
    private String itemName;
    private long date;

    public NoteData(String player, String itemName, long date) {
        this.player = player;
        this.itemName = itemName;
        this.date = date;
    }

    public String getPlayer() {
        return player;
    }

    public String getItemName() {
        return itemName;
    }

    public long getDate() {
        return date;
    }
}
