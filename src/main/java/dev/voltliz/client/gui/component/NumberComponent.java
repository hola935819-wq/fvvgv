package dev.voltliz.client.gui.component;

import dev.voltliz.client.setting.NumberSetting;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.gui.GuiGraphics;

public class NumberComponent extends SettingComponent<NumberSetting> {
	private boolean dragging;

	public NumberComponent(NumberSetting setting) {
		super(setting);
	}

	@Override
	public int getHeight() {
		return 19;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		int barX = x + PADDING;
		int barWidth = width - PADDING * 2;
		if (dragging) {
			setting.setProgress((mouseX - barX) / (double) barWidth);
		}

		drawBackground(graphics);
		drawName(graphics, setting.getName(), y + 3);
		drawRight(graphics, setting.format(), y + 3, Theme.TEXT);

		int barY = y + 14;
		int filled = (int) Math.round(barWidth * setting.getProgress());
		graphics.fill(barX, barY, barX + barWidth, barY + 2, 0xFF2A2A33);
		graphics.fill(barX, barY, barX + filled, barY + 2, Theme.accent());
		graphics.fill(barX + filled - 1, barY - 1, barX + filled + 1, barY + 3, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			dragging = true;
			return true;
		}
		if (button == 1) {
			setting.reset();
			return true;
		}
		return false;
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {
		dragging = false;
	}
}
