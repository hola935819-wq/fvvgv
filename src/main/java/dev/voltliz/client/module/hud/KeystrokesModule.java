package dev.voltliz.client.module.hud;

import dev.voltliz.client.module.HudModule;
import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ColorSetting;
import dev.voltliz.client.util.Animator;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.CpsTracker;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;

public class KeystrokesModule extends HudModule {
	private static final int KEY = 22;
	private static final int GAP = 3;
	private static final int TOTAL_WIDTH = KEY * 3 + GAP * 2;

	private final BooleanSetting showMouse = add(new BooleanSetting("Mouse Buttons", true));
	private final BooleanSetting showCps = add(new BooleanSetting("Show CPS", true));
	private final BooleanSetting showSpace = add(new BooleanSetting("Space Bar", true));
	private final BooleanSetting accentPressed = add(new BooleanSetting("Accent When Pressed", true));
	private final ColorSetting pressedColor = add(new ColorSetting("Pressed Color", 0xFFFFFFFF)).visibleWhen(() -> !accentPressed.get());

	private final Animator animator = new Animator();
	// W, A, S, D, LMB, RMB, Space
	private final float[] pressAnimations = new float[7];

	public KeystrokesModule() {
		super("Keystrokes", "Shows your movement keys and mouse buttons.", 8, 172);
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
			int mouseHeight = showCps.get() ? KEY + 2 : 18;
			key(graphics, 4, options.keyAttack, showCps.get() ? CpsTracker.getLeft() + " CPS" : null, 0, y, half, mouseHeight);
			key(graphics, 5, options.keyUse, showCps.get() ? CpsTracker.getRight() + " CPS" : null, half + GAP, y, TOTAL_WIDTH - half - GAP, mouseHeight);
			y += mouseHeight + GAP;
		}

		if (showSpace.get()) {
			int spaceHeight = 11;
			float progress = animate(6, options.keyJump.isDown());
			Render2D.roundedRect(graphics, 0, y, TOTAL_WIDTH, spaceHeight, 4, ColorUtil.lerp(Theme.HUD_BACKGROUND, pressed(), progress));
			Render2D.roundedRect(graphics, TOTAL_WIDTH / 2f - 12, y + 4.5f, 24, 2, 1, ColorUtil.lerp(Theme.TEXT, pressedText(), progress));
			y += spaceHeight + GAP;
		}

		width = TOTAL_WIDTH;
		height = y - GAP;
	}

	private int pressed() {
		return accentPressed.get() ? Theme.accent() : pressedColor.getColor();
	}

	private int pressedText() {
		return accentPressed.get() ? 0xFFFFFFFF : 0xFF0E1016;
	}

	private void key(GuiGraphics graphics, int index, KeyMapping mapping, String subtitle, int x, int y, int w, int h) {
		float progress = animate(index, mapping.isDown());

		// Pressed keys pop a little: the box grows by one pixel each side.
		float grow = progress * 1f;
		Render2D.roundedRect(graphics, x - grow, y - grow, w + grow * 2, h + grow * 2, 5, ColorUtil.lerp(Theme.HUD_BACKGROUND, pressed(), progress));

		int textColor = ColorUtil.lerp(Theme.TEXT, pressedText(), progress);
		String label = mapping.getTranslatedKeyMessage().getString();
		if (index == 4) {
			label = "LMB";
		} else if (index == 5) {
			label = "RMB";
		} else if (label.length() > 3) {
			label = label.substring(0, 3);
		}

		if (subtitle == null) {
			VText.drawCentered(graphics, label, x + w / 2f, y + (h - VText.HEIGHT) / 2f + 1, textColor, true);
			return;
		}

		VText.drawCentered(graphics, label, x + w / 2f, y + 4, textColor, true);
		float scale = 0.7f;
		VText.drawScaled(graphics, subtitle, x + w / 2f - VText.width(subtitle) * scale / 2, y + 14, ColorUtil.withAlpha(textColor, 0xB0), false, scale);
	}

	private float animate(int index, boolean pressed) {
		pressAnimations[index] = animator.approach(pressAnimations[index], pressed ? 1 : 0, 25);
		return pressAnimations[index];
	}
}
