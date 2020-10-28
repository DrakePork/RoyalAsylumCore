package com.github.drakepork.royalasylumcore.Commands.Traitor;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.github.drakepork.royalasylumcore.Core;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TraitorTrack implements CommandExecutor {
	private Core plugin;

	@Inject
	public TraitorTrack(Core plugin) {
		this.plugin = plugin;
	}

	public void tellConsole(String message){
		Bukkit.getConsoleSender().sendMessage(message);
	}

	public String ColourMessage(String message){
		message = plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message));
		return message;
	}

	public void chooseType (Player player, Player traitor) {
		File conf = new File(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
				.getDataFolder() + "/config.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(conf);
		Inventory chooseTrack = Bukkit.createInventory(null, 27, ChatColor.RED + "Tracking: " + traitor.getName());

		ArrayList lore1 = new ArrayList();
		ItemStack global = new ItemStack(Material.CLOCK);
		ItemMeta meta1 = global.getItemMeta();
		meta1.setDisplayName(ChatColor.YELLOW + "Global Tracking");
		lore1.add(ChatColor.GRAY + "Globally track this traitor");
		lore1.add(ChatColor.GRAY + "Cost:");
		lore1.add(ChatColor.GRAY + "- " + ChatColor.RED + "$"+ config.getInt("track-cost.global-money-cost") + ChatColor.GRAY + " (Left Click)");
		lore1.add(ChatColor.GRAY + "- " + ChatColor.RED + config.getInt("track-cost.global-token-cost") + " Tokens" + ChatColor.GRAY + " (Right Click)");
		meta1.setLore(lore1);
		global.setItemMeta(meta1);
		chooseTrack.setItem(11, global);

		ArrayList lore2 = new ArrayList();
		ItemStack personal = new ItemStack(Material.PAPER);
		ItemMeta meta2 = personal.getItemMeta();
		meta2.setDisplayName(ChatColor.YELLOW + "Personal Tracking");
		lore2.add(ChatColor.GRAY + "Personally track this traitor");
		lore2.add(ChatColor.GRAY + "Cost: " + ChatColor.RED + config.getInt("track-cost.personal-token-cost") + " Tokens");
		meta2.setLore(lore2);
		personal.setItemMeta(meta2);
		chooseTrack.setItem(15, personal);
		player.openInventory(chooseTrack);
	}

	public void openGUI(Player player, int page) {
		File f = new File(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
				.getDataFolder() + "/traitors.yml");
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File conf = new File(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
				.getDataFolder() + "/config.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(conf);
		FileConfiguration traitors = YamlConfiguration.loadConfiguration(f);
		Set<String> traitorList = traitors.getKeys(false);
		ArrayList<String> arr = new ArrayList();
		ArrayList totalPages = new ArrayList();
		for(String traitorPlayer : traitorList) {
			if(traitors.getInt(traitorPlayer + ".page") == page) {
				arr.add(traitorPlayer);
			}
			totalPages.add(traitors.getInt(traitorPlayer + ".page"));
		}
		Inventory traitorGUI = Bukkit.createInventory(null, 54, ChatColor.RED + "Traitors | Page " + page);
		int i = 0;
		for (String traitorPlayer : arr) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(traitorPlayer);
			Long iniPlaytime = traitors.getLong(traitorPlayer + ".playtime");
			Long currPlaytime = user.getTotalPlayTime();
			Long remPlaytime = currPlaytime - iniPlaytime;
			long timeRem = (TimeUnit.MINUTES.toMillis(config.getLong("cooldowns.traitor-time-required")) - remPlaytime) / 1000;
			int hours = (int) timeRem / 3600;
			int remainder = (int) timeRem - hours * 3600;
			int mins = remainder / 60;
			remainder = remainder - mins * 60;
			int secs = remainder;
			ArrayList lore = new ArrayList();
			ItemStack head = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta) head.getItemMeta();
			meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(traitorPlayer)));
			meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + Bukkit.getOfflinePlayer(UUID.fromString(traitorPlayer)).getName());
			lore.add(ChatColor.GRAY + "Time Left: " + ChatColor.YELLOW + hours + "h " + mins + "m " + secs + "s");
			lore.add(ChatColor.GRAY + "Lives left: " + ChatColor.GREEN + traitors.getInt(traitorPlayer + ".life"));
			lore.add(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "Click to track location!");
			meta.setLore(lore);
			head.setItemMeta(meta);
			traitorGUI.setItem(i, head);
			i++;
		}
		ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemStack pageChange = new ItemStack(Material.PAPER);
		ItemMeta itemMeta = pageChange.getItemMeta();
		itemMeta.setDisplayName(ChatColor.GREEN + "Next Page");
		pageChange.setItemMeta(itemMeta);
		for (int b = 45; b < 54; b++) {
			if (page == 0) {
				if(totalPages.size() < 1) {
					traitorGUI.setItem(b, pane);
				} else {
					if (Collections.max(totalPages).equals(page)) {
						traitorGUI.setItem(b, pane);
					} else {
						if(b != 52) {
							traitorGUI.setItem(b, pane);
						} else {
							traitorGUI.setItem(b, pageChange);
						}
					}
				}
			} else if (Collections.max(totalPages).equals(page)) {
				if(b != 46) {
					traitorGUI.setItem(b, pane);
				} else {
					itemMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
					pageChange.setItemMeta(itemMeta);
					traitorGUI.setItem(b, pageChange);
				}
			} else {
				if(b != 46 && b != 52) {
					traitorGUI.setItem(b, pane);
				} else if(b == 46) {
					itemMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
					pageChange.setItemMeta(itemMeta);
					traitorGUI.setItem(b, pageChange);
				} else {
					traitorGUI.setItem(b, pageChange);
				}
			}
		}
		player.openInventory(traitorGUI);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			File f = new File(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
					.getDataFolder() + "/traitors.yml");
			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			Player player = (Player) sender;
			openGUI(player, 0);
		} else {
			sender.sendMessage( ChatColor.DARK_RED + "This command can only be used in game!");
		}
		return true;
	}
}
