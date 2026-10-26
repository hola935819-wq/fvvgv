package dev.voltliz.client.render;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
//? if >=1.21.9
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

/** Text drawn with the bundled Poppins font instead of the Minecraft font. */
public final class VText {
	public static final int HEIGHT = 9;

	private static final Style REGULAR = Style.EMPTY.withFont(font("regular"));
	private static final Style BOLD = Style.EMPTY.withFont(font("bold"));

	private VText() {
	}

	//? if >=1.21.9 {
	private static FontDescription font(String name) {
		return new FontDescription.Resource(Identifier.fromNamespaceAndPath(VoltlizClient.MOD_ID, name));
	}
	//?} else {
	/*private static Identifier font(String name) {
		return Identifier.fromNamespaceAndPath(VoltlizClient.MOD_ID, name);
	}
	*///?}

	public static Component of(String text, boolean bold) {
		return Component.literal(text).withStyle(bold ? BOLD : REGULAR);
	}

	public static int width(String text) {
		return width(text, false);
	}

	public static int width(String text, boolean bold) {
		return font().width(of(text, bold));
	}

	public static void draw(GuiGraphics graphics, String text, float x, float y, int color) {
		draw(graphics, text, x, y, color, false);
	}

	public static void draw(GuiGraphics graphics, String text, float x, float y, int color, boolean bold) {
		color = Render2D.fade(color);
		if ((color >>> 24) < 4) {
			return;
		}
		graphics.drawString(font(), of(text, bold), Math.round(x), Math.round(y), color, false);
	}

	public static void drawCentered(GuiGraphics graphics, String text, float centerX, float y, int color, boolean bold) {
		draw(graphics, text, centerX - width(text, bold) / 2f, y, color, bold);
	}

	public static void drawScaled(GuiGraphics graphics, String text, float x, float y, int color, boolean bold, float scale) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(scale, scale);
		draw(graphics, text, 0, 0, color, bold);
		graphics.pose().popMatrix();
	}

	/** Draws each character with a color between {@code from} and {@code to}; returns the width. */
	public static int drawGradient(GuiGraphics graphics, String text, float x, float y, int from, int to, boolean bold) {
		int total = Math.max(1, width(text, bold));
		float cursor = x;
		for (int i = 0; i < text.length(); i++) {
			String letter = String.valueOf(text.charAt(i));
			float t = (cursor - x + width(letter, bold) / 2f) / total;
			draw(graphics, letter, cursor, y, ColorUtil.lerp(from, to, t), bold);
			cursor += width(letter, bold);
		}
		return total;
	}

	/** Shortens {@code text} with an ellipsis so it fits in {@code maxWidth}. */
	public static String trim(String text, int maxWidth, boolean bold) {
		if (width(text, bold) <= maxWidth) {
			return text;
		}
		String result = text;
		while (result.length() > 1 && width(result + "...", bold) > maxWidth) {
			result = result.substring(0, result.length() - 1);
		}
		return result.trim() + "...";
	}

	private static Font font() {
		return Minecraft.getInstance().font;
	}
}
