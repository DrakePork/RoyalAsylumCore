package com.github.drakepork.royalasylumcore.Listeners;

import com.github.drakepork.royalasylumcore.Core;
import com.google.inject.Inject;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;

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

	public void commandRun(String msg, String perm, String channelName, String langFormat) {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);

		String[] fullmsg = msg.split(" ");
		ArrayList cMessage = new ArrayList();
		for(int i = 3; i < fullmsg.length; i++) {
			cMessage.add(fullmsg[i]);
		}

		String message = ChatColor.stripColor(String.join(" ", cMessage));
		String format = langConf.getString(langFormat).replaceAll("\\[name\\]", ChatColor.stripColor(fullmsg[1]));
		message = format.replaceAll("\\[message\\]", message);
		for (Player online : Bukkit.getServer().getOnlinePlayers()) {
			if (online.hasPermission(perm)) {
				online.sendMessage(colourMessage(message));
			}
		}
		tellConsole(colourMessage(message));

		String dFormat = langConf.getString("chat.discordSRV.format").replaceAll("\\[name\\]", ChatColor.stripColor(fullmsg[1]));
		String dMessage = dFormat.replaceAll("\\[message\\]", ChatColor.stripColor(String.join(" ", cMessage)));
		TextChannel channel = github.scarsz.discordsrv.DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channelName);
		channel.sendMessage(dMessage).queue();
	}

	@Subscribe
	public void discordMessageProcessed(DiscordGuildMessagePostProcessEvent event) {
		switch(event.getChannel().getName().toLowerCase()) {
			case "admin-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getProcessedMessage(),
								"royalasylum.chat.admin",
								"admin-chat",
								"chat.admin.format");
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;
			case "build-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getProcessedMessage(),
								"royalasylum.chat.build",
								"build-chat",
								"chat.build.format");
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;
			case "guard-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getProcessedMessage(),
								"royalasylum.chat.guard",
								"guard-chat",
								"chat.guard.format");
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;
			case "hunter-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getProcessedMessage(),
								"royalasylum.chat.hunter",
								"hunter-chat",
								"chat.hunter.format");
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;
			case "roundtable-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getProcessedMessage(),
								"royalasylum.chat.roundtable",
								"roundtable-chat",
								"chat.roundtable.format");
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;
			case "report-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getProcessedMessage(),
								"royalasylum.chat.report.view",
								"report-chat",
								"chat.report.format");
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;
			case "discord-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getProcessedMessage(),
								"royalasylum.chat.discord",
								"discord-chat",
								"chat.discord.format");
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;
			case "traitor-chat":
				event.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						commandRun(event.getProcessedMessage(),
								"royalasylum.chat.traitor",
								"traitor-chat",
								"chat.traitor.format");
					}
				}.runTask(plugin);
				event.getMessage().delete().queue();
				break;

		}
	}
}
