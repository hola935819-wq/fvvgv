package dev.voltliz.client.module.hud;

import dev.voltliz.client.module.TextHudModule;
import dev.voltliz.client.setting.ModeSetting;
import dev.voltliz.client.util.CpsTracker;

public class CpsModule extends TextHudModule {
	private final ModeSetting mode = add(new ModeSetting("Buttons", "Both", "Both", "Left"));

	public CpsModule() {
		super("CPS", "Shows your clicks per second.", 8, 56);
		setEnabled(true);
	}

	@Override
	protected String getLabel() {
		return "CPS";
	}

	@Override
	protected String getValue() {
		if (mode.is("Left")) {
			return String.valueOf(CpsTracker.getLeft());
		}
		return CpsTracker.getLeft() + " | " + CpsTracker.getRight();
	}
}
