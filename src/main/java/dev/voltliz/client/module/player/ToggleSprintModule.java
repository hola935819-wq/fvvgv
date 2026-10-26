package dev.voltliz.client.module.player;

import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.Module;
import net.minecraft.client.Minecraft;

/** Keeps the sprint key held, like the vanilla "Sprint: Toggle" option. */
public class ToggleSprintModule extends Module {
	public ToggleSprintModule() {
		super("Toggle Sprint", "Sprint without holding the sprint key.", Category.PLAYER);
	}

	@Override
	public void onTick(Minecraft client) {
		if (client.player != null && client.screen == null) {
			client.options.keySprint.setDown(true);
		}
	}

	@Override
	protected void onDisable() {
		mc().options.keySprint.setDown(false);
	}
}
