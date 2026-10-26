package dev.voltliz.client.module.hud;

import dev.voltliz.client.module.TextHudModule;
import dev.voltliz.client.setting.BooleanSetting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public class DirectionModule extends TextHudModule {
	private final BooleanSetting showYaw = add(new BooleanSetting("Show Yaw", false));

	public DirectionModule() {
		super("Direction", "Shows the direction you are facing.", 4, 78);
		setEnabled(true);
	}

	@Override
	protected String getLabel() {
		return "Facing";
	}

	@Override
	protected String getValue() {
		LocalPlayer player = mc().player;
		if (player == null) {
			return "-";
		}

		String value = switch (player.getDirection()) {
			case NORTH -> "North (-Z)";
			case SOUTH -> "South (+Z)";
			case EAST -> "East (+X)";
			case WEST -> "West (-X)";
			default -> player.getDirection().getName();
		};

		if (showYaw.get()) {
			value += String.format(" %.1f", Mth.wrapDegrees(player.getYRot()));
		}
		return value;
	}
}
