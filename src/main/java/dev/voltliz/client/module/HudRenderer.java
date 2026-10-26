package dev.voltliz.client.module;

import dev.voltliz.client.VoltlizClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class HudRenderer {
	private HudRenderer() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		VoltlizClient voltliz = VoltlizClient.get();
		if (voltliz == null || client.player == null || client.options.hideGui) {
			return;
		}

		for (HudModule module : voltliz.getModuleManager().getHudModules()) {
			if (module.isEnabled()) {
				module.render(graphics);
			}
		}
	}
}
