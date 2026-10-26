package dev.voltliz.client.render;

import dev.voltliz.client.module.Category;
import net.minecraft.client.gui.GuiGraphics;

/** Small vector icons built from Render2D shapes, sized to a {@code size} x {@code size} box. */
public final class Icons {
	private Icons() {
	}

	public static void category(GuiGraphics graphics, Category category, float x, float y, float size, int color) {
		switch (category) {
			case HUD -> layout(graphics, x, y, size, color);
			case PLAYER -> player(graphics, x, y, size, color);
			case CLIENT -> sliders(graphics, x, y, size, color);
		}
	}

	/** Four tiles, the HUD icon. */
	public static void layout(GuiGraphics graphics, float x, float y, float size, int color) {
		float gap = Math.max(1, size * 0.14f);
		float tile = (size - gap) / 2;
		float radius = tile * 0.3f;
		Render2D.roundedRect(graphics, x, y, tile, tile, radius, color);
		Render2D.roundedRect(graphics, x + tile + gap, y, tile, tile, radius, color);
		Render2D.roundedRect(graphics, x, y + tile + gap, tile, tile, radius, color);
		Render2D.roundedRect(graphics, x + tile + gap, y + tile + gap, tile, tile, radius, color);
	}

	public static void player(GuiGraphics graphics, float x, float y, float size, int color) {
		float head = size * 0.42f;
		Render2D.circle(graphics, x + size / 2, y + head / 2, head / 2, color);
		Render2D.roundedRect(graphics, x + size * 0.1f, y + size * 0.52f, size * 0.8f, size * 0.48f, size * 0.24f, color);
	}

	/** Two slider tracks with knobs, the settings icon. */
	public static void sliders(GuiGraphics graphics, float x, float y, float size, int color) {
		float track = Math.max(1, size * 0.14f);
		float knob = size * 0.2f;
		float top = y + size * 0.28f;
		float bottom = y + size * 0.72f;
		Render2D.roundedRect(graphics, x, top - track / 2, size, track, track / 2, color);
		Render2D.roundedRect(graphics, x, bottom - track / 2, size, track, track / 2, color);
		Render2D.circle(graphics, x + size * 0.7f, top, knob, color);
		Render2D.circle(graphics, x + size * 0.3f, bottom, knob, color);
	}

	/** A magnifying glass: ring plus a short handle of dots. */
	public static void search(GuiGraphics graphics, float x, float y, float size, int color) {
		float ring = size * 0.72f;
		Render2D.roundedOutline(graphics, x, y, ring, ring, ring / 2, color);
		for (int i = 0; i < 3; i++) {
			float t = ring * 0.85f + i * size * 0.09f;
			Render2D.circle(graphics, x + t, y + t, Math.max(0.6f, size * 0.07f), color);
		}
	}

	/** The Voltliz bolt, tinted with {@code color}. */
	public static void bolt(GuiGraphics graphics, float x, float y, float height, int color) {
		float width = height * 160 / 262f;
		Render2D.image(graphics, "textures/gui/bolt.png", x, y, width, height, color);
	}

	/** The full app icon: dark tile, gradient border and glowing bolt. */
	public static void logo(GuiGraphics graphics, float x, float y, float size) {
		Render2D.image(graphics, "textures/gui/logo.png", x, y, size, size, 0xFFFFFFFF);
	}
}
