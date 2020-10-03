package net.magicraft.AutoRefill;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.World;

public class DataManager {

	private AutoRefillPlugin plugin;
	private ArrayList<InfiniteDispenser> dispensers = new ArrayList<InfiniteDispenser>();
	private HashMap<Location, InfiniteDispenser> dispenserMapping = new HashMap<Location, InfiniteDispenser>();
	private File f;
	private File dispenser_data;

	public DataManager(AutoRefillPlugin plugin) {
		this.plugin = plugin;
		this.f = plugin.getDataFolder();
		this.dispenser_data = new File(this.f, "dispenser_data.txt");
		loadFiles();
	}
	
	public void createFiles() {
		if (!this.f.exists())
			this.f.mkdir(); 
		try { 
			if (!this.dispenser_data.exists()) {
				FileOutputStream fos = new FileOutputStream(this.dispenser_data);
				fos.flush();
				fos.close();
				FileWriter writer = new FileWriter(this.dispenser_data);
				writer.write("#Autogenerated InfiniteDispenser data, edit at your own risk\n");
				writer.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} 
	}
	
	public void loadFiles() {
		createFiles();
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.dispenser_data));
			String l;
			while ((l = br.readLine()) != null) {
				String[] t = l.split(":");
				if (t.length != 4)
					continue;
				final World w = this.plugin.getServer().getWorld(t[0]);
				final int x = Integer.parseInt(t[1]);
				final int y = Integer.parseInt(t[2]);
				final int z = Integer.parseInt(t[3]);
				addInfiniteDispenser(new Location(w, x, y, z));
			} 
			br.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} 
	}

	public void saveData() {
		createFiles();
		try {
			FileOutputStream fos = new FileOutputStream(this.dispenser_data);
			fos.flush();
			fos.close();
			FileWriter writer = new FileWriter(this.dispenser_data);
			writer.write("#Autogenerated InfiniteDispenser data, edit at your own risk\n");
			for (InfiniteDispenser i : this.dispensers) {
				Location l = i.getLoc();
				writer.write(l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ() + "\n");
			}
			writer.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} 
	}
	
	public void cleanUp() {
		saveData();
		this.dispensers.clear();
		this.dispenserMapping.clear();
	}

	public boolean addInfiniteDispenser(Location location) {
		if (location == null)
			return false;
		if (!dispenserMapping.containsKey(location)) {
			InfiniteDispenser id = new InfiniteDispenser(location);
			dispenserMapping.put(location, id);
			dispensers.add(id);
			return true;
		}
		return false;
	}

	public boolean removeInfiniteDispenser(Location location) {
		if (location == null)
			return false;
		if (dispenserMapping.containsKey(location)) {
			dispensers.remove(dispenserMapping.get(location));
			dispenserMapping.remove(location);
			return true;
		}
		return false;
	}
	
	public InfiniteDispenser getDispenser(Location location) {
		return dispenserMapping.get(location);
	}

	public HashMap<Location, InfiniteDispenser> getDispenserMapping() {
		return dispenserMapping;
	}

	public ArrayList<InfiniteDispenser> getDispenserList() {
		return dispensers;
	}
}