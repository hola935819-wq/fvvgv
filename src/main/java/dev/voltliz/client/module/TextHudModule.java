package dev.voltliz.client.module;

import dev.voltliz.client.render.VText;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ColorSetting;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.gui.GuiGraphics;

/** A HUD element that shows a single "Label value" line. */
public abstract class TextHudModule extends HudModule {
	protected final BooleanSetting background = add(new BooleanSetting("Background", true));
	protected final BooleanSetting showLabel = add(new BooleanSetting("Show Label", true));
	protected final ColorSetting textColor = add(new ColorSetting("Text Color", Theme.TEXT));

	protected TextHudModule(String name, String description, float defaultX, float defaultY) {
		super(name, description, defaultX, defaultY);
	}

	protected abstract String getLabel();

	protected abstract String getValue();

	@Override
	protected void renderContent(GuiGraphics graphics) {
		String label = showLabel.get() ? getLabel() : "";
		String value = getValue();

		int gap = label.isEmpty() ? 0 : 4;
		int paddingX = background.get() ? 6 : 0;
		int paddingY = background.get() ? 4 : 0;
		width = VText.width(label, true) + gap + VText.width(value) + paddingX * 2;
		height = VText.HEIGHT + paddingY * 2;

		if (background.get()) {
			RenderUtil.hudBackground(graphics, 0, 0, width, height);
		}

		int textX = paddingX;
		if (!label.isEmpty()) {
			VText.draw(graphics, label, textX, paddingY, Theme.accent(), true);
			textX += VText.width(label, true) + gap;
		}
		VText.draw(graphics, value, textX, paddingY, textColor.getColor());
	}
}
