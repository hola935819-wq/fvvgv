package dev.voltliz.client.gui.component;

import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.util.Animator;
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
		return 16;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		progress = animator.approach(progress, setting.get() ? 1 : 0, 18);
		drawName(graphics, setting.getName(), y + 4);
		drawSwitch(graphics, x + width - 18, y + 3, progress);
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
