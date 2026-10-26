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
		Render2D.roundedRect(graphics, pillX, pillY, pillWidth, 13, 4, Theme.FIELD);
		VText.draw(graphics, text, pillX + 5, pillY + 3, textColor);
		return pillX;
	}

	/** An iOS style switch. {@code progress} goes from 0 (off) to 1 (on). */
	public static void drawSwitch(GuiGraphics graphics, float x, float y, float progress) {
		int accent = Theme.accent();
		Render2D.roundedRect(graphics, x, y, 18, 10, 5, ColorUtil.lerp(0xFF333849, accent, progress));
		Render2D.circle(graphics, x + 5 + progress * 8, y + 5, 3.5f, 0xFFFFFFFF);
	}
}
