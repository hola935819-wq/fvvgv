package dev.voltliz.client.module.hud;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.module.HudModule;
import dev.voltliz.client.render.Icons;
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

		int padding = background.get() ? 5 : 0;
		int logoSize = 14;
		int titleWidth = VText.width(TITLE, true);
		int contentWidth = logoSize + 5 + titleWidth + 4 + VText.width(suffix) + (info.isEmpty() ? 0 : 11 + VText.width(info));
		width = contentWidth + padding * 2 + (background.get() ? 2 : 0);
		height = Math.max(logoSize, VText.HEIGHT) + padding * 2;

		if (background.get()) {
			RenderUtil.hudBackground(graphics, 0, 0, width, height);
			// Thin gradient line along the top edge.
			Render2D.horizontalGradient(graphics, 4, 0, width - 8, 1, 0, Theme.accent(), Theme.accent2());
		}

		int centerY = height / 2;
		Icons.logo(graphics, padding, centerY - logoSize / 2f, logoSize);

		int x = padding + logoSize + 5;
		float textY = centerY - VText.HEIGHT / 2f;
		int from = Theme.accent();
		int to = Theme.accent2();
		if (wave.get()) {
			float t = (float) (Math.sin(System.currentTimeMillis() / 600.0) + 1) / 2;
			from = ColorUtil.lerp(Theme.accent(), Theme.accent2(), t);
			to = ColorUtil.lerp(Theme.accent2(), Theme.accent(), t);
		}
		x += VText.drawGradient(graphics, TITLE, x, textY, from, to, true);
		x += 4;
		VText.draw(graphics, suffix, x, textY, Theme.TEXT);
		x += VText.width(suffix);

		if (!info.isEmpty()) {
			Render2D.rect(graphics, x + 5, padding + 2, 1, height - padding * 2 - 4, 0x30FFFFFF);
			VText.draw(graphics, info, x + 11, textY, Theme.TEXT_DIM);
		}
	}
}
