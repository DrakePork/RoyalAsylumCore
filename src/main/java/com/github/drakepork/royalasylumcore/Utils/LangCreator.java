package com.github.drakepork.royalasylumcore.Utils;

import com.google.inject.Inject;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.github.drakepork.royalasylumcore.Core;

import java.io.File;
import java.io.IOException;

public class LangCreator {
	private Core plugin;

	@Inject
	public LangCreator(Core plugin) {
		this.plugin = plugin;
	}

	public void init() {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		try {
			FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);

			// Global Messages

			langConf.addDefault("global.plugin-prefix", "&f[&cRoyalAsylum&f] ");

			// Chat Messages

			langConf.addDefault("chat.admin.format", "&8&l(&c&lADMIN&8&l) &b[name]: &a[message]");
			langConf.addDefault("chat.admin.wrong-usage", "&cWrong Usage! /a <message>");

			langConf.addDefault("chat.report.format", "&8&l(&4&lREPORT&8&l) &b[name]: &c[message]");
			langConf.addDefault("chat.report.wrong-usage", "&cWrong Usage! /report <message>");
			langConf.addDefault("chat.report.success", "&bYour report has been received!");

			langConf.addDefault("chat.guard.format", "&8&l(&4&lRoyal&9&lGuard&8&l) &6[name]&7: &3[message]");
			langConf.addDefault("chat.guard.wrong-usage", "&cWrong Usage! /k <message>");

			langConf.addDefault("chat.discord.format", "&8&l(&9&lDISCORD&8&l) &8[name]: &7[message]");
			langConf.addDefault("chat.discord.wrong-usage", "&cWrong Usage! /d <message>");

			langConf.addDefault("chat.build.format", "&8&l(&a&lBUILDER&8&l) &7[name]: &9[message]");
			langConf.addDefault("chat.build.wrong-usage", "&cWrong Usage! /b <message>");

			langConf.addDefault("chat.roundtable.format", "&8&l(&4&lRound&6&lTable&8&l) &e[name]: &c[message]");
			langConf.addDefault("chat.roundtable.wrong-usage", "&cWrong Usage! /rtc <message>");

			langConf.addDefault("chat.traitor.format", "&8&l(&f&lTRAITOR&8&l) &7[name]: &f[message]");
			langConf.addDefault("chat.traitor.wrong-usage", "&cWrong Usage! /tc <message>");

			langConf.addDefault("chat.hunter.format", "&8&l(&6&lHUNTER&8&l) &8[name]: &f[message]");
			langConf.addDefault("chat.hunter.wrong-usage", "&cWrong Usage! /hunt <message>");

			langConf.addDefault("chat.secret.format", "&8&l(&e&lSECRET&8&l) &8[name]: &f[message]");
			langConf.addDefault("chat.secret.wrong-usage", "&cWrong Usage! /secret <message>");

			langConf.addDefault("chat.discordSRV.format", "**[name]**: [message]");
			langConf.addDefault("chat.stickied.enabled", "&aEnabled sticky chat for [chat]");
			langConf.addDefault("chat.stickied.disabled", "&aDisabled sticky chat for [chat]");
			langConf.addDefault("chat.stickied.swapped", "&aSwapped sticky chat from [oldchat] to [newchat]");

			// Traitor Messages

			langConf.addDefault("traitor.traitorcheck.wrong-usage","&cWrong Usage! /traitorcheck <player>");
			langConf.addDefault("traitor.traitorcheck.playtime-left","&8[&6TraitorTracker&8] &c[player] &7has &6[h]h [m]m [s]s &7left until they become a bandit!");
			langConf.addDefault("traitor.traitorcheck.lives-left","&8[&6TraitorTracker&8] &c[player] &7has &6[lives] &7lives left!");
			langConf.addDefault("traitor.no-such-player", "&cThis player hasnt been on the server before!");
			langConf.addDefault("traitor.not-a-traitor", "&cThis player isn't a traitor!");
			langConf.addDefault("traitor.already-bandit", "&cYou're already a bandit!");

			langConf.addDefault("traitor.traitorcheck.personal-playtime-left","&8[&6TraitorTracker&8] &7You have &6[h]h [m]m [s]s &7left until you become a bandit!");
			langConf.addDefault("traitor.traitorcheck.personal-lives-left","&8[&6TraitorTracker&8] &7You have &6[lives] &7lives left!");


			langConf.addDefault("traitor.portal-open-timer", "&8[&6Royal Decree&8] &bThe Badlands portals are opening in [m] minutes...");
			langConf.addDefault("traitor.portal-open", "&8[&6Royal Decree&8] &bThe Badlands portals have OPENED");
			langConf.addDefault("traitor.portal-close", "&8[&6Royal Decree&8] &bThe Badlands portals are opening in [m] minutes...");


			langConf.options().copyDefaults(true);
			langConf.save(lang);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
