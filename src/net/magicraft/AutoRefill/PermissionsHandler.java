package net.magicraft.AutoRefill;

import org.bukkit.entity.Player;

import net.milkbowl.vault.permission.Permission;

public class PermissionsHandler {
  private Permission perms;
  
  public PermissionsHandler(Permission perms) {
    this.perms = perms;
  }
  
  public void give(Player player, String perm) {
    this.perms.playerAdd(player, perm);
  }
  
  public void take(Player player, String perm) {
    this.perms.playerRemove(player, perm);
  }
  
  public boolean has(Player player, String perm) {
    return this.perms.has(player, perm);
  }
}