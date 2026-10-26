package dev.voltliz.client.gui.component;

import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
import dev.voltliz.client.setting.ColorSetting;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.gui.GuiGraphics;

/** Hue bar for a color. Right click switches rainbow mode. */
public class ColorComponent extends SettingComponent<ColorSetting> {
	private boolean dragging;

	public ColorComponent(ColorSetting setting) {
		super(setting);
	}

	@Override
	public int getHeight() {
		return 26;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		if (dragging) {
			setting.setRainbow(false);
			setting.setHue(Math.max(0, Math.min(0.999f, (mouseX - x) / (float) width)));
		}

		drawName(graphics, setting.getName(), y + 3);
		Render2D.circle(graphics, x + width - 5, y + 7, 5, 0xFF2E3344);
		Render2D.circle(graphics, x + width - 5, y + 7, 4, setting.getColor());
		if (setting.isRainbow()) {
			VText.draw(graphics, "Rainbow", x + width - 14 - VText.width("Rainbow"), y + 3, setting.getColor());
		}

		// Hue bar: rounded ends plus one-pixel columns between them.
		int barY = y + 17;
		int barHeight = 5;
		Render2D.roundedRect(graphics, x, barY, barHeight, barHeight, 2.5f, ColorUtil.hsb(0, 1, 1, 255));
		Render2D.roundedRect(graphics, x + width - barHeight, barY, barHeight, barHeight, 2.5f, ColorUtil.hsb(0.999f, 1, 1, 255));
		for (int i = 2; i < width - 2; i++) {
			graphics.fill(x + i, barY, x + i + 1, barY + barHeight, ColorUtil.hsb(i / (float) width, 0.85f, 1f, 255));
		}
		if (!setting.isRainbow()) {
			float markerX = x + setting.getHue() * width;
			Render2D.circle(graphics, markerX, barY + barHeight / 2f, 4, 0xFFFFFFFF);
			Render2D.circle(graphics, markerX, barY + barHeight / 2f, 2.5f, setting.getColor());
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && mouseY >= y + 13) {
			dragging = true;
			return true;
		}
		if (button == 1) {
			setting.setRainbow(!setting.isRainbow());
			return true;
		}
		return false;
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {
		dragging = false;
	}
}
