package dev.voltliz.client.module.render;

import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.NumberSetting;
import dev.voltliz.client.util.ColorUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

/** A glowing wireframe of each target's hitbox, visible through walls. */
public class Esp3DModule extends EspModule {
	/** Corner index pairs for the 12 edges; bit 1 of an index is the top (max Y) face. */
	private static final int[][] BOTTOM_EDGES = {{0, 1}, {1, 5}, {5, 4}, {4, 0}};
	private static final int[][] TOP_EDGES = {{2, 3}, {3, 7}, {7, 6}, {6, 2}};
	private static final int[][] SIDE_EDGES = {{0, 2}, {1, 3}, {4, 6}, {5, 7}};

	private final NumberSetting lineWidth = add(new NumberSetting("Line Width", 1.2, 0.5, 3, 0.1));
	private final BooleanSetting glow = add(new BooleanSetting("Glow", true));

	public Esp3DModule() {
		super("3D ESP", "Draws a glowing 3D box around targets.");
	}

	@Override
	protected void renderEntity(GuiGraphics graphics, LivingEntity entity, LocalPlayer player, float partialTick) {
		float[][] points = corners(box(entity, partialTick));
		if (points == null) {
			return;
		}
		int top = topColor(entity);
		int bottom = bottomColor(entity);
		float width = lineWidth.getFloat();

		if (glow.get()) {
			drawEdges(graphics, points, ColorUtil.withAlpha(top, 0x30), ColorUtil.withAlpha(bottom, 0x30), width + 2.5f);
		}
		drawEdges(graphics, points, top, bottom, width);
	}

	private static void drawEdges(GuiGraphics graphics, float[][] points, int top, int bottom, float width) {
		for (int[] edge : BOTTOM_EDGES) {
			line(graphics, points[edge[0]], points[edge[1]], bottom, width);
		}
		for (int[] edge : TOP_EDGES) {
			line(graphics, points[edge[0]], points[edge[1]], top, width);
		}
		// Side edges blend from the bottom color to the top one in two halves.
		int middle = ColorUtil.lerp(bottom, top, 0.5f);
		for (int[] edge : SIDE_EDGES) {
			float[] a = points[edge[0]];
			float[] b = points[edge[1]];
			float[] mid = {(a[0] + b[0]) / 2, (a[1] + b[1]) / 2};
			line(graphics, a, mid, ColorUtil.lerp(bottom, middle, 0.5f), width);
			line(graphics, mid, b, ColorUtil.lerp(middle, top, 0.5f), width);
		}
	}

	private static void line(GuiGraphics graphics, float[] a, float[] b, int color, float width) {
		Render2D.line(graphics, a[0], a[1], b[0], b[1], width, color);
	}
}
