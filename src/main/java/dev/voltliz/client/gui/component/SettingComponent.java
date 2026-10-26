package dev.voltliz.client.gui.component;

import dev.voltliz.client.setting.Setting;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.gui.GuiGraphics;

public abstract class SettingComponent<S extends Setting<?>> extends GuiComponent {
	protected static final int PADDING = 6;

	protected final S setting;

	protected SettingComponent(S setting) {
		this.setting = setting;
	}

	public boolean isVisible() {
		return setting == null || setting.isVisible();
	}

	protected void drawBackground(GuiGraphics graphics) {
		graphics.fill(x, y, x + width, y + getHeight(), Theme.SETTING);
	}

	protected void drawName(GuiGraphics graphics, String name, int textY) {
		graphics.drawString(font(), name, x + PADDING, textY, Theme.TEXT_DIM, false);
	}

	protected void drawRight(GuiGraphics graphics, String text, int textY, int color) {
		graphics.drawString(font(), text, x + width - PADDING - font().width(text), textY, color, false);
	}
}
