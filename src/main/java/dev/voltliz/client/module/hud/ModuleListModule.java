package dev.voltliz.client.module.hud;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.module.HudModule;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ModeSetting;
import dev.voltliz.client.util.Theme;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;

/** The "ArrayList": every enabled module, longest name first. */
public class ModuleListModule extends HudModule {
	private final BooleanSetting hideHud = add(new BooleanSetting("Hide HUD Modules", false));
	private final BooleanSetting background = add(new BooleanSetting("Background", true));
	private final ModeSetting align = add(new ModeSetting("Align", "Right", "Right", "Left"));

	public ModuleListModule() {
		super("Module List", "Lists your enabled modules.", 10_000, 8);
		setEnabled(true);
	}

	@Override
	protected void renderContent(GuiGraphics graphics) {
		List<String> names = VoltlizClient.get().getModuleManager().getModules().stream()
				.filter(module -> module.isEnabled() && module.isToggleable() && module != this)
				.filter(module -> !(module instanceof WatermarkModule))
				.filter(module -> !hideHud.get() || !(module instanceof HudModule))
				.map(Module::getName)
				.sorted(Comparator.comparingInt((String name) -> VText.width(name)).reversed())
				.toList();

		int lineHeight = VText.HEIGHT + 5;
		int maxWidth = 0;
		for (String name : names) {
			maxWidth = Math.max(maxWidth, VText.width(name));
		}
		width = Math.max(40, maxWidth + 12);
		height = Math.max(lineHeight, names.size() * (lineHeight + 1));

		boolean right = align.is("Right");
		int count = Math.max(1, names.size() - 1);
		float phase = (float) ((System.currentTimeMillis() % 4000L) / 4000.0);
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			int lineWidth = VText.width(name) + 12;
			int x = right ? width - lineWidth : 0;
			int y = i * (lineHeight + 1);
			// Each row takes its color from a slowly moving accent -> accent2 gradient.
			float t = (float) i / count * 0.6f + phase;
			int color = Theme.gradient((float) (Math.sin(t * Math.PI * 2) + 1) / 2);

			if (background.get()) {
				Render2D.roundedRect(graphics, x, y, lineWidth, lineHeight, 3.5f, Theme.HUD_BACKGROUND);
			}
			float barX = right ? x + lineWidth - 3 : x + 1;
			Render2D.roundedRect(graphics, barX, y + 3, 2, lineHeight - 6, 1, color);
			VText.draw(graphics, name, x + (right ? 4 : 8), y + 3, color);
		}
	}
}
