package dev.voltliz.client.module.hud;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.module.HudModule;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class WatermarkModule extends HudModule {
	private static final String TITLE = "Voltliz";

	private final BooleanSetting background = add(new BooleanSetting("Background", true));
	private final BooleanSetting showVersion = add(new BooleanSetting("Show Version", true));
	private final BooleanSetting wave = add(new BooleanSetting("Color Wave", true));

	private final String version = VoltlizClient.version();

	public WatermarkModule() {
		super("Watermark", "Shows the client name.", 4, 4);
		setEnabled(true);
	}

	@Override
	protected void renderContent(GuiGraphics graphics) {
		Font font = mc().font;
		String suffix = " Client" + (showVersion.get() ? " v" + version : "");

		int padding = background.get() ? 4 : 0;
		width = font.width(TITLE) + font.width(suffix) + padding * 2;
		height = font.lineHeight - 1 + padding * 2;

		if (background.get()) {
			RenderUtil.hudBackground(graphics, 0, 0, width, height);
		}

		int x = padding;
		long time = System.currentTimeMillis();
		for (int i = 0; i < TITLE.length(); i++) {
			String letter = String.valueOf(TITLE.charAt(i));
			int color = Theme.accent(i * 120L);
			if (wave.get()) {
				float t = (float) (Math.sin(time / 300.0 - i * 0.6) + 1) / 2;
				color = ColorUtil.lerp(color, 0xFFFFFFFF, t * 0.6f);
			}
			graphics.drawString(font, letter, x, padding, color, true);
			x += font.width(letter);
		}
		graphics.drawString(font, suffix, x, padding, Theme.TEXT_DIM, true);
	}
}
