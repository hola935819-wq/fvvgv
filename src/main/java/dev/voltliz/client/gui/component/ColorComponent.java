package dev.voltliz.client.gui.component;

import dev.voltliz.client.setting.ColorSetting;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.RenderUtil;
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
		return 21;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		int barX = x + PADDING;
		int barWidth = width - PADDING * 2;
		if (dragging) {
			setting.setRainbow(false);
			setting.setHue(Math.max(0, Math.min(0.999f, (mouseX - barX) / (float) barWidth)));
		}

		drawBackground(graphics);
		drawName(graphics, setting.getName(), y + 3);

		int previewX = x + width - PADDING - 8;
		RenderUtil.outline(graphics, previewX - 1, y + 2, 10, 10, 0xFF3A3A44);
		graphics.fill(previewX, y + 3, previewX + 8, y + 11, setting.getColor());
		if (setting.isRainbow()) {
			graphics.drawString(font(), "RGB", previewX - 4 - font().width("RGB"), y + 3, setting.getColor(), false);
		}

		int barY = y + 14;
		for (int i = 0; i < barWidth; i++) {
			graphics.fill(barX + i, barY, barX + i + 1, barY + 4, ColorUtil.hsb(i / (float) barWidth, 1f, 1f, 255));
		}
		if (!setting.isRainbow()) {
			int markerX = barX + Math.round(setting.getHue() * barWidth);
			graphics.fill(markerX - 1, barY - 1, markerX + 1, barY + 5, Theme.TEXT);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && mouseY >= y + 12) {
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
