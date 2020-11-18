package com.github.drakepork.royalasylumcore.Listeners;

import com.github.drakepork.royalasylumcore.Core;
import com.google.inject.Inject;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class Discord {
	private Core plugin;

	@Inject
	public Discord(Core plugin) {
		this.plugin = plugin;
	}

	public void tellConsole(String message){
		Bukkit.getConsoleSender().sendMessage(message);
	}

	public String colourMessage(String message){
		message = plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message));
		return message;
	}

	public void commandRun(String msg, String perm, String channelName, String langFormat, String UserName) {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);

		String message = ChatColor.stripColor(String.join(" ", msg));
		String format = langConf.getString(langFormat).replaceAll("\\[name\\]", ChatColor.stripColor(UserName));
		message = format.replaceAll("\\[message\\]", message);
		for (Player online : Bukkit.getServer().getOnlinePlayers()) {
			if (online.hasPermission(perm)) {
				online.sendMessage(colourMessage(message));
			}
		}
		tellConsole(colourMessage(message));

		String dFormat = langConf.getString("chat.discordSRV.format").replaceAll("\\[name\\]", ChatColor.stripColor(UserName));
		String dMessage = dFormat.replaceAll("\\[message\\]", ChatColor.stripColor(String.join(" ", msg)));
		TextChannel channel = github.scarsz.discordsrv.DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channelName);
		channel.sendMessage(dMessage).queue();
	}

	@Subscribe
	public void discordMessageProcessed(DiscordGuildMessagePreProcessEvent event) {
		switch(event.getChannel().getName().toLowerCase()) {
			case "admin-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getMessage().getContentRaw(),
								"royalasylum.chat.admin",
								"admin-chat",
								"chat.admin.format",
								event.getAuthor().getName());
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;
			case "build-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getMessage().getContentRaw(),
								"royalasylum.chat.build",
								"build-chat",
								"chat.build.format",
								event.getAuthor().getName());
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;
			case "guard-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getMessage().getContentRaw(),
								"royalasylum.chat.guard",
								"guard-chat",
								"chat.guard.format",
								event.getAuthor().getName());
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;
			case "hunter-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getMessage().getContentRaw(),
								"royalasylum.chat.hunter",
								"hunter-chat",
								"chat.hunter.format",
								event.getAuthor().getName());
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;
			case "roundtable-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getMessage().getContentRaw(),
								"royalasylum.chat.roundtable",
								"roundtable-chat",
								"chat.roundtable.format",
								event.getAuthor().getName());
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;
			case "report-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getMessage().getContentRaw(),
								"royalasylum.chat.report.view",
								"report-chat",
								"chat.report.format",
								event.getAuthor().getName());
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;
			case "discord-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getMessage().getContentRaw(),
								"royalasylum.chat.discord",
								"discord-chat",
								"chat.discord.format",
								event.getAuthor().getName());
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;
			case "traitor-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getMessage().getContentRaw(),
								"royalasylum.chat.traitor",
								"traitor-chat",
								"chat.traitor.format",
								event.getAuthor().getName());
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;

		}
	}
}
