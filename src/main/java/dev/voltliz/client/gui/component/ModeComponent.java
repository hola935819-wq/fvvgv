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
		return 17;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		drawName(graphics, setting.getName(), y + 4);
		drawPill(graphics, setting.get(), y + 1, Theme.accent());
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
