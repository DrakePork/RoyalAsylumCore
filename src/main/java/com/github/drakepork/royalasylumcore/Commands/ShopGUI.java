package com.github.drakepork.royalasylumcore.Commands;

import com.github.drakepork.royalasylumcore.Core;
import com.google.inject.Inject;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopGUI implements CommandExecutor {
	private Core plugin;

	@Inject
	public ShopGUI(Core plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 1) {
				String shop = args[0];
				if(ShopGuiPlusApi.getShop(shop) != null) {
					if(player.hasPermission("shopguiplus.shops." + shop)) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shop " + player.getName() + " " + shop);
					} else {
						player.sendMessage(ChatColor.RED + "You do not have access to this shop!");
					}
				} else {
					player.sendMessage(ChatColor.RED + "Not a valid shop!");
				}
			} else {
				player.sendMessage(ChatColor.RED + "Wrong Usage!");
			}
		}
		return true;
	}
}
