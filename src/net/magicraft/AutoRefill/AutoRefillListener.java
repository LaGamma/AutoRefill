package net.magicraft.AutoRefill;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;

public class AutoRefillListener implements Listener {

	private DataManager dm;

	public AutoRefillListener(DataManager dm) {
		this.dm = dm;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDispense(BlockDispenseEvent event) {	
		if (event.isCancelled()) return;
		InfiniteDispenser target = this.dm.getDispenserMapping().get(event.getBlock().getLocation());
		if (target != null) {
			if (event.getBlock().getState() instanceof Dispenser) {
				Dispenser dispenserBlock = (Dispenser) event.getBlock().getState();
				// set off dispenser
				//dispenser.setCooldownPeriod(currentPosition.getWorld().getFullTime());
				// create item stack
				ItemStack newItemStack = event.getItem().clone();
				dispenserBlock.getInventory().addItem(newItemStack);
				dispenserBlock.getInventory().addItem(newItemStack);
			} else if (event.getBlock().getState() instanceof Dropper) {
				Dropper dispenserBlock = (Dropper) event.getBlock().getState();
				// set off dispenser
				//dispenser.setCooldownPeriod(currentPosition.getWorld().getFullTime());
				// create item stack
				ItemStack newItemStack = event.getItem().clone();
				dispenserBlock.getInventory().addItem(newItemStack);
				dispenserBlock.getInventory().addItem(newItemStack);
			}			
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void checkBreak(BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.DISPENSER || event.getBlock().getType() == Material.DROPPER) {
			// loop through containers
			for(InfiniteDispenser dispenser : this.dm.getDispenserList()) {
				// check position
				if (dispenser.getLoc().equals(event.getBlock().getLocation())) {
					// check permission
					if (!event.getPlayer().hasPermission("autorefill.cmd.create")) {
						event.setCancelled(true);
						return;
					}
					// remove item
					this.dm.removeInfiniteDispenser(dispenser.getLoc());
					// notify user
					event.getPlayer().sendMessage(ChatColor.YELLOW + "You just destroyed an infinite dispenser");
					// skip all other loops
					return;
				}
			}
		}
	}
}