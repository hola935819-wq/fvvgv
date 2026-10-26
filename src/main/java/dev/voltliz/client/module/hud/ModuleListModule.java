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
		super("Module List", "Lists your enabled modules.", 10_000, 4);
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
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			int lineWidth = VText.width(name) + 12;
			int x = right ? width - lineWidth : 0;
			int y = i * (lineHeight + 1);
			int color = Theme.accent(i * 150L);

			if (background.get()) {
				Render2D.roundedRect(graphics, x, y, lineWidth, lineHeight, 3.5f, Theme.HUD_BACKGROUND);
			}
			float dotX = right ? x + lineWidth - 4.5f : x + 4.5f;
			Render2D.circle(graphics, dotX, y + lineHeight / 2f, 1.5f, color);
			VText.draw(graphics, name, x + (right ? 4 : 8), y + 3, color);
		}
	}
}
