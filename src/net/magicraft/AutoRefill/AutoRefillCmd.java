package net.magicraft.AutoRefill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AutoRefillCmd implements CommandExecutor {

	CmdDesc[] help = new CmdDesc[] { 
			new CmdDesc("/autorefill help [page]", "Shows this menu", null),
			new CmdDesc("/autorefill add", "Makes the target block an infinite dispenser", "autorefill.cmd.create"),
			new CmdDesc("/autorefill remove", "Disables the target block if it was an infinite dispenser", "autorefill.cmd.create"),
			new CmdDesc("/autorefill pos1", "Sets point 1 to current location", "autorefill.cmd.create"), 
			new CmdDesc("/autorefill pos2", "Sets point 2 to current location", "autorefill.cmd.create"),
			new CmdDesc("/autorefill add chunk", "Makes all dispensers between pos1 and pos2 infinite", "autorefill.cmd.create"),
			new CmdDesc("/autorefill remove chunk", "Disables all infinite dispensers between pos1 and pos2", "autorefill.cmd.create"),
			new CmdDesc("/autorefill list [page]", "Lists all infinite dispensers", "autorefill.cmd.create")
	};

	private HashMap<String, Location> p1 = new HashMap<String, Location>();
	private HashMap<String, Location> p2 = new HashMap<String, Location>();

	private AutoRefillPlugin plugin;

	public AutoRefillCmd(AutoRefillPlugin plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("help")) {
				helpCmd(sender, args, "AutoRefill", this.help);
			} else if (args[0].equalsIgnoreCase("pos1")) {
				positionCmd(sender, 1, args);
			} else if (args[0].equalsIgnoreCase("pos2")) {
				positionCmd(sender, 2, args);
			} else if (args[0].equalsIgnoreCase("add")) {
				refillCmd(sender, args, true);
			} else if (args[0].equalsIgnoreCase("remove")) {
				refillCmd(sender, args, false);
			} else if (args[0].equalsIgnoreCase("list")) {
				listCmd(sender, args);
			} else {
				sender.sendMessage(ChatColor.GOLD + "Command unrecognized. Type " + ChatColor.AQUA + "/autorefill help" + ChatColor.GOLD + " for help.");
			} 
		} else {
			sender.sendMessage(ChatColor.GOLD + "AutoRefill version " + this.plugin.getDescription().getVersion() + ChatColor.GOLD + " by " + ChatColor.AQUA + "MagicIdol");
			sender.sendMessage(ChatColor.GOLD + "Type \"" + ChatColor.AQUA + "/autorefill help" + ChatColor.GOLD + "\" for help!");
		} 
		return true;
	}

	public boolean refillCmd(CommandSender sender, String[] args, boolean create) {
		if (noPerm(sender, "autorefill.cmd.create"))
			return true; 
		if (noConsole(sender))
			return true; 
		if (args.length > 2 || (args.length == 2 && !args[1].equalsIgnoreCase("chunk")))
			return usage(sender, "autorefill " + args[0].toLowerCase() + " chunk");
		
		Player player = (Player)sender; 
		// handle single targeted block
		if (args.length == 1) {
			Block b = player.getTargetBlock((HashSet<Material>) null, 5);
			if (b == null)
				return msg(sender, ChatColor.RED + "You must look at a block!"); 
			if (b.getType() != Material.DISPENSER && b.getType() != Material.DROPPER)
				return msg(sender, ChatColor.RED + "You must target a dispenser or dropper! You targeted: " + ChatColor.YELLOW + b.getType().toString()); 
			// create target
			if (create) {
				if (this.plugin.getDataManager().addInfiniteDispenser(b.getLocation()))
					sender.sendMessage(ChatColor.GREEN + "Targeted dispenser set to infinite!");
				else
					sender.sendMessage(ChatColor.RED + "This dispenser is already infinite!");
			// delete target
			} else {
				if (this.plugin.getDataManager().removeInfiniteDispenser(b.getLocation()))
					sender.sendMessage(ChatColor.YELLOW + "Targeted dispenser set to normal!");
				else
					sender.sendMessage(ChatColor.RED + "This dispenser is not infinite!");
			}
		// handle chunk of blocks
		} else {
			Location c1 = this.p1.get(player.getName());
			Location c2 = this.p2.get(player.getName());
			if (c1 == null || c2 == null || !c1.getWorld().equals(c2.getWorld()))
				return msg(sender, ChatColor.RED + "You must first select two points in the same world!");
			double total_count = 0;
			double new_count = 0;
			double volume = Math.abs(c1.getBlockX() - c2.getBlockX()) * Math.abs(c1.getBlockY() - c2.getBlockY()) * Math.abs(c1.getBlockZ() - c2.getBlockZ());
			sender.sendMessage(ChatColor.YELLOW + "Scanning for dispensers in " + Double.toString(volume) + " blocks...");
			for (double x = Math.min(c1.getBlockX(), c2.getBlockX()); x <= Math.max(c1.getBlockX(), c2.getBlockX()); x++) {
				for (double y = Math.min(c1.getBlockY(), c2.getBlockY()); y <= Math.max(c1.getBlockY(), c2.getBlockY()); y++) {
					for (double z = Math.min(c1.getBlockZ(), c2.getBlockZ()); z <= Math.max(c1.getBlockZ(), c2.getBlockZ()); z++) {
						Block b = c1.getWorld().getBlockAt(new Location(c1.getWorld(), x, y, z));
						if (b.getType() == Material.DISPENSER || b.getType() == Material.DROPPER) {
							total_count++;
							// creating infinite dispensers
							if (create) {
								if (this.plugin.getDataManager().addInfiniteDispenser(b.getLocation()))
									new_count++;
							// removing infinite dispensers
							} else {
								if (this.plugin.getDataManager().removeInfiniteDispenser(b.getLocation()))
									new_count++;
							}		
						}
					}
				}
			}
			if (create)
				sender.sendMessage(ChatColor.GREEN + "Scan complete!\n" + ChatColor.YELLOW + Double.toString(new_count) + " new infinite dispensers created\n" + Double.toString(total_count) + " total infinite dispensers in area");
			else
				sender.sendMessage(ChatColor.GREEN + "Scan complete!\n" + ChatColor.YELLOW + Double.toString(new_count) + " infinite dispensers removed\n" + Double.toString(total_count) + " total normal dispensers in area");
		}
		return true;
	}

	public boolean positionCmd(CommandSender sender, int num, String[] args) {
		if (noPerm(sender, "autorefill.cmd.create"))
			return true; 
		if (noConsole(sender))
			return true;
		
		Player player = (Player)sender;
		Location pos = player.getLocation().getBlock().getLocation();
		if (num == 1) {
			this.p1.put(player.getName(), pos);
		} else {
			this.p2.put(player.getName(), pos);
		} 
		sender.sendMessage(ChatColor.GREEN + "Position " + num + " set to " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
		return true;
	}

	public boolean listCmd(CommandSender sender, String[] args) {
		if (noPerm(sender, "autorefill.cmd.create"))
			return true; 
		if (args.length != 2 && args.length != 1)
			return usage(sender, "autorefill list [page]"); 
		ArrayList<InfiniteDispenser> dispensers = this.plugin.getDataManager().getDispenserList();
		int page = 1;
		if (args.length == 2)
			try {
				page = Integer.parseInt(args[1]);
			} catch (NumberFormatException nfe) {
				return msg(sender, ChatColor.RED + "\"" + args[1] + "\" is not a valid number");
			}  
		ArrayList<String> d = new ArrayList<String>();
		int max = 1;
		for (int i = 0; i < dispensers.size(); i++) {
			if (i > 10 && i % 10 == 1)
				max++; 
			if (d.size() < 10)
				if (i >= (page - 1) * 10 && i <= (page - 1) * 10 + 9) {
					InfiniteDispenser id = dispensers.get(i);
					d.add(ChatColor.AQUA + id.toString());
				}
		} 
		sender.sendMessage(ChatColor.GOLD + "Infinite Dispenser list (" + ChatColor.AQUA + page + ChatColor.GOLD + "/" + ChatColor.AQUA + max + ChatColor.GOLD + "), " + ChatColor.AQUA + dispensers.size() + ChatColor.GOLD + " total");
		for (String s : d)
			sender.sendMessage(s); 
		return true;
	}

	public boolean helpCmd(CommandSender sender, String[] args, String title, CmdDesc[] help) {
		int page = 1;
		if (args.length == 2)
			try {
				page = Integer.parseInt(args[1]);
			} catch (NumberFormatException nfe) {
				return msg(sender, ChatColor.RED + "\"" + args[1] + "\" is not a valid number");
			}  
		ArrayList<String> d = new ArrayList<String>();
		int max = 1;
		int cmda = 0;
		for (int i = 0; i < help.length; i++) {
			CmdDesc c = help[i];
			if (c.getPerm() != null)
				if (!sender.hasPermission(c.getPerm()))
					continue;  
			if (d.size() < 10)
				if (i >= (page - 1) * 10 && i <= (page - 1) * 10 + 9)
					d.add(c.asDef());  
			if (cmda > 10 && cmda % 10 == 1)
				max++; 
			cmda++;
			continue;
		} 
		sender.sendMessage(ChatColor.GOLD + title + " Help (" + ChatColor.AQUA + page + ChatColor.GOLD + "/" + ChatColor.AQUA + max + ChatColor.GOLD + "), " + ChatColor.AQUA + cmda + ChatColor.GOLD + " total");
		for (String s : d)
			sender.sendMessage(s); 
		return true;
	}

	private boolean noConsole(CommandSender sender) {
		if (sender instanceof Player)
			return false; 
		sender.sendMessage(ChatColor.RED + "This command can only be executed as an in-game player!");
		return true;
	}

	private boolean noPerm(CommandSender sender, String node) {
		if (sender.hasPermission(node))
			return false; 
		sender.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
		return true;
	}

	private boolean msg(CommandSender sender, String message) {
		sender.sendMessage(message);
		return true;
	}

	private boolean usage(CommandSender sender, String cmd) {
		return msg(sender, ChatColor.RED + "Usage: " + ((sender instanceof Player) ? "/" : "") + cmd);
	}

	private class CmdDesc {
		
		private String cmd;
		private String desc;
		private String perm;

		private CmdDesc(String cmd, String desc, String perm) {
			this.cmd = cmd;
			this.desc = desc;
			this.perm = perm;
		}

		public String asDef() {
			return ChatColor.AQUA + cmd + " - " + ChatColor.GOLD + desc;
		}

		public String getPerm() {
			return this.perm;
		}
	}
}
