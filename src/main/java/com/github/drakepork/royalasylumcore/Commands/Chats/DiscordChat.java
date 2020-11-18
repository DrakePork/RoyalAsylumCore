package com.github.drakepork.royalasylumcore.Commands.Chats;

import com.github.drakepork.royalasylumcore.Core;
import com.google.inject.Inject;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.regex.Matcher;

public class DiscordChat implements CommandExecutor {
	private Core plugin;

	@Inject
	public DiscordChat(Core plugin) {
		this.plugin = plugin;
	}

	public void tellConsole(String message){
		Bukkit.getConsoleSender().sendMessage(message);
	}

	public String colourMessage(String message){
		message = plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message));
		return message;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
		String prefix = langConf.getString("global.plugin-prefix");

		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(args.length > 0) {
				String cMessage = "";
				for (int i = 0; i < args.length; i++) {
					cMessage = cMessage + args[i] + " ";
				}
				String format = langConf.getString("chat.discord.format").replaceAll("\\[name\\]", Matcher.quoteReplacement(player.getName()));
				String message = format.replaceAll("\\[message\\]", Matcher.quoteReplacement(cMessage));
				for (Player online : Bukkit.getServer().getOnlinePlayers()) {
					if (online.hasPermission("royalasylum.chat.discord")) {
						online.sendMessage(colourMessage(message));
					}
				}
				tellConsole(colourMessage(message));

				String dFormat = langConf.getString("chat.discordSRV.format").replaceAll("\\[name\\]", Matcher.quoteReplacement(player.getName()));
				String dMessage = dFormat.replaceAll("\\[message\\]", Matcher.quoteReplacement(cMessage));
				TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("discord-chat");
				channel.sendMessage(dMessage).queue();
			} else {
				if(plugin.stickyChatEnabled.containsKey(player.getUniqueId())) {
					if (plugin.stickyChatEnabled.get(player.getUniqueId()).equals("discord-chat")) {
						String stickEnabled = langConf.getString("chat.stickied.disabled").replaceAll("\\[chat\\]", "Discord");
						player.sendMessage(colourMessage(prefix + stickEnabled));
						plugin.stickyChatEnabled.remove(player.getUniqueId());
					} else {
						String[] oldSticky = plugin.stickyChatEnabled.get(player.getUniqueId()).split("-");
						String oldChat = oldSticky[0].substring(0, 1).toUpperCase() + oldSticky[0].substring(1);
						String stickSwapped = langConf.getString("chat.stickied.swapped").replaceAll("\\[oldchat\\]", oldChat);
						stickSwapped = stickSwapped.replaceAll("\\[newchat\\]", "Discord");
						player.sendMessage(colourMessage(prefix + stickSwapped));
						plugin.stickyChatEnabled.put(player.getUniqueId(), "discord-chat");
					}
				} else {
					String stickEnabled = langConf.getString("chat.stickied.enabled").replaceAll("\\[chat\\]", "Discord");
					player.sendMessage(colourMessage(prefix + stickEnabled));
					plugin.stickyChatEnabled.put(player.getUniqueId(), "discord-chat");
				}
			}
		} else {
			if(args.length > 0) {
				String cMessage = "";
				for (int i = 0; i < args.length; i++) {
					cMessage = cMessage + args[i] + " ";
				}
				String format = langConf.getString("chat.discord.format").replaceAll("\\[name\\]", "Console");
				String message = format.replaceAll("\\[message\\]", Matcher.quoteReplacement(cMessage));
				for (Player online : Bukkit.getServer().getOnlinePlayers()) {
					if (online.hasPermission("royalasylum.chat.discord")) {
						online.sendMessage(colourMessage(message));
					}
				}
				tellConsole(colourMessage(message));

				String dFormat = langConf.getString("chat.discordSRV.format").replaceAll("\\[name\\]", "Console");
				String dMessage = dFormat.replaceAll("\\[message\\]", Matcher.quoteReplacement(cMessage));
				TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("discord-chat");
				channel.sendMessage(dMessage).queue();
			} else {
				tellConsole(colourMessage(prefix + langConf.getString("chat.discord.wrong-usage")));
			}
		}
		return true;
	}
}
