package dev.voltliz.client.module.hud;

import dev.voltliz.client.module.TextHudModule;

public class FpsModule extends TextHudModule {
	public FpsModule() {
		super("FPS", "Shows your frames per second.", 8, 38);
		setEnabled(true);
	}

	@Override
	protected String getLabel() {
		return "FPS";
	}

	@Override
	protected String getValue() {
		return String.valueOf(mc().getFps());
	}
}
