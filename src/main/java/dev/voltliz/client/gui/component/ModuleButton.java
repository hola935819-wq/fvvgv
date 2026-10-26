package dev.voltliz.client.gui.component;

import dev.voltliz.client.gui.ClickGuiScreen;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ColorSetting;
import dev.voltliz.client.setting.ModeSetting;
import dev.voltliz.client.setting.NumberSetting;
import dev.voltliz.client.setting.Setting;
import dev.voltliz.client.util.Animator;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;

/** A module row: left click toggles it, right click opens its settings. */
public class ModuleButton extends GuiComponent {
	public static final int ROW = 15;

	private final Module module;
	private final Animator animator;
	private final List<SettingComponent<?>> settings = new ArrayList<>();

	private boolean expanded;
	private float expandProgress;
	private float enableProgress;
	private float hoverProgress;

	public ModuleButton(Module module, Animator animator) {
		this.module = module;
		this.animator = animator;
		this.enableProgress = module.isEnabled() ? 1 : 0;

		for (Setting<?> setting : module.getSettings()) {
			if (setting instanceof BooleanSetting bool) {
				settings.add(new BooleanComponent(bool, animator));
			} else if (setting instanceof NumberSetting number) {
				settings.add(new NumberComponent(number));
			} else if (setting instanceof ModeSetting mode) {
				settings.add(new ModeComponent(mode));
			} else if (setting instanceof ColorSetting color) {
				settings.add(new ColorComponent(color));
			}
		}
		if (module.isToggleable()) {
			settings.add(new BindComponent(module));
		}
	}

	private int settingsHeight() {
		int height = 0;
		for (SettingComponent<?> component : settings) {
			if (component.isVisible()) {
				height += component.getHeight();
			}
		}
		return height;
	}

	@Override
	public int getHeight() {
		return ROW + Math.round(settingsHeight() * expandProgress);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		boolean hovered = RenderUtil.isInside(mouseX, mouseY, x, y, width, ROW);
		hoverProgress = animator.approach(hoverProgress, hovered ? 1 : 0, 20);
		enableProgress = animator.approach(enableProgress, module.isEnabled() && module.isToggleable() ? 1 : 0, 15);
		expandProgress = animator.approach(expandProgress, expanded ? 1 : 0, 16);

		int accent = Theme.accent(y * 8L);
		graphics.fill(x, y, x + width, y + ROW, ColorUtil.lerp(Theme.ROW, Theme.ROW_HOVER, hoverProgress));
		if (enableProgress > 0) {
			graphics.fill(x, y, x + width, y + ROW, ColorUtil.withAlpha(accent, (int) (enableProgress * 0x55)));
			graphics.fill(x, y, x + 2, y + ROW, ColorUtil.withAlpha(accent, (int) (enableProgress * 0xFF)));
		}

		int nameColor = module.isToggleable() ? ColorUtil.lerp(Theme.TEXT_DIM, Theme.TEXT, enableProgress) : Theme.TEXT;
		graphics.drawString(font(), module.getName(), x + 6, y + 4, nameColor, false);
		if (!settings.isEmpty()) {
			String arrow = expanded ? "-" : "+";
			graphics.drawString(font(), arrow, x + width - 6 - font().width(arrow), y + 4, Theme.TEXT_DIM, false);
		}

		if (hovered) {
			ClickGuiScreen.setHoveredDescription(module.getDescription());
		}

		if (expandProgress > 0) {
			int top = y + ROW;
			int bottom = y + getHeight();
			graphics.enableScissor(x, top, x + width, bottom);
			int componentY = top;
			for (SettingComponent<?> component : settings) {
				if (!component.isVisible()) {
					continue;
				}
				component.x = x;
				component.y = componentY;
				component.width = width;
				component.render(graphics, mouseX, mouseY);
				componentY += component.getHeight();
			}
			graphics.fill(x, top, x + 1, bottom, accent);
			graphics.disableScissor();
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (RenderUtil.isInside(mouseX, mouseY, x, y, width, ROW)) {
			if (button == 0 && module.isToggleable()) {
				module.toggle();
			} else if ((button == 0 || button == 1) && !settings.isEmpty()) {
				expanded = !expanded;
			}
			return true;
		}

		if (expanded && expandProgress > 0.95f) {
			for (SettingComponent<?> component : settings) {
				if (component.isVisible() && component.isHovered(mouseX, mouseY) && component.mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {
		for (SettingComponent<?> component : settings) {
			component.mouseReleased(mouseX, mouseY, button);
		}
	}

	@Override
	public boolean keyPressed(int key) {
		if (!expanded) {
			return false;
		}
		for (SettingComponent<?> component : settings) {
			if (component.keyPressed(key)) {
				return true;
			}
		}
		return false;
	}
}
