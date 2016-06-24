package com.lookcook.particlebond;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class BondMain extends JavaPlugin implements Listener {

	public HashMap<String, Color> bonds = new HashMap<String, Color>();
	public HashMap<String, Color> requests = new HashMap<String, Color>();
	public static final String NAME = ChatColor.GOLD + "[" + ChatColor.AQUA + "Particle" + ChatColor.LIGHT_PURPLE
			+ "Bonds" + ChatColor.GOLD + "]";

	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				for (String string : bonds.keySet()) {
					String[] list = string.split(",");
					Player player1 = Bukkit.getPlayer(list[0]);
					Player player2 = Bukkit.getPlayer(list[1]);
					if (!player1.isOnline() || !player2.isOnline()) {
						bonds.remove(string);
					} else if (!player1.getWorld().equals(player2.getWorld())) {
						bonds.remove(string);
						player1.sendMessage(
								NAME + ChatColor.RED + " You went too far apart, the bond has been broken.");
						player2.sendMessage(
								NAME + ChatColor.RED + " You went too far apart, the bond has been broken.");
						bonds.remove(string);
					} else if (player1.getLocation().distance(player2.getLocation()) > 40) {
						player1.sendMessage(
								NAME + ChatColor.RED + " You went too far apart, the bond has been broken.");
						player2.sendMessage(
								NAME + ChatColor.RED + " You went too far apart, the bond has been broken.");
						bonds.remove(string);
					} else {
						lineColor(player1.getLocation().clone().add(0, 0.8, 0), player2.getLocation().clone().add(0, 0.8, 0), bonds.get(string), 0);
					}
				}
			}
		}, 1L, 2L);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("bond")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "No Console");
				return false;
			}
			Player player = (Player) sender;
			if (args.length > 2 || args.length == 0) {
				player.sendMessage(
						NAME + ChatColor.RED + " Commands: \n/bond request <Player> \n/bond remove \n/bond accept");
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("request")) {
					if (player.hasPermission("particlebonds.request")) {
						player.sendMessage(NAME + ChatColor.RED + " Usage: /bond request <Player>");
					} else {
						player.sendMessage(NAME + ChatColor.RED + " No Permission!");
					}
				} else if (args[0].equalsIgnoreCase("accept")) {
					if (!player.hasPermission("particlebonds.accept")) {
						player.sendMessage(NAME + ChatColor.RED + " No Permission!");
						return false;
					}
					int i = 0;
					String remove = "";
					for (String string : requests.keySet()) {
						if (string.endsWith("," + player.getName()) && i == 0) {
							remove = string;
							bonds.put(string, requests.get(string));
							i++;
						}
					}
					if (i == 0) {
						player.sendMessage(NAME + ChatColor.RED + " You have no requests currently!");
					} else {
						requests.remove(remove);
						player.sendMessage(NAME + ChatColor.GREEN + " You have summoned a particle bond with "
								+ remove.replace("," + player.getName(), ""));
					}
				} else if (args[0].equalsIgnoreCase("remove")) {
					boolean broken = false;
					for (String string : bonds.keySet()) {
						String[] list = string.split(",");
						if (string.contains(player.getName())) {
							bonds.remove(string);
							for (String name : list) {
								Bukkit.getPlayer(name).sendMessage(NAME + ChatColor.RED + " The bond has been freely broken.");
							}
							broken = true;
						}
					}
					if (!broken) {
						player.sendMessage(NAME + ChatColor.RED + " You don't have a bond currently!");
					}
				} else {

				}
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("request")) {
					if (!player.hasPermission("particlebonds.request")) {
						player.sendMessage(NAME + ChatColor.RED + " No Permission!");
						return false;
					}
					if (args[1].equalsIgnoreCase(player.getName())) {
						player.sendMessage(NAME + ChatColor.RED + " You can't send a request to yourself!");
						return false;
					}
					for (Player cycle : Bukkit.getOnlinePlayers()) {
						if (cycle.getName().equalsIgnoreCase(args[1])) {
							player.openInventory(getMainMenu(cycle.getName()));
							return false;
						}
					}
					player.sendMessage(NAME + ChatColor.RED + " " + args[1] + " is not online!");
				} else if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("remove")) {
					player.sendMessage(NAME + ChatColor.RED + " Too many args!");
				} else {
					player.sendMessage(
							NAME + ChatColor.RED + " Commands: /bond request <Player> OR /bond remove OR /bond accept");
				}
			}
		}
		return false;
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		removeBond(event.getPlayer());
	}
	
	public void removeBond(Player player) {
		for (String string : bonds.keySet()) {
			String[] list = string.split(",");
			if (string.contains(player.getName())) {
				bonds.remove(string);
				for (String name : list) {
					if (Bukkit.getPlayer(name).isOnline()) {
						Bukkit.getPlayer(name).sendMessage(NAME + ChatColor.RED + " The bond has been broken.");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		Inventory inv = event.getInventory();
		ItemStack item = event.getCurrentItem();
		final Player player = (Player) event.getWhoClicked();
		if (inv == null) {
			return;
		}
		if (item == null) {
			return;
		}
		if (inv.getName().startsWith(ChatColor.LIGHT_PURPLE + "Particle Bond> ")) {
			event.setCancelled(true);
			player.closeInventory();
			final String name = inv.getName().replace(ChatColor.LIGHT_PURPLE + "Particle Bond> ", "");
			DyeColor dyeColor = DyeColor.getByData((byte) item.getDurability());
			Color color = dyeColor.getColor();
			if (Bukkit.getPlayer(name).isOnline()) {
				requests.put(player.getName() + "," + name, color);
				player.sendMessage(NAME + ChatColor.GREEN + " Sent request to " + name + ", times out after 30 seconds.");
				removeBond(player);
				Bukkit.getPlayer(name)
						.sendMessage(NAME + ChatColor.GREEN + " You got a request from " + player.getName()
								+ " to have a particle bond. Do " + ChatColor.DARK_PURPLE + "/bond accept "
								+ ChatColor.GREEN + "to accept.");
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					public void run() {
						if (requests.containsKey(player.getName() + "," + name)) {
							player.sendMessage(NAME + ChatColor.RED + " Request timed out, the other player didn't accept.");
							requests.remove(player.getName() + "," + name); 
						}
 					}
				}, 20*30L);
			} else {
				player.sendMessage(NAME + ChatColor.RED + " " + name + " is not online!");
			}
		}
	}

	public Inventory getMainMenu(String string) {
		Inventory inv = Bukkit.createInventory(null, 18, ChatColor.LIGHT_PURPLE + "Particle Bond> " + string);

		for (DyeColor color : DyeColor.values()) {
			String itemName = color.toString().substring(0, 1).toUpperCase()
					+ color.toString().substring(1, color.toString().length()).toLowerCase();
			ItemStack wool = rename(new ItemStack(Material.WOOL, 1, color.getData()), itemName.replace("_", " "));
			inv.addItem(wool);
		}

		return inv;
	}

	public ItemStack rename(ItemStack item, String name) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}

	public void lineColor(Location loc1, Location loc2, Color color, int speed) {
		while (loc1.distance(loc2) > 0.5) {
			Vector direction = loc2.toVector().subtract(loc1.toVector()).normalize();
			loc1.add(direction.multiply(0.5));
			ParticleEffect.REDSTONE.display(
					new ParticleEffect.OrdinaryColor(color.getRed(), color.getGreen(), color.getBlue()), loc1, 'c');
		}
	}
}
