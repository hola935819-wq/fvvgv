package dev.voltliz.client.util;

public final class ColorUtil {
	private ColorUtil() {
	}

	public static int withAlpha(int color, int alpha) {
		return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
	}

	public static int lerp(int from, int to, float t) {
		int a = lerpChannel(from >>> 24, to >>> 24, t);
		int r = lerpChannel(from >> 16 & 0xFF, to >> 16 & 0xFF, t);
		int g = lerpChannel(from >> 8 & 0xFF, to >> 8 & 0xFF, t);
		int b = lerpChannel(from & 0xFF, to & 0xFF, t);
		return a << 24 | r << 16 | g << 8 | b;
	}

	private static int lerpChannel(int from, int to, float t) {
		return Math.round(from + (to - from) * t);
	}

	public static int rainbow(long offset, float saturation, float brightness) {
		long period = 4000L;
		float hue = Math.floorMod(System.currentTimeMillis() + offset, period) / (float) period;
		return hsb(hue, saturation, brightness, 255);
	}

	/** HSB to ARGB, same math as {@code java.awt.Color.HSBtoRGB} without loading AWT. */
	public static int hsb(float hue, float saturation, float brightness, int alpha) {
		int r = 0;
		int g = 0;
		int b = 0;
		if (saturation == 0) {
			r = g = b = (int) (brightness * 255.0f + 0.5f);
		} else {
			float h = (hue - (float) Math.floor(hue)) * 6.0f;
			float f = h - (float) Math.floor(h);
			float p = brightness * (1.0f - saturation);
			float q = brightness * (1.0f - saturation * f);
			float t = brightness * (1.0f - (saturation * (1.0f - f)));
			switch ((int) h) {
				case 0 -> {
					r = (int) (brightness * 255.0f + 0.5f);
					g = (int) (t * 255.0f + 0.5f);
					b = (int) (p * 255.0f + 0.5f);
				}
				case 1 -> {
					r = (int) (q * 255.0f + 0.5f);
					g = (int) (brightness * 255.0f + 0.5f);
					b = (int) (p * 255.0f + 0.5f);
				}
				case 2 -> {
					r = (int) (p * 255.0f + 0.5f);
					g = (int) (brightness * 255.0f + 0.5f);
					b = (int) (t * 255.0f + 0.5f);
				}
				case 3 -> {
					r = (int) (p * 255.0f + 0.5f);
					g = (int) (q * 255.0f + 0.5f);
					b = (int) (brightness * 255.0f + 0.5f);
				}
				case 4 -> {
					r = (int) (t * 255.0f + 0.5f);
					g = (int) (p * 255.0f + 0.5f);
					b = (int) (brightness * 255.0f + 0.5f);
				}
				default -> {
					r = (int) (brightness * 255.0f + 0.5f);
					g = (int) (p * 255.0f + 0.5f);
					b = (int) (q * 255.0f + 0.5f);
				}
			}
		}
		return (alpha & 0xFF) << 24 | r << 16 | g << 8 | b;
	}

	/** ARGB to {hue, saturation, brightness}, each from 0 to 1. */
	public static float[] toHsb(int color) {
		int r = color >> 16 & 0xFF;
		int g = color >> 8 & 0xFF;
		int b = color & 0xFF;
		int max = Math.max(r, Math.max(g, b));
		int min = Math.min(r, Math.min(g, b));
		float brightness = max / 255.0f;
		float saturation = max != 0 ? (float) (max - min) / max : 0;
		float hue = 0;
		if (saturation != 0) {
			float redc = (float) (max - r) / (max - min);
			float greenc = (float) (max - g) / (max - min);
			float bluec = (float) (max - b) / (max - min);
			if (r == max) {
				hue = bluec - greenc;
			} else if (g == max) {
				hue = 2.0f + redc - bluec;
			} else {
				hue = 4.0f + greenc - redc;
			}
			hue /= 6.0f;
			if (hue < 0) {
				hue += 1.0f;
			}
		}
		return new float[] {hue, saturation, brightness};
	}
}
