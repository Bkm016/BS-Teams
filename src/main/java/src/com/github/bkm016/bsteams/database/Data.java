package com.github.bkm016.bsteams.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Data {
	public static YamlConfiguration data;
	static File dataFile = new File("plugins" + File.separator + "BS-Teams" + File.separator + "Data.dat");

	
	static public void loadData(){
		//检测data.dat是否存在
		if(!dataFile.exists()){
	        Bukkit.getConsoleSender().sendMessage("[BS-Teams] §cCreate Data.dat");
			data = new YamlConfiguration();
			try {data.save(dataFile);} catch (IOException e) {e.printStackTrace();}
		}else{
	        Bukkit.getConsoleSender().sendMessage("[BS-Teams] §aFind Data.dat");
		}
		data = new YamlConfiguration();
		try {data.load(dataFile);} catch (IOException | InvalidConfigurationException e) {e.printStackTrace();Bukkit.getConsoleSender().sendMessage("[BS-Teams] §c读取data时发生错误");}
	}


}
