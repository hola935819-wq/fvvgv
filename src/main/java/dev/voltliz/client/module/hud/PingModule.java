package dev.voltliz.client.module.hud;

import dev.voltliz.client.module.TextHudModule;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

public class PingModule extends TextHudModule {
	public PingModule() {
		super("Ping", "Shows your latency to the server.", 4, 84);
	}

	@Override
	protected String getLabel() {
		return "Ping";
	}

	@Override
	protected String getValue() {
		ClientPacketListener connection = mc().getConnection();
		if (connection == null || mc().player == null) {
			return "-";
		}
		PlayerInfo info = connection.getPlayerInfo(mc().player.getUUID());
		return (info != null ? info.getLatency() : 0) + " ms";
	}
}
