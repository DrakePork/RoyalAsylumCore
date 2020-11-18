package com.github.drakepork.royalasylumcore.Commands;

import com.github.drakepork.royalasylumcore.Core;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class SecretFound implements CommandExecutor {
	private Core plugin;

	@Inject
	public SecretFound(Core plugin) {
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
		Player player = (Player) sender;
		File f = new File(plugin.getDataFolder() + File.separator + "secrets.yml");
		File secretsDataFile = new File(plugin.getDataFolder() + File.separator
				+ "secretsdata.yml");
		YamlConfiguration pData = YamlConfiguration.loadConfiguration(secretsDataFile);
		YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
		String pWorld = player.getWorld().getName();
		Set<String> secrets = yamlf.getKeys(true);

		for(String secretKey : secrets) {
			if(secretKey.contains("category")) {
				String[] getId = secretKey.split("[.]");
				String secretId = getId[0] + "." + getId[1] + "." + getId[2];
				if(yamlf.getString(secretId + ".id").equalsIgnoreCase(args[0])) {
					String cat = yamlf.getString(secretId + ".category");
					int amountFound = pData.getInt(player.getUniqueId().toString() + ".secrets-found." + args[0] + ".times-found");
					pData.set(player.getUniqueId().toString() + ".secrets-found." + cat + "." + args[0] + ".times-found", amountFound + 1);
					try {
						pData.save(secretsDataFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}

		return true;
	}
}
