package dev.voltliz.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.util.ColorUtil;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * Voltliz's own 2D shapes: anti-aliased rounded rectangles, circles and soft shadows.
 *
 * <p>Corners come from circle textures generated at runtime at the window's real pixel size, so each
 * texel lands on exactly one screen pixel and the edges stay smooth at every GUI scale.
 */
public final class Render2D {
	private static final Map<Integer, Identifier> CIRCLES = new HashMap<>();
	private static final Map<Integer, Identifier> RINGS = new HashMap<>();

	private Render2D() {
	}

	public static void rect(GuiGraphics graphics, float x, float y, float width, float height, int color) {
		if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
			return;
		}
		int x1 = Math.round(x);
		int y1 = Math.round(y);
		graphics.fill(x1, y1, Math.round(x + width), Math.round(y + height), color);
	}

	public static void roundedRect(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color) {
		if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
			return;
		}
		int x1 = Math.round(x);
		int y1 = Math.round(y);
		int x2 = Math.round(x + width);
		int y2 = Math.round(y + height);
		int r = Math.min(Math.round(radius), Math.min((x2 - x1) / 2, (y2 - y1) / 2));
		if (r <= 0) {
			graphics.fill(x1, y1, x2, y2, color);
			return;
		}

		graphics.fill(x1 + r, y1, x2 - r, y2, color);
		graphics.fill(x1, y1 + r, x1 + r, y2 - r, color);
		graphics.fill(x2 - r, y1 + r, x2, y2 - r, color);

		Identifier circle = circle(r);
		int size = texturePixels(r);
		int half = size / 2;
		corner(graphics, circle, x1, y1, r, 0, 0, half, size, color);
		corner(graphics, circle, x2 - r, y1, r, half, 0, half, size, color);
		corner(graphics, circle, x1, y2 - r, r, 0, half, half, size, color);
		corner(graphics, circle, x2 - r, y2 - r, r, half, half, half, size, color);
	}

	/** A one pixel rounded border. */
	public static void roundedOutline(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color) {
		int x1 = Math.round(x);
		int y1 = Math.round(y);
		int x2 = Math.round(x + width);
		int y2 = Math.round(y + height);
		int r = Math.max(1, Math.min(Math.round(radius), Math.min((x2 - x1) / 2, (y2 - y1) / 2)));

		graphics.fill(x1 + r, y1, x2 - r, y1 + 1, color);
		graphics.fill(x1 + r, y2 - 1, x2 - r, y2, color);
		graphics.fill(x1, y1 + r, x1 + 1, y2 - r, color);
		graphics.fill(x2 - 1, y1 + r, x2, y2 - r, color);

		int size = texturePixels(r);
		Identifier ring = RINGS.computeIfAbsent(size, Render2D::createRing);
		int half = size / 2;
		corner(graphics, ring, x1, y1, r, 0, 0, half, size, color);
		corner(graphics, ring, x2 - r, y1, r, half, 0, half, size, color);
		corner(graphics, ring, x1, y2 - r, r, 0, half, half, size, color);
		corner(graphics, ring, x2 - r, y2 - r, r, half, half, half, size, color);
	}

	public static void circle(GuiGraphics graphics, float centerX, float centerY, float radius, int color) {
		roundedRect(graphics, centerX - radius, centerY - radius, radius * 2, radius * 2, radius, color);
	}

	/** A soft drop shadow made of stacked rounded rectangles that fade out. */
	public static void shadow(GuiGraphics graphics, float x, float y, float width, float height, float radius, int size, int alpha) {
		for (int i = size; i >= 1; i--) {
			float t = 1 - (i - 1) / (float) size;
			int layerAlpha = Math.round(alpha * t * t / size * 1.6f);
			roundedRect(graphics, x - i, y - i + 1, width + i * 2, height + i * 2, radius + i, ColorUtil.withAlpha(0, Math.min(255, layerAlpha)));
		}
	}

	/** A vertical gradient with rounded corners: top and bottom halves take the end colors. */
	public static void roundedGradient(GuiGraphics graphics, float x, float y, float width, float height, float radius, int top, int bottom) {
		int r = Math.round(radius);
		roundedRect(graphics, x, y, width, r * 2, radius, top);
		roundedRect(graphics, x, y + height - r * 2, width, r * 2, radius, bottom);
		graphics.fillGradient(Math.round(x), Math.round(y + r), Math.round(x + width), Math.round(y + height - r), top, bottom);
	}

	private static void corner(GuiGraphics graphics, Identifier texture, int x, int y, int r, int u, int v, int region, int size, int color) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, r, r, region, region, size, size, color);
	}

	private static int texturePixels(int radius) {
		int scale = Math.max(1, (int) Math.ceil(Minecraft.getInstance().getWindow().getGuiScale()));
		return radius * 2 * scale;
	}

	private static Identifier circle(int radius) {
		int size = texturePixels(radius);
		return CIRCLES.computeIfAbsent(size, Render2D::createCircle);
	}

	private static Identifier createCircle(int size) {
		return createShape(size, "circle", 0);
	}

	/** A circle outline whose thickness is one GUI pixel at the current scale. */
	private static Identifier createRing(int size) {
		int scale = Math.max(1, (int) Math.ceil(Minecraft.getInstance().getWindow().getGuiScale()));
		return createShape(size, "ring", scale);
	}

	private static Identifier createShape(int size, String kind, int ringThickness) {
		NativeImage image = new NativeImage(size, size, true);
		float center = size / 2f;
		float outer = size / 2f;
		float inner = ringThickness > 0 ? outer - ringThickness : -1;
		// 4x4 supersampling per pixel for a smooth edge.
		for (int px = 0; px < size; px++) {
			for (int py = 0; py < size; py++) {
				int inside = 0;
				for (int sx = 0; sx < 4; sx++) {
					for (int sy = 0; sy < 4; sy++) {
						float dx = px + (sx + 0.5f) / 4f - center;
						float dy = py + (sy + 0.5f) / 4f - center;
						float distance = dx * dx + dy * dy;
						if (distance <= outer * outer && (inner < 0 || distance >= inner * inner)) {
							inside++;
						}
					}
				}
				int alpha = Math.round(inside / 16f * 255);
				image.setPixel(px, py, alpha << 24 | 0xFFFFFF);
			}
		}

		Identifier id = Identifier.fromNamespaceAndPath(VoltlizClient.MOD_ID, "generated/" + kind + "_" + size);
		Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> "voltliz " + kind + " " + size, image));
		return id;
	}
}
