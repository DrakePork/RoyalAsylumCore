package com.github.drakepork.royalasylumcore.Utils;

import com.google.inject.Inject;
import org.bukkit.configuration.file.FileConfiguration;
import com.github.drakepork.royalasylumcore.Core;

import java.util.ArrayList;


public class ConfigCreator {
	private Core plugin;

	@Inject
	public ConfigCreator(Core plugin) {
		this.plugin = plugin;
	}

	public void init() {
		FileConfiguration config = plugin.getConfig();
		config.addDefault("lang-file", "en.yml");

		config.addDefault("cooldowns.traitortrack-personal-cooldown", 30);
		config.addDefault("cooldowns.traitortrack-global-cooldown", 30);
		config.addDefault("cooldowns.traitor-grace-period", 10);
		config.addDefault("cooldowns.traitor-time-required", 1200);
		config.addDefault("cooldowns.login-badlands-delay", 5);
		config.addDefault("login-badlands-tpout", true);
		config.addDefault("tracker-token-cost", 100);
		config.addDefault("track-cost.personal-token-cost", 100);
		config.addDefault("track-cost.global-token-cost", 100);
		config.addDefault("track-cost.global-money-cost", 5000);
		config.addDefault("default-lives", 1);
		config.addDefault("traitor-kill-tokens", 20);
		config.addDefault("hunter-kill-prize.money", 250000);
		config.addDefault("hunter-kill-prize.money-extra-life", 50000);
		config.addDefault("hunter-kill-prize.tokens", 500);
		config.addDefault("hunter-kill-prize.tokens-extra-life", 250);
		config.addDefault("traitor-effect.type", "HUNGER");
		config.addDefault("traitor-effect.duration", 100000);
		config.addDefault("traitor-effect.amplifier", 3);
		ArrayList list = new ArrayList();
		list.add("group.default:100");
		config.addDefault("token-kill-delay", 15);
		config.addDefault("token-group-kill-rewards", list);

		config.options().copyDefaults(true);
		this.plugin.saveConfig();
	}
}

