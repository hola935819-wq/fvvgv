package dev.voltliz.client.render;

import dev.voltliz.client.VoltlizClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

/** Text drawn with the bundled Poppins font instead of the Minecraft font. */
public final class VText {
	public static final int HEIGHT = 9;

	private static final Style REGULAR = Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath(VoltlizClient.MOD_ID, "regular")));
	private static final Style BOLD = Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath(VoltlizClient.MOD_ID, "bold")));

	private VText() {
	}

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
		graphics.drawString(font(), of(text, bold), Math.round(x), Math.round(y), color, false);
	}

	public static void drawCentered(GuiGraphics graphics, String text, float centerX, float y, int color, boolean bold) {
		draw(graphics, text, centerX - width(text, bold) / 2f, y, color, bold);
	}

	public static void drawScaled(GuiGraphics graphics, String text, float x, float y, int color, boolean bold, float scale) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(scale, scale);
		graphics.drawString(font(), of(text, bold), 0, 0, color, false);
		graphics.pose().popMatrix();
	}

	private static Font font() {
		return Minecraft.getInstance().font;
	}
}
