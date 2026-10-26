package dev.voltliz.client.gui.component;

import dev.voltliz.client.setting.ModeSetting;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.gui.GuiGraphics;

public class ModeComponent extends SettingComponent<ModeSetting> {
	public ModeComponent(ModeSetting setting) {
		super(setting);
	}

	@Override
	public int getHeight() {
		return 13;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		drawBackground(graphics);
		drawName(graphics, setting.getName(), y + 3);
		drawRight(graphics, setting.get(), y + 3, Theme.accent());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 || button == 1) {
			setting.cycle(button == 0 ? 1 : -1);
			return true;
		}
		return false;
	}
}
