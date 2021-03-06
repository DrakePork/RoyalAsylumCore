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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TraitorRun implements CommandExecutor {
	private Core plugin;

	@Inject
	public TraitorRun(Core plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
		File f = new File(plugin.getDataFolder() + File.separator
				+ "traitors.yml");
		FileConfiguration config = plugin.getConfig();
		FileConfiguration traitors = YamlConfiguration.loadConfiguration(f);
		Set<String> traitorList = traitors.getKeys(false);
		Long delay = (config.getLong("cooldowns.traitor-grace-period")*60)*20;
		if (args.length < 1) {
		} else if (args[0].equalsIgnoreCase("add")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
				String pUUID = player.getUniqueId().toString();
				Long playtime = user.getTotalPlayTime();
				if (!traitorList.contains(pUUID) || !sender.hasPermission("group.bandit")) {
					traitors.set(pUUID + ".playtime", playtime);
					traitors.set(pUUID + ".life", config.getInt("default-lives"));
					traitors.set(pUUID + ".startdate" , System.currentTimeMillis());
					int page = 0;
					for (int i = 0; i < traitorList.size(); ) {
						ArrayList arr = new ArrayList();
						for (String traitorPlayer : traitorList) {
							if (traitors.getInt(traitorPlayer + ".page") == i) {
								arr.add(traitorPlayer);
							}
						}
						if (arr.size() <= 44) {
							page = i;
							break;
						} else {
							i++;
							continue;
						}
					}
					traitors.set(pUUID + ".page", page);
					try {
						traitors.save(f);
						for (Player online : Bukkit.getOnlinePlayers()) {
							online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
									+ ChatColor.DARK_RED + user.getName() + ChatColor.GRAY + " has betrayed the Kingdom and escaped to"
									+ ChatColor.DARK_GRAY + " The Badlands" + ChatColor.GRAY + "!" + ChatColor.YELLOW + " Farmers+, "
									+ ChatColor.GRAY + "hunt them down for " + ChatColor.AQUA + "100k Bounty and Special Tag!");
						}
						traitors = YamlConfiguration.loadConfiguration(f);
						traitorList = traitors.getKeys(false);
						if(traitorList.size() < 2) {
							String portalOpen = plugin.colourMessage(langConf.getString("traitor.portal-open-timer")
									.replaceAll("\\[m\\]", String.valueOf(config.getLong("cooldowns.traitor-grace-period"))));
							for (Player online : Bukkit.getOnlinePlayers()) {
								online.sendMessage(portalOpen);
							}
							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
								public void run() {
									if (CMI.getInstance().getPortalManager().getByName("BHPortal") != null || CMI.getInstance().getPortalManager().getByName("NHPortal") != null) {
										String portalOpened = plugin.colourMessage(langConf.getString("traitor.portal-open"));
										for (Player online : Bukkit.getOnlinePlayers()) {
											online.sendMessage(portalOpened);
										}
										CMI.getInstance().getPortalManager().getByName("BHPortal").setEnabled(true);
										CMI.getInstance().getPortalManager().getByName("NHPortal").setEnabled(true);
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi effect " + player.getName() + " " + config.getString("traitor-effect.type") + " " + config.getInt("traitor-effect.duration") + config.getInt("traitor-effect.amplifier") +  " -s");
									} else {
										for (Player online : Bukkit.getOnlinePlayers()) {
											online.sendMessage(ChatColor.RED + "Looks like ImuRgency has cucked something up with the badlands portals! Report to an admin.");
										}
									}
								}
							}, delay);
						} else {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi effect " + player.getName() + " " + config.getString("traitor-effect.type") + " " + config.getInt("traitor-effect.duration") + config.getInt("traitor-effect.amplifier") +  " -s");
						}
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent add traitor");
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi effect " + player.getName() + " invisibility " + TimeUnit.MINUTES.toSeconds(config.getInt("cooldowns.traitor-grace-period")) + " 1 -s");
							}
						}, 100);
					} catch (IOException e) {
						player.sendMessage(ChatColor.RED + "Something went wrong! Contact an admin!");
						e.printStackTrace();
					}
				} else {
					player.sendMessage(plugin.colourMessage(langConf.getString("traitor.already-bandit")));
				}
			} else {
				if (args.length < 2) {
					sender.sendMessage(ChatColor.RED + "/traitor add <player>");
				} else {
					Player player = Bukkit.getPlayer(args[1]);
					CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
					String pUUID = player.getUniqueId().toString();
					Long playtime = user.getTotalPlayTime();
					if (!traitorList.contains(pUUID) || !sender.hasPermission("group.bandit")) {
						traitors.set(pUUID + ".playtime", playtime);
						traitors.set(pUUID + ".life", config.getInt("default-lives"));
						traitors.set(pUUID + ".startdate" , System.currentTimeMillis());
						int page = 0;
						for (int i = 0; i < traitorList.size(); ) {
							ArrayList arr = new ArrayList();
							for (String traitorPlayer : traitorList) {
								if (traitors.getInt(traitorPlayer + ".page") == i) {
									arr.add(traitorPlayer);
								}
							}
							if (arr.size() <= 44) {
								page = i;
								break;
							} else {
								i++;
								continue;
							}
						}
						traitors.set(pUUID + ".page", page);
						try {
							traitors.save(f);
							for (Player online : Bukkit.getOnlinePlayers()) {
								online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
										+ ChatColor.DARK_RED + user.getName() + ChatColor.GRAY + " has betrayed the Kingdom and escaped to"
										+ ChatColor.DARK_GRAY + " The Badlands" + ChatColor.GRAY + "!" + ChatColor.YELLOW + " Farmers+, "
										+ ChatColor.GRAY + "hunt them down for " + ChatColor.AQUA + "100k Bounty and Special Tag!");
							}
							traitors = YamlConfiguration.loadConfiguration(f);
							traitorList = traitors.getKeys(false);
							if(traitorList.size() < 2) {
								for (Player online : Bukkit.getOnlinePlayers()) {
									online.sendMessage("");
									online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
											+ ChatColor.AQUA + "The Noble and Bandit Badlands Portals are opening in " + config.getLong("cooldowns.traitor-grace-period") + " minutes...");
								}
								plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
									public void run() {
										if (CMI.getInstance().getPortalManager().getByName("BHPortal") != null || CMI.getInstance().getPortalManager().getByName("NHPortal") != null) {
											for (Player online : Bukkit.getOnlinePlayers()) {
												online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
														+ ChatColor.AQUA + "The Noble and Bandit Badlands Portals have " + ChatColor.GREEN + "OPENED");
											}
											CMI.getInstance().getPortalManager().getByName("BHPortal").setEnabled(true);
											CMI.getInstance().getPortalManager().getByName("NHPortal").setEnabled(true);
											Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi effect " + player.getName() + " " + config.getString("traitor-effect.type") + " " + config.getInt("traitor-effect.duration") + config.getInt("traitor-effect.amplifier") +  " -s");
										} else {
											for (Player online : Bukkit.getOnlinePlayers()) {
												online.sendMessage(ChatColor.RED + "Looks like ImuRgency has cucked something up with the badlands portals! Report to an admin.");
											}
										}
									}
								}, delay);
							} else {
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi effect " + player.getName() + " " + config.getString("traitor-effect.type") + " " + config.getInt("traitor-effect.duration") + config.getInt("traitor-effect.amplifier") +  " -s");
							}
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent add traitor");
							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
								public void run() {
									Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi effect " + player.getName() + " invisibility " + TimeUnit.MINUTES.toSeconds(config.getInt("cooldowns.traitor-grace-period")) + " 1 -s");
								}
							}, 100);
						} catch (IOException e) {
							player.sendMessage(ChatColor.RED + "Something went wrong! Contact an admin!");
							e.printStackTrace();
						}
					} else {
						sender.sendMessage(ChatColor.RED + "This player is already a bandit!");
					}
				}
			}
		} else if (args[0].equalsIgnoreCase("admincheck")) {
			if (traitorList != null && !traitorList.isEmpty()) {
				final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
				for (Player OnlinePlayer : onlinePlayers) {
					String onlineUUID = OnlinePlayer.getUniqueId().toString();
					if (traitorList.contains(onlineUUID)) {
						CMIUser user = CMI.getInstance().getPlayerManager().getUser(OnlinePlayer);
						Long iniPlaytime = traitors.getLong(onlineUUID + ".playtime");
						Long currPlaytime = user.getTotalPlayTime();
						Long remPlaytime = currPlaytime - iniPlaytime;
						Long neededPlaytime = TimeUnit.MINUTES.toMillis(config.getLong("cooldowns.traitor-time-required"));
						if (remPlaytime >= neededPlaytime) {
							traitors.set(onlineUUID, null);
							try {
								traitors.save(f);
								for (Player online : Bukkit.getOnlinePlayers()) {
									online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
											+ ChatColor.AQUA + user.getName() + ChatColor.GRAY + " has " + ChatColor.RED + ChatColor.BOLD + "ESCAPED"
											+ ChatColor.GRAY + " the kingdom and become a bandit!");
								}
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi warp traitorfinish " + user.getName());
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + user.getName() + " parent add outlaw");
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + user.getName() + " parent add noble");
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + user.getName() + " parent remove traitor");
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + user.getName() + " permission settemp banitem.bypass.Traitor.ender_chest.interact.* true 5m");
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + user.getName() + " permission settemp betterenderchest.user.open.privatechest true 5m");
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi effect " + OnlinePlayer.getName() + " " + config.getString("traitor-effect.type") + " " + config.getInt("traitor-effect.duration") + config.getInt("traitor-effect.amplifier") +  " -s");
						}
					}
				}
			}
		} else if(args[0].equalsIgnoreCase("help")) {
			sender.sendMessage("/traitor help\n/traitor admincheck\n/traitor add (player)\n/traitorcheck (player)\n/traitortrack\n/traitor givelife <player> <amount>");
		} else if(args[0].equalsIgnoreCase("givelife")) {
			if(args.length < 3) {
				sender.sendMessage(ChatColor.RED + "/traitor givelife <traitor name> <lives>");
			} else {
				if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
					Player player = Bukkit.getPlayer(args[1]);
					if(traitorList.contains(player.getUniqueId().toString())) {
						int newLives = Integer.valueOf(args[2]);
						int currLives = traitors.getInt(player.getUniqueId().toString() + ".life");
						traitors.set(player.getUniqueId().toString() + ".life", currLives + newLives);
						try {
							traitors.save(f);
							sender.sendMessage("Successfully gave " + player.getName() + " " + args[2] + " more lives!");
						} catch (IOException e) {
							e.printStackTrace();
						}

					} else {
						sender.sendMessage(ChatColor.RED + "This player isnt a traitor!");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "This player hasnt been on the server before!");
				}
				Player player = Bukkit.getPlayer(args[1]);

			}
		}
		return true;
	}
}
