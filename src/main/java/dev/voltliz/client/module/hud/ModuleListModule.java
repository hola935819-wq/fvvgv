package dev.voltliz.client.module.hud;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.module.HudModule;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ModeSetting;
import dev.voltliz.client.util.Theme;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** The "ArrayList": every enabled module, longest name first. */
public class ModuleListModule extends HudModule {
	private final BooleanSetting hideHud = add(new BooleanSetting("Hide HUD Modules", false));
	private final BooleanSetting background = add(new BooleanSetting("Background", true));
	private final ModeSetting align = add(new ModeSetting("Align", "Right", "Right", "Left"));

	public ModuleListModule() {
		super("Module List", "Lists your enabled modules.", 10_000, 4);
		setEnabled(true);
	}

	@Override
	protected void renderContent(GuiGraphics graphics) {
		Font font = mc().font;
		List<String> names = VoltlizClient.get().getModuleManager().getModules().stream()
				.filter(module -> module.isEnabled() && module.isToggleable() && module != this)
				.filter(module -> !(module instanceof WatermarkModule))
				.filter(module -> !hideHud.get() || !(module instanceof HudModule))
				.map(Module::getName)
				.sorted(Comparator.comparingInt((String name) -> font.width(name)).reversed())
				.toList();

		int lineHeight = font.lineHeight + 2;
		int maxWidth = 0;
		for (String name : names) {
			maxWidth = Math.max(maxWidth, font.width(name));
		}
		width = Math.max(40, maxWidth + 6);
		height = Math.max(lineHeight, names.size() * lineHeight);

		boolean right = align.is("Right");
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			int lineWidth = font.width(name) + 6;
			int x = right ? width - lineWidth : 0;
			int y = i * lineHeight;
			int color = Theme.accent(i * 150L);

			if (background.get()) {
				graphics.fill(x, y, x + lineWidth, y + lineHeight, Theme.HUD_BACKGROUND);
			}
			int barX = right ? width - 1 : 0;
			graphics.fill(barX, y, barX + 1, y + lineHeight, color);
			graphics.drawString(font, name, x + (right ? 2 : 4), y + 2, color, true);
		}
	}
}
