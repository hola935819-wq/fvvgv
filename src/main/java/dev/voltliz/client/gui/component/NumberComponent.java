package dev.voltliz.client.gui.component;

import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
import dev.voltliz.client.setting.NumberSetting;
import dev.voltliz.client.util.Animator;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.gui.GuiGraphics;

public class NumberComponent extends SettingComponent<NumberSetting> {
	private final Animator animator;
	private boolean dragging;
	private float shown;

	public NumberComponent(NumberSetting setting, Animator animator) {
		super(setting);
		this.animator = animator;
		this.shown = (float) setting.getProgress();
	}

	@Override
	public int getHeight() {
		return 24;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		if (dragging) {
			setting.setProgress((mouseX - x) / (double) width);
		}
		shown = animator.approach(shown, (float) setting.getProgress(), 22);

		drawName(graphics, setting.getName(), y + 3);
		String value = setting.format();
		VText.draw(graphics, value, x + width - VText.width(value), y + 3, Theme.TEXT);

		int trackY = y + 16;
		float filled = width * shown;
		Render2D.roundedRect(graphics, x, trackY, width, 3, 1.5f, Theme.FIELD);
		Render2D.roundedRect(graphics, x, trackY, Math.max(3, filled), 3, 1.5f, Theme.accent());
		Render2D.circle(graphics, x + filled, trackY + 1.5f, 3.5f, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			dragging = true;
			return true;
		}
		if (button == 1) {
			setting.reset();
			return true;
		}
		return false;
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {
		dragging = false;
	}
}
