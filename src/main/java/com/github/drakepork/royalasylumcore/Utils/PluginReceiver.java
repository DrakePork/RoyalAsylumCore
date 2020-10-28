package com.github.drakepork.royalasylumcore.Utils;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.github.drakepork.royalasylumcore.Core;

public class PluginReceiver extends AbstractModule {

	protected final Core plugin;

	public PluginReceiver(Core plugin) {
		this.plugin = plugin;
	}

	public Injector createInjector() {
		return Guice.createInjector(this);
	}

	@Override
	protected void configure() {
		this.bind(Core.class).toInstance(this.plugin);
	}
}
