package dev.voltliz.client.module;

import dev.voltliz.client.VoltlizClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class HudRenderer {
	private HudRenderer() {
	}

	/** World overlays such as ESP, drawn under the vanilla HUD. */
	public static void renderOverlay(GuiGraphics graphics, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		VoltlizClient voltliz = VoltlizClient.get();
		if (voltliz == null || client.player == null || client.options.hideGui) {
			return;
		}

		float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
		for (Module module : voltliz.getModuleManager().getOverlayModules()) {
			if (module.isEnabled()) {
				((Overlay) module).renderOverlay(graphics, partialTick);
			}
		}
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
