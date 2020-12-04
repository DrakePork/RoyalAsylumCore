package com.github.drakepork.royalasylumcore.Commands.Chats;
import com.github.drakepork.royalasylumcore.Core;
import com.github.drakepork.royalasylumcore.Utils.ChatUtils;
import com.google.inject.Inject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SecretChat implements CommandExecutor {
	private Core plugin;

	@Inject
	public SecretChat(Core plugin) {
		this.plugin = plugin;
	}

	@Inject private ChatUtils chatUtils;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			chatUtils.chatSendMessage(args, sender, "secret", "secret-chat");
		} else {
			chatUtils.consoleChatSend(args, "secret", "secret-chat");
		}
		return true;
	}
}
