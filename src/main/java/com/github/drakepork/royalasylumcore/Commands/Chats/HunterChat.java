package com.github.drakepork.royalasylumcore.Commands.Chats;

import com.github.drakepork.royalasylumcore.Core;
import com.github.drakepork.royalasylumcore.Utils.ChatUtils;
import com.google.inject.Inject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HunterChat implements CommandExecutor {
	private Core plugin;

	@Inject
	public HunterChat(Core plugin) {
		this.plugin = plugin;
	}

	@Inject private ChatUtils chatUtils;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			chatUtils.chatSendMessage(args, sender, "hunter", "hunter-chat");
		} else {
			chatUtils.consoleChatSend(args, "hunter", "hunter-chat");
		}
		return true;
	}
}
