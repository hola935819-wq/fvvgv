package dev.voltliz.client.gui.component;

import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.util.Animator;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.gui.GuiGraphics;

public class BooleanComponent extends SettingComponent<BooleanSetting> {
	private final Animator animator;
	private float progress;

	public BooleanComponent(BooleanSetting setting, Animator animator) {
		super(setting);
		this.animator = animator;
		this.progress = setting.get() ? 1 : 0;
	}

	@Override
	public int getHeight() {
		return 13;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		progress = animator.approach(progress, setting.get() ? 1 : 0, 18);
		drawBackground(graphics);
		drawName(graphics, setting.getName(), y + 3);

		// A small switch: track plus sliding knob.
		int trackWidth = 14;
		int trackX = x + width - PADDING - trackWidth;
		int trackY = y + 3;
		int accent = Theme.accent();
		RenderUtil.roundedRect(graphics, trackX, trackY, trackWidth, 7, ColorUtil.lerp(0xFF2A2A33, ColorUtil.withAlpha(accent, 0xC0), progress));
		int knobX = trackX + 1 + Math.round(progress * (trackWidth - 7));
		RenderUtil.roundedRect(graphics, knobX, trackY + 1, 5, 5, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			setting.toggle();
			return true;
		}
		return false;
	}
}
