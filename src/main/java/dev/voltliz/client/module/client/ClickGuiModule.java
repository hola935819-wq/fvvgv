package dev.voltliz.client.module.client;

import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ColorSetting;
import dev.voltliz.client.util.Theme;

/** Holds the look of the Click GUI and the accent color shared by the HUD. */
public class ClickGuiModule extends Module {
	public final ColorSetting accent = add(new ColorSetting("Accent", Theme.DEFAULT_ACCENT));
	public final BooleanSetting dimBackground = add(new BooleanSetting("Dim Background", true));
	public final BooleanSetting descriptions = add(new BooleanSetting("Descriptions", true));

	public ClickGuiModule() {
		super("ClickGUI", "Colors and behaviour of this menu. Open it with Right Shift.", Category.CLIENT);
	}

	@Override
	public boolean isToggleable() {
		return false;
	}
}
