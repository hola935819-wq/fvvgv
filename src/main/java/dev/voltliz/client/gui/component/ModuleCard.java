package dev.voltliz.client.gui.component;

import dev.voltliz.client.module.Module;
import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
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

/** A module tile: click to toggle, right click (or the dots) to open its settings. */
public class ModuleCard extends GuiComponent {
	public static final int HEADER = 36;
	private static final int PADDING = 10;
	private static final int SETTING_GAP = 3;

	private final Module module;
	private final Animator animator;
	private final List<SettingComponent<?>> settings = new ArrayList<>();

	private boolean expanded;
	private float expandProgress;
	private float enableProgress;
	private float hoverProgress;

	public ModuleCard(Module module, Animator animator) {
		this.module = module;
		this.animator = animator;
		this.enableProgress = module.isEnabled() ? 1 : 0;
		this.expanded = !module.isToggleable();

		for (Setting<?> setting : module.getSettings()) {
			if (setting instanceof BooleanSetting bool) {
				settings.add(new BooleanComponent(bool, animator));
			} else if (setting instanceof NumberSetting number) {
				settings.add(new NumberComponent(number, animator));
			} else if (setting instanceof ModeSetting mode) {
				settings.add(new ModeComponent(mode));
			} else if (setting instanceof ColorSetting color) {
				settings.add(new ColorComponent(color));
			}
		}
		if (module.isToggleable()) {
			settings.add(new BindComponent(module));
		}
		this.expandProgress = expanded ? 1 : 0;
	}

	public Module getModule() {
		return module;
	}

	private int settingsHeight() {
		int height = 6;
		for (SettingComponent<?> component : settings) {
			if (component.isVisible()) {
				height += component.getHeight() + SETTING_GAP;
			}
		}
		return height + 4;
	}

	@Override
	public int getHeight() {
		return HEADER + Math.round(settingsHeight() * expandProgress);
	}

	/** Advances the animations; called before layout so heights are current. */
	public void tick(int mouseX, int mouseY) {
		boolean hovered = RenderUtil.isInside(mouseX, mouseY, x, y, width, HEADER);
		hoverProgress = animator.approach(hoverProgress, hovered ? 1 : 0, 18);
		enableProgress = animator.approach(enableProgress, module.isEnabled() && module.isToggleable() ? 1 : 0, 14);
		expandProgress = animator.approach(expandProgress, expanded ? 1 : 0, 14);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		int height = getHeight();
		int accent = Theme.accent();

		int background = ColorUtil.lerp(Theme.CARD, Theme.CARD_HOVER, hoverProgress);
		if (enableProgress > 0) {
			Render2D.roundedRect(graphics, x, y, width, height, 7, ColorUtil.withAlpha(accent, (int) (enableProgress * 0x90)));
			Render2D.roundedRect(graphics, x + 1, y + 1, width - 2, height - 2, 6, background);
			Render2D.roundedRect(graphics, x + 1, y + 1, width - 2, height - 2, 6, ColorUtil.withAlpha(accent, (int) (enableProgress * 0x14)));
		} else {
			Render2D.roundedRect(graphics, x, y, width, height, 7, background);
		}

		VText.draw(graphics, module.getName(), x + PADDING, y + 8, Theme.TEXT, true);
		String description = module.getDescription();
		int maxWidth = width - PADDING * 2 - 34;
		if (VText.width(description) > maxWidth) {
			while (description.length() > 3 && VText.width(description + "...") > maxWidth) {
				description = description.substring(0, description.length() - 1);
			}
			description = description.trim() + "...";
		}
		VText.draw(graphics, description, x + PADDING, y + 20, Theme.TEXT_DIM);

		if (module.isToggleable()) {
			SettingComponent.drawSwitch(graphics, x + width - PADDING - 18, y + 9, enableProgress);
		}
		if (!settings.isEmpty()) {
			// Three dots that rotate into a line while the settings are open.
			float cx = x + width - PADDING - 9;
			float cy = y + 25;
			for (int i = -1; i <= 1; i++) {
				float dotX = cx + i * 4 * (1 - expandProgress * 0.25f);
				Render2D.circle(graphics, dotX, cy, 1.2f, ColorUtil.lerp(Theme.TEXT_DIM, accent, expandProgress));
			}
		}

		if (expandProgress > 0) {
			int top = y + HEADER;
			int bottom = y + height - 2;
			graphics.fill(x + PADDING, top - 1, x + width - PADDING, top, Theme.SEPARATOR);
			graphics.enableScissor(x, top, x + width, bottom);
			int componentY = top + 6;
			for (SettingComponent<?> component : settings) {
				if (!component.isVisible()) {
					continue;
				}
				component.x = x + PADDING;
				component.y = componentY;
				component.width = width - PADDING * 2;
				component.render(graphics, mouseX, mouseY);
				componentY += component.getHeight() + SETTING_GAP;
			}
			graphics.disableScissor();
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (RenderUtil.isInside(mouseX, mouseY, x, y, width, HEADER)) {
			boolean onDots = mouseX >= x + width - PADDING - 18 && mouseY >= y + 20;
			if (button == 0 && module.isToggleable() && !onDots) {
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
		return isHovered(mouseX, mouseY);
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
