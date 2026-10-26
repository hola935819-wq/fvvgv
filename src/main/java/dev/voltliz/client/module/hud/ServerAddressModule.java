package dev.voltliz.client.module.hud;

import dev.voltliz.client.module.TextHudModule;
import net.minecraft.client.multiplayer.ServerData;

public class ServerAddressModule extends TextHudModule {
	public ServerAddressModule() {
		super("Server Address", "Shows the server you are playing on.", 4, 110);
	}

	@Override
	protected String getLabel() {
		return "Server";
	}

	@Override
	protected String getValue() {
		ServerData server = mc().getCurrentServer();
		return server != null ? server.ip : "Singleplayer";
	}
}
