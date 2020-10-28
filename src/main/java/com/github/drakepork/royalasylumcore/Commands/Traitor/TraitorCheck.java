package com.github.drakepork.royalasylumcore.Commands.Traitor;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.github.drakepork.royalasylumcore.Core;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TraitorCheck implements CommandExecutor {
	private Core plugin;

	@Inject
	public TraitorCheck(Core plugin) {
		this.plugin = plugin;
	}

	public void tellConsole(String message){
		Bukkit.getConsoleSender().sendMessage(message);
	}

	public String ColourMessage(String message){
		message = plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message));
		return message;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		File f = new File(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
				.getDataFolder() + "/traitors.yml");
		FileConfiguration traitors = YamlConfiguration.loadConfiguration(f);
		Set<String> traitorList = traitors.getKeys(false);
		File conf = new File(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
				.getDataFolder() + "/config.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(conf);
		if(sender instanceof Player) {
			if (args.length < 1) {
				Player player = (Player) sender;
				if (traitorList.contains(player.getUniqueId().toString())) {
					CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
					Long iniPlaytime = traitors.getLong(player.getUniqueId().toString() + ".playtime");
					int lives = traitors.getInt(player.getUniqueId().toString() + ".life");
					Long currPlaytime = user.getTotalPlayTime();
					Long remPlaytime = currPlaytime - iniPlaytime;
					long timeRem = (TimeUnit.MINUTES.toMillis(config.getLong("cooldowns.traitor-time-required")) - remPlaytime) / 1000;
					int hours = (int) timeRem / 3600;
					int remainder = (int) timeRem - hours * 3600;
					int mins = remainder / 60;
					remainder = remainder - mins * 60;
					int secs = remainder;
					sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
							+ ChatColor.GRAY + "You have "
							+ ChatColor.YELLOW + hours + "h " + mins + "m " + secs + "s " + ChatColor.GRAY + "left until you become a bandit!");
					sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
							+ ChatColor.GRAY + "You have " + ChatColor.GREEN + lives + ChatColor.GRAY + " lives left!");
				} else {
					sender.sendMessage(ChatColor.RED + "You are not a traitor!");
				}
			} else {
				if(CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
					CMIUser player  = CMI.getInstance().getPlayerManager().getUser(args[0]);
					if (traitorList.contains(player.getUniqueId().toString())) {
						Long iniPlaytime = traitors.getLong(player.getUniqueId().toString() + ".playtime");
						int lives = traitors.getInt(player.getUniqueId().toString() + ".life");
						Long currPlaytime = player.getTotalPlayTime();
						Long remPlaytime = currPlaytime - iniPlaytime;
						long timeRem = (TimeUnit.MINUTES.toMillis(config.getLong("cooldowns.traitor-time-required")) - remPlaytime) / 1000;
						int hours = (int) timeRem / 3600;
						int remainder = (int) timeRem - hours * 3600;
						int mins = remainder / 60;
						remainder = remainder - mins * 60;
						int secs = remainder;
						sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
								+ ChatColor.RED + player.getName() + ChatColor.GRAY + " has "
								+ ChatColor.YELLOW + hours + "h " + mins + "m " + secs + "s " + ChatColor.GRAY + "left until they become a bandit!");
						sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
								+ ChatColor.RED + player.getName() + ChatColor.GRAY + " has " + ChatColor.GREEN + lives + ChatColor.GRAY + " lives left!");
					} else {
						sender.sendMessage(ChatColor.RED + "This player isn't a traitor!");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "This player hasnt been on the server before!");
				}
			}
		} else {
			if (args.length < 1) {
				sender.sendMessage(ChatColor.RED + "/traitorcheck <player>");
			} else {
				if(CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
					CMIUser player = CMI.getInstance().getPlayerManager().getUser(args[0]);
					if (traitorList.contains(player.getUniqueId().toString())) {
						Long iniPlaytime = traitors.getLong(player.getUniqueId().toString() + ".playtime");
						int lives = traitors.getInt(player.getUniqueId().toString() + ".life");
						Long currPlaytime = player.getTotalPlayTime();
						Long remPlaytime = currPlaytime - iniPlaytime;
						long timeRem = (TimeUnit.MINUTES.toMillis(config.getLong("traitor-time-required")) - remPlaytime) / 1000;
						int hours = (int) timeRem / 3600;
						int remainder = (int) timeRem - hours * 3600;
						int mins = remainder / 60;
						remainder = remainder - mins * 60;
						int secs = remainder;
						sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
								+ ChatColor.RED + player.getName() + ChatColor.GRAY + " has "
								+ ChatColor.YELLOW + hours + "h " + mins + "m " + secs + "s " + ChatColor.GRAY + "left until they become a bandit!");
						sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
								+ ChatColor.RED + player.getName() + ChatColor.GRAY + " has " + ChatColor.GREEN + lives + ChatColor.GRAY + " lives left!");
					} else {
						sender.sendMessage(ChatColor.RED + "This player isn't a traitor!");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "This player hasnt been on the server before!");
				}
			}
		}
		return true;
	}
}
