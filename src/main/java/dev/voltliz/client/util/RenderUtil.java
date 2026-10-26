package dev.voltliz.client.util;

import dev.voltliz.client.render.Render2D;
import net.minecraft.client.gui.GuiGraphics;

public final class RenderUtil {
	private RenderUtil() {
	}

	/** The rounded, translucent box behind HUD elements. */
	public static void hudBackground(GuiGraphics graphics, float x, float y, float width, float height) {
		Render2D.roundedRect(graphics, x, y, width, height, 4, Theme.HUD_BACKGROUND);
	}

	public static boolean isInside(double mouseX, double mouseY, double x, double y, double width, double height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
}
