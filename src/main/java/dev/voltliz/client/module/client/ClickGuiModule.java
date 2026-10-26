package dev.voltliz.client.module.client;

import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ColorSetting;
import dev.voltliz.client.util.Theme;

/** Holds the look of the Click GUI and the accent color shared by the HUD. */
public class ClickGuiModule extends Module {
	public final ColorSetting accent = add(new ColorSetting("Accent", Theme.DEFAULT_ACCENT));
	public final ColorSetting accent2 = add(new ColorSetting("Accent 2", Theme.DEFAULT_ACCENT_2));
	public final BooleanSetting dimBackground = add(new BooleanSetting("Dim Background", true));
	public final BooleanSetting descriptions = add(new BooleanSetting("Descriptions", true));

	public ClickGuiModule() {
		super("Theme", "Accent colors and menu behaviour. Open the menu with Right Shift.", Category.CLIENT);
	}

	@Override
	public boolean isToggleable() {
		return false;
	}
}
