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

public class TraitorChat implements CommandExecutor {
	private Core plugin;

	@Inject
	public TraitorChat(Core plugin) {
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
				String format = langConf.getString("chat.traitor.format").replaceAll("\\[name\\]", player.getName());
				String message = format.replaceAll("\\[message\\]", cMessage);
				for (Player online : Bukkit.getServer().getOnlinePlayers()) {
					if (online.hasPermission("royalasylum.chat.traitor")) {
						online.sendMessage(colourMessage(message));
					}
				}
				tellConsole(colourMessage(message));

				String dFormat = langConf.getString("chat.discordSRV.format").replaceAll("\\[name\\]", player.getName());
				String dMessage = dFormat.replaceAll("\\[message\\]", cMessage);
				TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("traitor-chat");
				channel.sendMessage(dMessage).queue();
			} else {
				player.sendMessage(colourMessage(prefix + langConf.getString("chat.traitor.wrong-usage")));
			}
		} else {
			if(args.length > 0) {
				String cMessage = "";
				for (int i = 0; i < args.length; i++) {
					cMessage = cMessage + args[i] + " ";
				}
				String format = langConf.getString("chat.traitor.format").replaceAll("\\[name\\]", "Console");
				String message = format.replaceAll("\\[message\\]", cMessage);
				for (Player online : Bukkit.getServer().getOnlinePlayers()) {
					if (online.hasPermission("royalasylum.chat.traitor")) {
						online.sendMessage(colourMessage(message));
					}
				}
				tellConsole(colourMessage(message));

				String dFormat = langConf.getString("chat.discordSRV.format").replaceAll("\\[name\\]", "Console");
				String dMessage = dFormat.replaceAll("\\[message\\]", cMessage);
				TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("traitor-chat");
				channel.sendMessage(dMessage).queue();
			} else {
				tellConsole(colourMessage(prefix + langConf.getString("chat.traitor.wrong-usage")));
			}
		}
		return true;
	}
}
