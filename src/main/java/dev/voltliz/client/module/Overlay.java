package dev.voltliz.client.module;

import net.minecraft.client.gui.GuiGraphics;

/** A module that draws over the world, under the HUD (ESP, FOV circle). */
public interface Overlay {
	void renderOverlay(GuiGraphics graphics, float partialTick);
}
