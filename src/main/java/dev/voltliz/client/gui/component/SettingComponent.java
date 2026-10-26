package dev.voltliz.client.gui.component;

import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
import dev.voltliz.client.setting.Setting;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.gui.GuiGraphics;

public abstract class SettingComponent<S extends Setting<?>> extends GuiComponent {
	protected final S setting;

	protected SettingComponent(S setting) {
		this.setting = setting;
	}

	public boolean isVisible() {
		return setting == null || setting.isVisible();
	}

	protected void drawName(GuiGraphics graphics, String name, int textY) {
		VText.draw(graphics, name, x, textY, Theme.TEXT_DIM);
	}

	/** A text pill aligned to the right edge; returns its left x. */
	protected int drawPill(GuiGraphics graphics, String text, int pillY, int textColor) {
		int pillWidth = VText.width(text) + 10;
		int pillX = x + width - pillWidth;
		Render2D.roundedRect(graphics, pillX, pillY, pillWidth, 13, 6.5f, Theme.FIELD);
		VText.draw(graphics, text, pillX + 5, pillY + 3, textColor);
		return pillX;
	}

	/** An iOS style switch. {@code progress} goes from 0 (off) to 1 (on). */
	public static void drawSwitch(GuiGraphics graphics, float x, float y, float progress) {
		Render2D.roundedRect(graphics, x, y, 20, 11, 5.5f, 0xFF2C3349);
		if (progress > 0.01f) {
			int alpha = Math.round(progress * 255);
			Render2D.horizontalGradient(graphics, x, y, 20, 11, 5.5f, ColorUtil.withAlpha(Theme.accent(), alpha), ColorUtil.withAlpha(Theme.accent2(), alpha));
		}
		Render2D.circle(graphics, x + 5.5f + progress * 9, y + 5.5f, 4, 0xFFFFFFFF);
	}
}
