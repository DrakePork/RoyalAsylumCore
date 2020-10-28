package com.github.drakepork.royalasylumcore.Utils;

import com.google.inject.Inject;
import org.bukkit.configuration.file.FileConfiguration;
import com.github.drakepork.royalasylumcore.Core;


public class ConfigCreator {
	private Core plugin;

	@Inject
	public ConfigCreator(Core plugin) {
		this.plugin = plugin;
	}

	public void init() {
		FileConfiguration config = this.plugin.getConfig();
		config.addDefault("lang-file", "en.yml");
		config.options().copyDefaults(true);
		this.plugin.saveConfig();
	}
}

