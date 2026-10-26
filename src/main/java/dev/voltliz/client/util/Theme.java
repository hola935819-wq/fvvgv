package dev.voltliz.client.util;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.module.client.ClickGuiModule;

public final class Theme {
	public static final int DEFAULT_ACCENT = 0xFF00B4FF;

	public static final int PANEL = 0xF00E0E12;
	public static final int ROW = 0xE016161C;
	public static final int ROW_HOVER = 0xE0202028;
	public static final int SETTING = 0xE0111116;
	public static final int TEXT = 0xFFFFFFFF;
	public static final int TEXT_DIM = 0xFFA0A0AC;
	public static final int HUD_BACKGROUND = 0x80000000;

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
