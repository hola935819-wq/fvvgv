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
	public static final int HEADER = 40;
	private static final int PADDING = 10;
	private static final int SETTING_GAP = 4;
	private static final int RADIUS = 8;

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
		int height = 8;
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
		int accent2 = Theme.accent2();

		// Lift on hover, glow when enabled.
		Render2D.shadow(graphics, x, y, width, height, RADIUS, 5, Math.round(0x30 + hoverProgress * 0x30));
		if (enableProgress > 0.01f) {
			Render2D.shadow(graphics, x, y, width, height, RADIUS, 6, Math.round(enableProgress * 0x30), accent);
			int alpha = Math.round(enableProgress * 0xD0);
			Render2D.horizontalGradient(graphics, x, y, width, height, RADIUS, ColorUtil.withAlpha(accent, alpha), ColorUtil.withAlpha(accent2, alpha));
			Render2D.roundedRect(graphics, x + 1, y + 1, width - 2, height - 2, RADIUS - 1, ColorUtil.lerp(Theme.CARD, Theme.CARD_HOVER, hoverProgress));
		} else {
			Render2D.roundedRect(graphics, x, y, width, height, RADIUS, ColorUtil.lerp(Theme.CARD, Theme.CARD_HOVER, hoverProgress));
		}
		Render2D.rect(graphics, x + RADIUS, y + 1, width - RADIUS * 2, 1, 0x0CFFFFFF);

		// Badge with the module's initial.
		int badgeX = x + PADDING;
		int badgeY = y + 9;
		Render2D.roundedRect(graphics, badgeX, badgeY, 22, 22, 7, Theme.FIELD);
		if (enableProgress > 0.01f) {
			int alpha = Math.round(enableProgress * 255);
			Render2D.horizontalGradient(graphics, badgeX, badgeY, 22, 22, 7, ColorUtil.withAlpha(accent, alpha), ColorUtil.withAlpha(accent2, alpha));
		}
		String initial = module.getName().substring(0, 1);
		VText.drawCentered(graphics, initial, badgeX + 11, badgeY + 7, ColorUtil.lerp(Theme.TEXT_DIM, 0xFFFFFFFF, Math.max(enableProgress, module.isToggleable() ? 0 : 1)), true);

		int textX = badgeX + 30;
		int textWidth = width - (textX - x) - PADDING - 26;
		VText.draw(graphics, VText.trim(module.getName(), textWidth, true), textX, y + 10, Theme.TEXT, true);
		VText.draw(graphics, VText.trim(module.getDescription(), textWidth, false), textX, y + 22, Theme.TEXT_DIM);

		if (module.isToggleable()) {
			SettingComponent.drawSwitch(graphics, x + width - PADDING - 20, y + 9, enableProgress);
		}
		if (!settings.isEmpty()) {
			// Three dots that pull together and take the accent color while the settings are open.
			float cx = x + width - PADDING - 10;
			float cy = y + 29;
			for (int i = -1; i <= 1; i++) {
				float dotX = cx + i * 4 * (1 - expandProgress * 0.3f);
				Render2D.circle(graphics, dotX, cy, 1.3f, ColorUtil.lerp(Theme.TEXT_DIM, accent, expandProgress));
			}
		}

		if (expandProgress > 0) {
			int top = y + HEADER;
			int bottom = y + height - 2;
			Render2D.rect(graphics, x + PADDING, top - 1, width - PADDING * 2, 1, Theme.SEPARATOR);
			graphics.enableScissor(x, top, x + width, bottom);
			int componentY = top + 8;
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
			boolean onDots = mouseX >= x + width - PADDING - 20 && mouseY >= y + 22;
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

	public boolean isListening() {
		for (SettingComponent<?> component : settings) {
			if (component instanceof BindComponent bind && bind.isListening()) {
				return true;
			}
		}
		return false;
	}
}
