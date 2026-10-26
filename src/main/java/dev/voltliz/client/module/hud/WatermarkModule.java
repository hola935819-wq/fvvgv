package dev.voltliz.client.module.hud;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.module.HudModule;
import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.gui.GuiGraphics;

public class WatermarkModule extends HudModule {
	private static final String TITLE = "Voltliz";

	private final BooleanSetting background = add(new BooleanSetting("Background", true));
	private final BooleanSetting showVersion = add(new BooleanSetting("Show Version", true));
	private final BooleanSetting showFps = add(new BooleanSetting("Show FPS", false));
	private final BooleanSetting wave = add(new BooleanSetting("Color Wave", true));

	private final String version = VoltlizClient.version();

	public WatermarkModule() {
		super("Watermark", "Shows the client name.", 4, 4);
		setEnabled(true);
	}

	@Override
	protected void renderContent(GuiGraphics graphics) {
		String suffix = "Client";
		String info = (showVersion.get() ? "v" + version : "") + (showFps.get() ? (showVersion.get() ? "  " : "") + mc().getFps() + " fps" : "");

		int padding = background.get() ? 6 : 0;
		int titleWidth = VText.width(TITLE, true);
		int contentWidth = 12 + titleWidth + 3 + VText.width(suffix) + (info.isEmpty() ? 0 : 10 + VText.width(info));
		width = contentWidth + padding * 2;
		height = VText.HEIGHT + 8;

		if (background.get()) {
			RenderUtil.hudBackground(graphics, 0, 0, width, height);
		}

		int centerY = height / 2;
		Render2D.circle(graphics, padding + 4, centerY, 4.5f, ColorUtil.withAlpha(Theme.accent(), 0x50));
		Render2D.circle(graphics, padding + 4, centerY, 2.5f, Theme.accent());

		int x = padding + 12;
		int textY = 4;
		long time = System.currentTimeMillis();
		for (int i = 0; i < TITLE.length(); i++) {
			String letter = String.valueOf(TITLE.charAt(i));
			int color = Theme.accent(i * 120L);
			if (wave.get()) {
				float t = (float) (Math.sin(time / 350.0 - i * 0.55) + 1) / 2;
				color = ColorUtil.lerp(color, 0xFFFFFFFF, t * 0.55f);
			}
			VText.draw(graphics, letter, x, textY, color, true);
			x += VText.width(letter, true);
		}
		x += 3;
		VText.draw(graphics, suffix, x, textY, Theme.TEXT);
		x += VText.width(suffix);

		if (!info.isEmpty()) {
			Render2D.rect(graphics, x + 5, 4, 1, height - 8, 0x30FFFFFF);
			VText.draw(graphics, info, x + 10, textY, Theme.TEXT_DIM);
		}
	}
}
