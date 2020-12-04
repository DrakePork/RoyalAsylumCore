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
import java.util.regex.Matcher;

public class TraitorCheck implements CommandExecutor {
	private Core plugin;

	@Inject
	public TraitorCheck(Core plugin) {
		this.plugin = plugin;
	}

	public void playerTraitorCheck(Set<String> traitorList, String[] args, FileConfiguration traitors, CommandSender sender, FileConfiguration langConf) {
		FileConfiguration config = plugin.getConfig();
		if(CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
			CMIUser player = CMI.getInstance().getPlayerManager().getUser(args[0]);
			if (traitorList.contains(player.getUniqueId().toString())) {
				Long iniPlaytime = traitors.getLong(player.getUniqueId().toString() + ".playtime");
				Long currPlaytime = player.getTotalPlayTime();
				Long remPlaytime = currPlaytime - iniPlaytime;
				long timeRem = (TimeUnit.MINUTES.toMillis(config.getLong("traitor-time-required")) - remPlaytime) / 1000;
				int hours = (int) timeRem / 3600;
				int remainder = (int) timeRem - hours * 3600;
				int mins = remainder / 60;
				remainder = remainder - mins * 60;
				int secs = remainder;
				int lives = traitors.getInt(player.getUniqueId().toString() + ".life");
				String playTimeString = langConf.getString("traitor.traitorcheck.playtime-left").replaceAll("\\[player\\]", player.getName());
				playTimeString = playTimeString.replaceAll("\\[h\\]", String.valueOf(hours));
				playTimeString = playTimeString.replaceAll("\\[m\\]", String.valueOf(mins));
				playTimeString = playTimeString.replaceAll("\\[s\\]", String.valueOf(secs));
				String livesString = langConf.getString("traitor.traitorcheck.lives-left").replaceAll("\\[message\\]", String.valueOf(lives));
				sender.sendMessage(plugin.colourMessage(playTimeString + "\n" + livesString));
			} else {
				sender.sendMessage(plugin.colourMessage(langConf.getString("traitor.not-a-traitor")));
			}
		} else {
			sender.sendMessage(plugin.colourMessage(langConf.getString("traitor.no-such-player")));
		}
	}


	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
		File f = new File(plugin.getDataFolder() + File.separator
				+ "traitors.yml");
		FileConfiguration traitors = YamlConfiguration.loadConfiguration(f);
		Set<String> traitorList = traitors.getKeys(false);
		FileConfiguration config = plugin.getConfig();
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
				playerTraitorCheck(traitorList, args, traitors, sender, langConf);
			}
		} else {
			if (args.length > 0) {
				playerTraitorCheck(traitorList, args, traitors, sender, langConf);
			} else {
				sender.sendMessage(plugin.colourMessage(langConf.getString("traitor.traitorcheck.wrong-usage")));
			}
		}
		return true;
	}
}
