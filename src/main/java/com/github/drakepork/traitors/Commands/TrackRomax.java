package com.github.drakepork.traitors.Commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.github.drakepork.traitors.TraitorsMain;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TrackRomax implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(player.equals(Bukkit.getPlayer("DrakePork"))) {
				CMIUser user = CMI.getInstance().getPlayerManager().getUser(UUID.fromString("075538c0-dd3b-4207-ae5a-9ce97547017a"));
				Player rom = Bukkit.getPlayer(UUID.fromString("075538c0-dd3b-4207-ae5a-9ce97547017a"));
				if(args.length < 1) {
					sender.sendMessage("Log off location: " + user.getLogOutLocation().getX() + " " + user.getLogOutLocation().getY() + " " + user.getLogOutLocation().getZ() + " " + user.getLogOutLocation().getWorld().getName() + "\nCurrent Coords: " + user.getLocation().getX() + " " + user.getLocation().getY() + " " + user.getLocation().getZ() + " " + user.getLocation().getWorld().getName());
				} else if(args[0].equalsIgnoreCase("smite")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi smite " + user.getName());
				} else if(args[0].equalsIgnoreCase("slow-down")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi walkspeed " + user.getName() + " 0.1");
					TraitorsMain.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(TraitorsMain.getInstance(), new Runnable() {
						public void run() {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi walkspeed " + user.getName() + " 1");
						}
					}, 300);
				} else if(args[0].equalsIgnoreCase("cuff")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi cuff " + user.getName() + " true");
					TraitorsMain.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(TraitorsMain.getInstance(), new Runnable() {
						public void run() {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi cuff " + user.getName() + " false");
						}
					}, 300);
				} else if(args[0].equalsIgnoreCase("no-hp")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi maxhp set " + user.getName() + " 1");
					TraitorsMain.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(TraitorsMain.getInstance(), new Runnable() {
						public void run() {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi cuff " + user.getName() + " 20");
						}
					}, 300);
				} else if(args[0].equalsIgnoreCase("fuck-off")) {
					if(args.length > 2) {
						Player wham = Bukkit.getPlayer(args[1]);
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi warp noble " + wham.getName());
					}
				} else if(args[0].equalsIgnoreCase("launch")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi launch " + user.getName() + " p:5");
				} else if(args[0].equalsIgnoreCase("bye-potions")) {
					rom.getActivePotionEffects().clear();
				} else if(args[0].equalsIgnoreCase("hungry")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi hunger " + user.getName() + " 0");
				}
			}
		}
		return true;
	}
}
