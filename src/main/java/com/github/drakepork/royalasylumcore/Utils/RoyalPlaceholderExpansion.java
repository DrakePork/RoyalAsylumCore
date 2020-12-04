package com.github.drakepork.royalasylumcore.Utils;

import com.github.drakepork.royalasylumcore.Core;
import com.google.inject.Inject;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoyalPlaceholderExpansion extends PlaceholderExpansion {
	private Core plugin;

	@Inject
	public RoyalPlaceholderExpansion(Core plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean persist(){
		return true;
	}

	@Override
	public boolean canRegister(){
		return true;
	}

	@Override
	public String getAuthor(){
		return plugin.getDescription().getAuthors().toString();
	}

	@Override
	public String getIdentifier(){
		return "RoyalAsylumCore";
	}

	@Override
	public String getVersion(){
		return plugin.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier){

		if(player == null){
			return "";
		}
		@NotNull String quests = PlaceholderAPI.setPlaceholders(player, "%quests_player_current_quest_names%");
		List<String> quest;
		if(!quests.isEmpty()) {
			quest = new ArrayList<String>(Arrays.asList(quests.split("\n")));
		} else {
			quest = new ArrayList<String>();
			quest.add("");
			quest.add("");
		}
		if(quest.size() == 1) {
			quest.add("");
		}
		return null;
	}
}
