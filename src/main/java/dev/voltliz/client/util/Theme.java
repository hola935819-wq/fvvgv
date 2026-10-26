package dev.voltliz.client.util;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.module.client.ClickGuiModule;

public final class Theme {
	public static final int DEFAULT_ACCENT = 0xFF00B4FF;

	public static final int WINDOW = 0xF20E1016;
	public static final int SIDEBAR = 0xFF13161E;
	public static final int CARD = 0xFF191C26;
	public static final int CARD_HOVER = 0xFF1F2330;
	public static final int FIELD = 0xFF262A38;
	public static final int SEPARATOR = 0x14FFFFFF;
	public static final int TEXT = 0xFFF2F4F8;
	public static final int TEXT_DIM = 0xFF8A90A2;
	public static final int HUD_BACKGROUND = 0xB00E1016;

	private Theme() {
	}

	public static int accent() {
		return accent(0);
	}

	public static int accent(long offset) {
		ClickGuiModule gui = gui();
		return gui != null ? gui.accent.getColor(offset) : DEFAULT_ACCENT;
	}

	public static ClickGuiModule gui() {
		VoltlizClient client = VoltlizClient.get();
		return client != null ? client.getModuleManager().get(ClickGuiModule.class) : null;
	}
}
