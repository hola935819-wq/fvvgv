package dev.voltliz.client.module.hud;

import dev.voltliz.client.module.TextHudModule;
import dev.voltliz.client.setting.ModeSetting;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockModule extends TextHudModule {
	private static final DateTimeFormatter FORMAT_24H = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter FORMAT_12H = DateTimeFormatter.ofPattern("h:mm a");

	private final ModeSetting format = add(new ModeSetting("Format", "24h", "24h", "12h"));

	public ClockModule() {
		super("Clock", "Shows your real-world time.", 4, 126);
	}

	@Override
	protected String getLabel() {
		return "Time";
	}

	@Override
	protected String getValue() {
		return LocalTime.now().format(format.is("24h") ? FORMAT_24H : FORMAT_12H);
	}
}
