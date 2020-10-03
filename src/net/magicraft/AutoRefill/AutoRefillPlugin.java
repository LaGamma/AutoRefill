package net.magicraft.AutoRefill;

import net.milkbowl.vault.permission.Permission;

import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoRefillPlugin extends JavaPlugin {

	private static AutoRefillPlugin plugin = null;	
	private PermissionsHandler pmh;
	private DataManager dm;
	private final static Logger LOGGER = Logger.getLogger(AutoRefillPlugin.class.getName());

	public void onEnable() {
		plugin = this;
		this.dm = new DataManager(this);

		getCommand("autorefill").setExecutor(new AutoRefillCmd(this));
		getServer().getPluginManager().registerEvents(new AutoRefillListener(this.dm), (Plugin)this);
		if (getServer().getPluginManager().isPluginEnabled("Vault")) {
			RegisteredServiceProvider<Permission> provp = getServer().getServicesManager().getRegistration(Permission.class);
			if (provp != null) {
				Permission perms = (Permission) provp.getProvider();
				if (perms != null) {
					this.pmh = new PermissionsHandler(perms);
					LOGGER.info("Hooked into Vault's permissions!");
				} 
			}  
		} 
	}

	public void onDisable() {
		this.dm.cleanUp();
	}
	public DataManager getDataManager() {
		return this.dm;
	}
	public PermissionsHandler getPermissionsHandler() {
		return this.pmh;
	}
	public static AutoRefillPlugin getPlugin() {
		return plugin;
	}
}