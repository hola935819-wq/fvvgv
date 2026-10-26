package dev.voltliz.client.module.render;

import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ModeSetting;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

/** A flat box around each target with a health bar, name tag and distance. */
public class Esp2DModule extends EspModule {
	private static final int OUTLINE = 0xB0000000;

	private final ModeSetting style = add(new ModeSetting("Style", "Corners", "Corners", "Box"));
	private final BooleanSetting fill = add(new BooleanSetting("Fill", true));
	private final BooleanSetting healthBar = add(new BooleanSetting("Health Bar", true));
	private final BooleanSetting nameTag = add(new BooleanSetting("Name Tag", true));
	private final BooleanSetting distance = add(new BooleanSetting("Distance", true));

	public Esp2DModule() {
		super("2D ESP", "Draws a clean 2D box, health bar and name on targets.");
	}

	@Override
	protected void renderEntity(GuiGraphics graphics, LivingEntity entity, LocalPlayer player, float partialTick) {
		float[][] points = corners(box(entity, partialTick));
		if (points == null) {
			return;
		}
		float left = Float.MAX_VALUE;
		float top = Float.MAX_VALUE;
		float right = -Float.MAX_VALUE;
		float bottom = -Float.MAX_VALUE;
		for (float[] point : points) {
			left = Math.min(left, point[0]);
			top = Math.min(top, point[1]);
			right = Math.max(right, point[0]);
			bottom = Math.max(bottom, point[1]);
		}
		float width = right - left;
		float height = bottom - top;
		if (width < 2 || height < 2 || width > 4000 || height > 4000) {
			return;
		}

		int topColor = topColor(entity);
		int bottomColor = bottomColor(entity);

		if (fill.get()) {
			Render2D.gradientRect(graphics, left, top, width, height, ColorUtil.withAlpha(topColor, 0x22), ColorUtil.withAlpha(bottomColor, 0x10));
		}
		if (style.is("Box")) {
			drawBox(graphics, left, top, width, height, topColor, bottomColor);
		} else {
			drawCorners(graphics, left, top, width, height, topColor, bottomColor);
		}

		if (healthBar.get()) {
			float fraction = healthFraction(entity);
			float barX = left - 4;
			Render2D.rect(graphics, barX - 1, top - 1, 3, height + 2, OUTLINE);
			float filled = height * fraction;
			Render2D.gradientRect(graphics, barX, bottom - filled, 1, filled, healthColor(fraction), healthColor(fraction * 0.6f));
		}

		float centerX = left + width / 2;
		if (nameTag.get()) {
			String name = entity.getName().getString();
			String health = String.format("%.1f", entity.getHealth() + entity.getAbsorptionAmount());
			float scale = 0.8f;
			float nameWidth = VText.width(name, true) * scale;
			float healthWidth = VText.width(health, true) * scale;
			float pillWidth = nameWidth + healthWidth + 16;
			float pillHeight = 12;
			float pillX = centerX - pillWidth / 2;
			float pillY = top - pillHeight - 3;
			Render2D.roundedRect(graphics, pillX, pillY, pillWidth, pillHeight, 6, Theme.HUD_BACKGROUND);
			Render2D.circle(graphics, pillX + 5, pillY + pillHeight / 2, 1.6f, topColor);
			VText.drawScaled(graphics, name, pillX + 9, pillY + 2.5f, Theme.TEXT, true, scale);
			VText.drawScaled(graphics, health, pillX + 12 + nameWidth, pillY + 2.5f, healthColor(healthFraction(entity)), true, scale);
		}
		if (distance.get()) {
			String text = Math.round(player.distanceTo(entity)) + "m";
			float scale = 0.7f;
			VText.drawScaled(graphics, text, centerX - VText.width(text) * scale / 2, bottom + 3, ColorUtil.withAlpha(Theme.TEXT, 0xC0), false, scale);
		}
	}

	private static void drawBox(GuiGraphics graphics, float x, float y, float width, float height, int top, int bottom) {
		// Dark outline first for contrast on bright backgrounds, then the gradient frame.
		Render2D.rect(graphics, x - 1, y - 1, width + 2, 3, OUTLINE);
		Render2D.rect(graphics, x - 1, y + height - 2, width + 2, 3, OUTLINE);
		Render2D.rect(graphics, x - 1, y - 1, 3, height + 2, OUTLINE);
		Render2D.rect(graphics, x + width - 2, y - 1, 3, height + 2, OUTLINE);
		Render2D.rect(graphics, x, y, width, 1, top);
		Render2D.rect(graphics, x, y + height - 1, width, 1, bottom);
		Render2D.gradientRect(graphics, x, y, 1, height, top, bottom);
		Render2D.gradientRect(graphics, x + width - 1, y, 1, height, top, bottom);
	}

	private static void drawCorners(GuiGraphics graphics, float x, float y, float width, float height, int top, int bottom) {
		float lengthX = Math.max(3, width * 0.28f);
		float lengthY = Math.max(3, height * 0.22f);
		float right = x + width;
		float lower = y + height;
		// Outline.
		Render2D.rect(graphics, x - 1, y - 1, lengthX + 1, 3, OUTLINE);
		Render2D.rect(graphics, x - 1, y - 1, 3, lengthY + 1, OUTLINE);
		Render2D.rect(graphics, right - lengthX, y - 1, lengthX + 1, 3, OUTLINE);
		Render2D.rect(graphics, right - 2, y - 1, 3, lengthY + 1, OUTLINE);
		Render2D.rect(graphics, x - 1, lower - 2, lengthX + 1, 3, OUTLINE);
		Render2D.rect(graphics, x - 1, lower - lengthY, 3, lengthY + 1, OUTLINE);
		Render2D.rect(graphics, right - lengthX, lower - 2, lengthX + 1, 3, OUTLINE);
		Render2D.rect(graphics, right - 2, lower - lengthY, 3, lengthY + 1, OUTLINE);
		// Corners.
		Render2D.rect(graphics, x, y, lengthX, 1, top);
		Render2D.rect(graphics, x, y, 1, lengthY, top);
		Render2D.rect(graphics, right - lengthX, y, lengthX, 1, top);
		Render2D.rect(graphics, right - 1, y, 1, lengthY, top);
		Render2D.rect(graphics, x, lower - 1, lengthX, 1, bottom);
		Render2D.rect(graphics, x, lower - lengthY, 1, lengthY, bottom);
		Render2D.rect(graphics, right - lengthX, lower - 1, lengthX, 1, bottom);
		Render2D.rect(graphics, right - 1, lower - lengthY, 1, lengthY, bottom);
	}
}
