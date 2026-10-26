package dev.voltliz.client.module;

import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ColorSetting;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** A HUD element that shows a single "Label: value" line. */
public abstract class TextHudModule extends HudModule {
	protected final BooleanSetting background = add(new BooleanSetting("Background", true));
	protected final BooleanSetting showLabel = add(new BooleanSetting("Show Label", true));
	protected final BooleanSetting shadow = add(new BooleanSetting("Text Shadow", true));
	protected final ColorSetting textColor = add(new ColorSetting("Text Color", 0xFFFFFFFF));

	protected TextHudModule(String name, String description, float defaultX, float defaultY) {
		super(name, description, defaultX, defaultY);
	}

	protected abstract String getLabel();

	protected abstract String getValue();

	@Override
	protected void renderContent(GuiGraphics graphics) {
		Font font = mc().font;
		String label = showLabel.get() ? getLabel() + ": " : "";
		String value = getValue();

		int padding = background.get() ? 4 : 0;
		width = font.width(label) + font.width(value) + padding * 2;
		height = font.lineHeight - 1 + padding * 2;

		if (background.get()) {
			RenderUtil.hudBackground(graphics, 0, 0, width, height);
		}

		int textX = padding;
		if (!label.isEmpty()) {
			graphics.drawString(font, label, textX, padding, Theme.accent(), shadow.get());
			textX += font.width(label);
		}
		graphics.drawString(font, value, textX, padding, textColor.getColor(), shadow.get());
	}
}
