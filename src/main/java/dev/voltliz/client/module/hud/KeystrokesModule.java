package dev.voltliz.client.module.hud;

import dev.voltliz.client.module.HudModule;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ColorSetting;
import dev.voltliz.client.util.Animator;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.CpsTracker;
import dev.voltliz.client.util.RenderUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class KeystrokesModule extends HudModule {
	private static final int KEY = 22;
	private static final int GAP = 2;
	private static final int TOTAL_WIDTH = KEY * 3 + GAP * 2;

	private final BooleanSetting showMouse = add(new BooleanSetting("Mouse Buttons", true));
	private final BooleanSetting showCps = add(new BooleanSetting("Show CPS", true));
	private final BooleanSetting showSpace = add(new BooleanSetting("Space Bar", true));
	private final ColorSetting pressedColor = add(new ColorSetting("Pressed Color", 0xFFFFFFFF));
	private final ColorSetting idleColor = add(new ColorSetting("Idle Color", 0x80000000));

	private final Animator animator = new Animator();
	// W, A, S, D, LMB, RMB, Space
	private final float[] pressAnimations = new float[7];

	public KeystrokesModule() {
		super("Keystrokes", "Shows your movement keys and mouse buttons.", 4, 140);
		setEnabled(true);
	}

	@Override
	protected void renderContent(GuiGraphics graphics) {
		animator.tick();
		Options options = mc().options;

		key(graphics, 0, options.keyUp, null, KEY + GAP, 0, KEY, KEY);
		key(graphics, 1, options.keyLeft, null, 0, KEY + GAP, KEY, KEY);
		key(graphics, 2, options.keyDown, null, KEY + GAP, KEY + GAP, KEY, KEY);
		key(graphics, 3, options.keyRight, null, (KEY + GAP) * 2, KEY + GAP, KEY, KEY);

		int y = (KEY + GAP) * 2;
		if (showMouse.get()) {
			int half = (TOTAL_WIDTH - GAP) / 2;
			int mouseHeight = showCps.get() ? KEY : 16;
			key(graphics, 4, options.keyAttack, showCps.get() ? CpsTracker.getLeft() + " CPS" : null, 0, y, half, mouseHeight);
			key(graphics, 5, options.keyUse, showCps.get() ? CpsTracker.getRight() + " CPS" : null, half + GAP, y, TOTAL_WIDTH - half - GAP, mouseHeight);
			y += mouseHeight + GAP;
		}

		if (showSpace.get()) {
			int spaceHeight = 10;
			float progress = animate(6, options.keyJump.isDown());
			RenderUtil.roundedRect(graphics, 0, y, TOTAL_WIDTH, spaceHeight, ColorUtil.lerp(idleColor.getColor(), pressedColor.getColor(), progress));
			int barColor = ColorUtil.lerp(0xFFFFFFFF, 0xFF000000, progress);
			graphics.fill(TOTAL_WIDTH / 2 - 12, y + 4, TOTAL_WIDTH / 2 + 12, y + 5, barColor);
			y += spaceHeight + GAP;
		}

		width = TOTAL_WIDTH;
		height = y - GAP;
	}

	private void key(GuiGraphics graphics, int index, KeyMapping mapping, String subtitle, int x, int y, int w, int h) {
		Font font = mc().font;
		float progress = animate(index, mapping.isDown());

		RenderUtil.roundedRect(graphics, x, y, w, h, ColorUtil.lerp(idleColor.getColor(), pressedColor.getColor(), progress));

		int textColor = ColorUtil.lerp(0xFFFFFFFF, 0xFF000000, progress);
		String label = mapping.getTranslatedKeyMessage().getString();
		if (index == 4) {
			label = "LMB";
		} else if (index == 5) {
			label = "RMB";
		} else if (font.width(label) > w - 4) {
			label = font.plainSubstrByWidth(label, w - 4);
		}

		if (subtitle == null) {
			graphics.drawString(font, label, x + (w - font.width(label)) / 2, y + (h - 8) / 2, textColor, false);
			return;
		}

		graphics.drawString(font, label, x + (w - font.width(label)) / 2, y + 3, textColor, false);
		graphics.pose().pushMatrix();
		graphics.pose().translate(x + w / 2f, y + 13);
		graphics.pose().scale(0.6f, 0.6f);
		graphics.drawString(font, subtitle, -font.width(subtitle) / 2, 0, textColor, false);
		graphics.pose().popMatrix();
	}

	private float animate(int index, boolean pressed) {
		pressAnimations[index] = animator.approach(pressAnimations[index], pressed ? 1 : 0, 25);
		return pressAnimations[index];
	}
}
