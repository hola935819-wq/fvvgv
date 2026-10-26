package dev.voltliz.client.module.hud;

import dev.voltliz.client.module.TextHudModule;
import dev.voltliz.client.setting.BooleanSetting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;

public class CoordinatesModule extends TextHudModule {
	private final BooleanSetting decimals = add(new BooleanSetting("Decimals", false));
	private final BooleanSetting otherDimension = add(new BooleanSetting("Nether/Overworld", false));

	public CoordinatesModule() {
		super("Coordinates", "Shows your position in the world.", 4, 62);
		setEnabled(true);
	}

	@Override
	protected String getLabel() {
		return "XYZ";
	}

	@Override
	protected String getValue() {
		LocalPlayer player = mc().player;
		if (player == null) {
			return "-";
		}

		String value = format(player.getX(), player.getY(), player.getZ());

		if (otherDimension.get() && player.level() != null) {
			if (player.level().dimension() == Level.NETHER) {
				value += "  (OW " + format(player.getX() * 8, player.getY(), player.getZ() * 8) + ")";
			} else if (player.level().dimension() == Level.OVERWORLD) {
				value += "  (N " + format(player.getX() / 8, player.getY(), player.getZ() / 8) + ")";
			}
		}
		return value;
	}

	private String format(double x, double y, double z) {
		if (decimals.get()) {
			return String.format("%.1f, %.1f, %.1f", x, y, z);
		}
		return (int) Math.floor(x) + ", " + (int) Math.floor(y) + ", " + (int) Math.floor(z);
	}
}
