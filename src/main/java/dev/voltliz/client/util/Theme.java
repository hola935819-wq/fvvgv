package dev.voltliz.client.util;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.module.client.ClickGuiModule;

public final class Theme {
	public static final int DEFAULT_ACCENT = 0xFF00C8FF;
	public static final int DEFAULT_ACCENT_2 = 0xFF8C5AFF;

	public static final int WINDOW_TOP = 0xF6141A2C;
	public static final int WINDOW_BOTTOM = 0xF60A0D18;
	public static final int SIDEBAR = 0x70060810;
	public static final int CARD = 0xFF161B2B;
	public static final int CARD_HOVER = 0xFF1C2236;
	public static final int FIELD = 0xFF252C42;
	public static final int SEPARATOR = 0x12FFFFFF;
	public static final int TEXT = 0xFFF3F5FA;
	public static final int TEXT_DIM = 0xFF8C94AD;
	public static final int HUD_BACKGROUND = 0xB80D111C;

	private Theme() {
	}

	public static int accent() {
		return accent(0);
	}

	public static int accent(long offset) {
		ClickGuiModule gui = gui();
		return gui != null ? gui.accent.getColor(offset) : DEFAULT_ACCENT;
	}

	public static int accent2() {
		ClickGuiModule gui = gui();
		if (gui == null) {
			return DEFAULT_ACCENT_2;
		}
		return gui.accent.isRainbow() ? gui.accent.getColor(1200) : gui.accent2.getColor();
	}

	/** A color along the accent gradient, from {@code 0} (accent) to {@code 1} (second accent). */
	public static int gradient(float t) {
		return ColorUtil.lerp(accent(), accent2(), Math.max(0, Math.min(1, t)));
	}

	public static ClickGuiModule gui() {
		VoltlizClient client = VoltlizClient.get();
		return client != null ? client.getModuleManager().get(ClickGuiModule.class) : null;
	}
}
