package com.github.drakepork.royalasylumcore.Commands;

import com.github.drakepork.royalasylumcore.Core;
import com.google.inject.Inject;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

public class SecretsGUI implements CommandExecutor {
	private Core plugin;

	@Inject
	public SecretsGUI(Core plugin) {
		this.plugin = plugin;
	}

	private ItemStack decorativeItemStack(Material material, int amount, String name) {
		ItemStack item = new ItemStack(material, amount);
		ItemMeta itemmeta = item.getItemMeta();
		itemmeta.setDisplayName(name);
		item.setItemMeta(itemmeta);
		return item;
	}

	private ItemStack unknownItemStack() {
		String name = ChatColor.RED + "" + ChatColor.BOLD + "???";
		ItemStack item = new ItemStack(Material.BOOK, 1);
		ItemMeta itemmeta = item.getItemMeta();
		itemmeta.setDisplayName(name);
		itemmeta.setLore(Arrays.asList(new String[]{ChatColor.GRAY + "" + ChatColor.ITALIC + "Find this secret to unlock it!"}));
		item.setItemMeta(itemmeta);
		return item;
	}

	private ItemStack secretItemStack(Material material, int amount, String name, String line3, Player player, String secretId) {
		ItemStack item = new ItemStack(material, amount);
		ItemMeta itemmeta = item.getItemMeta();

		itemmeta.setDisplayName(name);
		String line2 = ChatColor.DARK_PURPLE + "Cooldown:";
		String combinedName = name.replaceAll("\\s", "");
		String plsName = combinedName;
		String line1;
		File secretsDataFile = new File(plugin.getDataFolder() + File.separator
				+ "secretsdata.yml");
		YamlConfiguration pData = YamlConfiguration.loadConfiguration(secretsDataFile);
		int amountFound = pData.getInt(player.getUniqueId().toString() + ".secrets-found." + secretId + ".times-found");
		if (pData.get(player.getUniqueId().toString() + ".secrets-found." + secretId) != null) {
			if (plsName.toLowerCase().contains("parkour")) {
				if (amountFound == 1) {
					line1 = ChatColor.GRAY + "You've done this parkour " + ChatColor.AQUA + amountFound + ChatColor.GRAY + " time";
				} else {
					line1 = ChatColor.GRAY + "You've done this parkour " + ChatColor.AQUA + amountFound + ChatColor.GRAY + " times";
				}
			} else if (plsName.toLowerCase().contains("puzzle")) {
				if (amountFound == 1) {
					line1 = ChatColor.GRAY + "You've completed this puzzle " + ChatColor.AQUA + amountFound + ChatColor.GRAY + " time";
				} else {
					line1 = ChatColor.GRAY + "You've completed this puzzle " + ChatColor.AQUA + amountFound + ChatColor.GRAY + " times";
				}

			} else if (amountFound == 1) {
				line1 = ChatColor.GRAY + "You've found this secret " + ChatColor.AQUA + amountFound + ChatColor.GRAY + " time";
			} else {
				line1 = ChatColor.GRAY + "You've found this secret " + ChatColor.AQUA + amountFound + ChatColor.GRAY + " times";
			}
		} else {
			line1 = ChatColor.GRAY + "You've found this secret " + ChatColor.AQUA + "0" + ChatColor.GRAY + " times";
		}
		line1 = PlaceholderAPI.setPlaceholders(player, line1);

		itemmeta.setLore(Arrays.asList(new String[]{line1, line2, line3}));
		item.setItemMeta(itemmeta);
		return item;
	}



	private String getCooldown(String SVSSignID, UUID pUUID) {
		File SVSFile = new File("plugins/ServerSigns/signs/" + SVSSignID);
		String output;
		if (!SVSFile.exists()) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "ServerSigns File " + ChatColor.DARK_RED + SVSSignID + ChatColor.RED + " DOES NOT EXIST! Please notify DrakePork of issue and file name...");
			output = ChatColor.DARK_RED + "ERROR! Notify Admin!";
		} else {
			YamlConfiguration f = YamlConfiguration.loadConfiguration(SVSFile);
			if (f.getLong("lastUse." + pUUID) > 0L) {
				Long useTime = Long.valueOf(f.getLong("lastUse." + pUUID));
				Long cooldownLong = Long.valueOf(useTime.longValue() / 1000L + f.getLong("cooldown") - System.currentTimeMillis() / 1000L);
				int cooldown = cooldownLong.intValue();
				if (cooldown > 86400) {
					int days = cooldown / 86400;
					int hours = cooldown % 86400 / 3600;
					output = ChatColor.RED + "" + days + " days " + hours + " hrs";
				} else {
					int hours = cooldown / 3600;
					int minutes = cooldown % 3600 / 60;
					output = ChatColor.RED + "" + hours + " hrs " + minutes + " mins";
				}
			} else {
				output = ChatColor.GREEN + "Available Now!";
			}
		}
		return output;
	}

	public void openGUI(Player player, String pWorld) {
		File f = new File(plugin.getDataFolder() + File.separator + "secrets.yml");
		File secretsDataFile = new File(plugin.getDataFolder() + File.separator
				+ "secretsdata.yml");
		YamlConfiguration pData = YamlConfiguration.loadConfiguration(secretsDataFile);
		YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);

		Inventory rewardInv = Bukkit.createInventory(null, yamlf.getInt("inv-slots"), ChatColor.RED + "Secrets");
		if(yamlf.contains("inventory." + pWorld)) {
			for (int i = 0; i < yamlf.getInt("inv-slots"); i++) {
				if (yamlf.isSet("inventory." + pWorld + "." + i)) {
					Material material = Material.getMaterial(yamlf.getString("inventory." + pWorld + "." + i + ".material"));
					ItemStack item;
					int amount = yamlf.getInt("inventory." + pWorld + "." + i + ".amount");
					if (yamlf.getBoolean("inventory." + pWorld + "." + i + ".meta")) {
						String secretId = yamlf.getString("inventory." + pWorld + "." + i + ".id");
						if (!pData.contains(player.getUniqueId().toString())) {
							item = unknownItemStack();
							rewardInv.setItem(yamlf.getInt("inventory." + pWorld + "." + i + ".slot"), item);
						} else if (pData.getConfigurationSection(player.getUniqueId().toString() + ".secrets-found").contains(secretId)) {
							String name = yamlf.getString("inventory." + pWorld + "." + i + ".name");
							String file = yamlf.getString("inventory." + pWorld + "." + i + ".file");
							item = secretItemStack(material, amount, name, getCooldown(file, player.getUniqueId()), player, secretId);
							rewardInv.setItem(yamlf.getInt("inventory." + pWorld + "." + i + ".slot"), item);
						} else {
							item = unknownItemStack();
							rewardInv.setItem(yamlf.getInt("inventory." + pWorld + "." + i + ".slot"), item);
						}
					} else {
						String name = yamlf.getString("inventory." + pWorld + "." + i + ".name");
						item = decorativeItemStack(material, amount, name);
						rewardInv.setItem(yamlf.getInt("inventory." + pWorld + "." + i + ".slot"), item);
					}
				}
			}
			player.openInventory(rewardInv);
		} else {
			player.sendMessage(ChatColor.RED + "No secrets exist in this world yet!");
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player)sender;
		String pWorld = player.getWorld().getName().toLowerCase();
		openGUI(player, pWorld);
		return true;
	}
}
