package dev.voltliz.client.util;

import net.minecraft.client.gui.GuiGraphics;

public final class RenderUtil {
	private RenderUtil() {
	}

	/** A rectangle with its corner pixels cut, which reads as slightly rounded at GUI scale. */
	public static void roundedRect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
		if (width < 3 || height < 3) {
			graphics.fill(x, y, x + width, y + height, color);
			return;
		}
		graphics.fill(x + 1, y, x + width - 1, y + height, color);
		graphics.fill(x, y + 1, x + 1, y + height - 1, color);
		graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
	}

	public static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + 1, color);
		graphics.fill(x, y + height - 1, x + width, y + height, color);
		graphics.fill(x, y + 1, x + 1, y + height - 1, color);
		graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
	}

	/** The translucent box behind HUD elements, with an accent bar on the left. */
	public static void hudBackground(GuiGraphics graphics, int x, int y, int width, int height) {
		roundedRect(graphics, x, y, width, height, Theme.HUD_BACKGROUND);
		graphics.fill(x, y + 1, x + 1, y + height - 1, Theme.accent());
	}

	public static boolean isInside(double mouseX, double mouseY, double x, double y, double width, double height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
}
