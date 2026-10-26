package dev.voltliz.client.module.render;

import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.Module;
import net.minecraft.client.gui.GuiGraphics;

/** A module that draws over the world, under the HUD (for example ESP). */
public abstract class OverlayModule extends Module {
	protected OverlayModule(String name, String description) {
		super(name, description, Category.RENDER);
	}

	public abstract void renderOverlay(GuiGraphics graphics, float partialTick);
}
