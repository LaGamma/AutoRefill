package net.magicraft.AutoRefill;

import org.bukkit.Location;

// currently just a stub class - nothing interesting here
public class InfiniteDispenser {
	
	private Location loc;

	public InfiniteDispenser(Location loc) {
		this.loc = loc;
	}

	public Location getLoc() {
		return this.loc;
	}
	
	public String toString() {
		return String.format("<%s, %d, %d, %d>", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
}