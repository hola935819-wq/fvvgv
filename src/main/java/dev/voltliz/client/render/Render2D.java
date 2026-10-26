package dev.voltliz.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.util.ColorUtil;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

/**
 * Voltliz's own 2D shapes: anti-aliased rounded rectangles, gradients, circles, soft shadows and
 * smoothly scaled images.
 *
 * <p>Corners and images are baked into textures at the window's real pixel size, so each texel lands
 * on exactly one screen pixel and edges stay smooth at every GUI scale.
 */
public final class Render2D {
	private static final Map<Integer, Identifier> CIRCLES = new HashMap<>();
	private static final Map<Integer, Identifier> RINGS = new HashMap<>();
	private static final Map<String, Identifier> IMAGES = new HashMap<>();

	private static float alpha = 1;

	private Render2D() {
	}

	/** Multiplies the alpha of everything drawn through Render2D and VText; used for fades. */
	public static void setAlpha(float value) {
		alpha = Math.max(0, Math.min(1, value));
	}

	public static float getAlpha() {
		return alpha;
	}

	static int fade(int color) {
		if (alpha >= 1) {
			return color;
		}
		return ColorUtil.withAlpha(color, Math.round((color >>> 24) * alpha));
	}

	public static void rect(GuiGraphics graphics, float x, float y, float width, float height, int color) {
		color = fade(color);
		if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
			return;
		}
		graphics.fill(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), color);
	}

	public static void gradientRect(GuiGraphics graphics, float x, float y, float width, float height, int top, int bottom) {
		graphics.fillGradient(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), fade(top), fade(bottom));
	}

	public static void roundedRect(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color) {
		color = fade(color);
		if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
			return;
		}
		int x1 = Math.round(x);
		int y1 = Math.round(y);
		int x2 = Math.round(x + width);
		int y2 = Math.round(y + height);
		int r = clampRadius(radius, x1, y1, x2, y2);
		if (r <= 0) {
			graphics.fill(x1, y1, x2, y2, color);
			return;
		}

		graphics.fill(x1 + r, y1, x2 - r, y2, color);
		graphics.fill(x1, y1 + r, x1 + r, y2 - r, color);
		graphics.fill(x2 - r, y1 + r, x2, y2 - r, color);
		corners(graphics, circle(r), x1, y1, x2, y2, r, color, color, color, color);
	}

	/** A rounded rectangle whose color goes from {@code left} to {@code right}. */
	public static void horizontalGradient(GuiGraphics graphics, float x, float y, float width, float height, float radius, int left, int right) {
		left = fade(left);
		right = fade(right);
		int x1 = Math.round(x);
		int y1 = Math.round(y);
		int x2 = Math.round(x + width);
		int y2 = Math.round(y + height);
		if (x2 <= x1 || y2 <= y1) {
			return;
		}
		int r = clampRadius(radius, x1, y1, x2, y2);

		// One-pixel columns between the corners, each with its interpolated color.
		int span = Math.max(1, x2 - x1 - 1);
		for (int column = x1 + r; column < x2 - r; column++) {
			graphics.fill(column, y1, column + 1, y2, ColorUtil.lerp(left, right, (column - x1) / (float) span));
		}
		if (r > 0) {
			graphics.fill(x1, y1 + r, x1 + r, y2 - r, left);
			graphics.fill(x2 - r, y1 + r, x2, y2 - r, right);
			corners(graphics, circle(r), x1, y1, x2, y2, r, left, right, left, right);
		}
	}

	/** A rounded rectangle whose color goes from {@code top} to {@code bottom}. */
	public static void verticalGradient(GuiGraphics graphics, float x, float y, float width, float height, float radius, int top, int bottom) {
		top = fade(top);
		bottom = fade(bottom);
		int x1 = Math.round(x);
		int y1 = Math.round(y);
		int x2 = Math.round(x + width);
		int y2 = Math.round(y + height);
		if (x2 <= x1 || y2 <= y1) {
			return;
		}
		int r = clampRadius(radius, x1, y1, x2, y2);
		float span = Math.max(1, y2 - y1 - 1);
		int innerTop = ColorUtil.lerp(top, bottom, r / span);
		int innerBottom = ColorUtil.lerp(top, bottom, (y2 - y1 - r) / span);

		graphics.fill(x1 + r, y1, x2 - r, y1 + r, innerTop);
		graphics.fillGradient(x1, y1 + r, x2, y2 - r, innerTop, innerBottom);
		graphics.fill(x1 + r, y2 - r, x2 - r, y2, innerBottom);
		if (r > 0) {
			corners(graphics, circle(r), x1, y1, x2, y2, r, top, top, bottom, bottom);
		}
	}

	/** A one pixel rounded border. */
	public static void roundedOutline(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color) {
		color = fade(color);
		int x1 = Math.round(x);
		int y1 = Math.round(y);
		int x2 = Math.round(x + width);
		int y2 = Math.round(y + height);
		int r = Math.max(1, clampRadius(radius, x1, y1, x2, y2));

		graphics.fill(x1 + r, y1, x2 - r, y1 + 1, color);
		graphics.fill(x1 + r, y2 - 1, x2 - r, y2, color);
		graphics.fill(x1, y1 + r, x1 + 1, y2 - r, color);
		graphics.fill(x2 - 1, y1 + r, x2, y2 - r, color);

		int size = texturePixels(r);
		Identifier ring = RINGS.computeIfAbsent(size, Render2D::createRing);
		corners(graphics, ring, x1, y1, x2, y2, r, color, color, color, color);
	}

	public static void circle(GuiGraphics graphics, float centerX, float centerY, float radius, int color) {
		roundedRect(graphics, centerX - radius, centerY - radius, radius * 2, radius * 2, radius, color);
	}

	/** A soft drop shadow made of stacked rounded rectangles that fade out. */
	public static void shadow(GuiGraphics graphics, float x, float y, float width, float height, float radius, int size, int strength) {
		shadow(graphics, x, y, width, height, radius, size, strength, 0);
	}

	/** A soft glow or shadow; {@code color} is the RGB of the glow, black for a shadow. */
	public static void shadow(GuiGraphics graphics, float x, float y, float width, float height, float radius, int size, int strength, int color) {
		for (int i = size; i >= 1; i--) {
			float t = 1 - (i - 1) / (float) size;
			int layerAlpha = Math.round(strength * t * t / size * 1.6f);
			roundedRect(graphics, x - i, y - i + 1, width + i * 2, height + i * 2, radius + i, ColorUtil.withAlpha(color, Math.min(255, layerAlpha)));
		}
	}

	/**
	 * Draws a PNG from the mod's assets, resampled with area averaging to the exact on-screen pixel
	 * size so it stays sharp instead of the jagged look of nearest-neighbour scaling.
	 */
	public static void image(GuiGraphics graphics, String path, float x, float y, float width, float height, int tint) {
		int x1 = Math.round(x);
		int y1 = Math.round(y);
		int w = Math.max(1, Math.round(width));
		int h = Math.max(1, Math.round(height));
		int scale = guiScale();
		Identifier texture = IMAGES.computeIfAbsent(path + "@" + w * scale + "x" + h * scale, key -> createImage(path, w * scale, h * scale));
		if (texture != null) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x1, y1, 0, 0, w, h, w * scale, h * scale, w * scale, h * scale, fade(tint));
		}
	}

	private static int clampRadius(float radius, int x1, int y1, int x2, int y2) {
		return Math.max(0, Math.min(Math.round(radius), Math.min((x2 - x1) / 2, (y2 - y1) / 2)));
	}

	/** Draws the four quarter circles of a rounded shape, each with its own color. */
	private static void corners(GuiGraphics graphics, Identifier texture, int x1, int y1, int x2, int y2, int r,
			int topLeft, int topRight, int bottomLeft, int bottomRight) {
		int size = texturePixels(r);
		int half = size / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x1, y1, 0, 0, r, r, half, half, size, size, topLeft);
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x2 - r, y1, half, 0, r, r, half, half, size, size, topRight);
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x1, y2 - r, 0, half, r, r, half, half, size, size, bottomLeft);
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x2 - r, y2 - r, half, half, r, r, half, half, size, size, bottomRight);
	}

	private static int guiScale() {
		return Math.max(1, (int) Math.ceil(Minecraft.getInstance().getWindow().getGuiScale()));
	}

	private static int texturePixels(int radius) {
		return radius * 2 * guiScale();
	}

	private static Identifier circle(int radius) {
		return CIRCLES.computeIfAbsent(texturePixels(radius), size -> createShape(size, "circle", 0));
	}

	/** A circle outline whose thickness is one GUI pixel at the current scale. */
	private static Identifier createRing(int size) {
		return createShape(size, "ring", guiScale());
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
				image.setPixel(px, py, Math.round(inside / 16f * 255) << 24 | 0xFFFFFF);
			}
		}
		return register(kind + "_" + size, image);
	}

	private static Identifier createImage(String path, int width, int height) {
		Identifier source = Identifier.fromNamespaceAndPath(VoltlizClient.MOD_ID, path);
		Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(source);
		if (resource.isEmpty()) {
			VoltlizClient.LOGGER.warn("Missing GUI image {}", source);
			return null;
		}

		try (InputStream stream = resource.get().open(); NativeImage original = NativeImage.read(stream)) {
			NativeImage scaled = new NativeImage(width, height, true);
			float stepX = original.getWidth() / (float) width;
			float stepY = original.getHeight() / (float) height;
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					scaled.setPixel(x, y, average(original, x * stepX, y * stepY, stepX, stepY));
				}
			}
			return register("image/" + path.replace('/', '_') + "_" + width + "x" + height, scaled);
		} catch (Exception e) {
			VoltlizClient.LOGGER.error("Could not load GUI image {}", source, e);
			return null;
		}
	}

	/** Premultiplied average of the source pixels covered by one destination pixel. */
	private static int average(NativeImage image, float startX, float startY, float stepX, float stepY) {
		int x0 = (int) startX;
		int y0 = (int) startY;
		int x1 = Math.min(image.getWidth(), Math.max(x0 + 1, (int) Math.ceil(startX + stepX)));
		int y1 = Math.min(image.getHeight(), Math.max(y0 + 1, (int) Math.ceil(startY + stepY)));
		long a = 0;
		long r = 0;
		long g = 0;
		long b = 0;
		int count = 0;
		for (int x = x0; x < x1; x++) {
			for (int y = y0; y < y1; y++) {
				int pixel = image.getPixel(x, y);
				int pa = pixel >>> 24;
				a += pa;
				r += (pixel >> 16 & 0xFF) * pa;
				g += (pixel >> 8 & 0xFF) * pa;
				b += (pixel & 0xFF) * pa;
				count++;
			}
		}
		if (a == 0) {
			return 0;
		}
		return (int) (a / count) << 24 | (int) (r / a) << 16 | (int) (g / a) << 8 | (int) (b / a);
	}

	private static Identifier register(String name, NativeImage image) {
		Identifier id = Identifier.fromNamespaceAndPath(VoltlizClient.MOD_ID, "generated/" + name);
		Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> "voltliz " + name, image));
		return id;
	}
}
