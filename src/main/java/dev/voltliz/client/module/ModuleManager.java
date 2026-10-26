package dev.voltliz.client.module;

import dev.voltliz.client.module.client.ClickGuiModule;
import dev.voltliz.client.module.combat.AimAssistModule;
import dev.voltliz.client.module.combat.AutoCrystalModule;
import dev.voltliz.client.module.combat.AutoTotemModule;
import dev.voltliz.client.module.combat.TriggerBotModule;
import dev.voltliz.client.module.hud.ArmorStatusModule;
import dev.voltliz.client.module.hud.ClockModule;
import dev.voltliz.client.module.hud.CoordinatesModule;
import dev.voltliz.client.module.hud.CpsModule;
import dev.voltliz.client.module.hud.DirectionModule;
import dev.voltliz.client.module.hud.FpsModule;
import dev.voltliz.client.module.hud.KeystrokesModule;
import dev.voltliz.client.module.hud.ModuleListModule;
import dev.voltliz.client.module.hud.PingModule;
import dev.voltliz.client.module.hud.PotionEffectsModule;
import dev.voltliz.client.module.hud.ServerAddressModule;
import dev.voltliz.client.module.hud.TargetHudModule;
import dev.voltliz.client.module.hud.WatermarkModule;
import dev.voltliz.client.module.player.ToggleSprintModule;
import dev.voltliz.client.module.render.Esp2DModule;
import dev.voltliz.client.module.render.Esp3DModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class ModuleManager {
	private final List<Module> modules = new ArrayList<>();
	private final List<HudModule> hudModules = new ArrayList<>();
	private Set<Integer> heldKeys = new HashSet<>();

	public ModuleManager() {
		add(new TriggerBotModule());
		add(new AimAssistModule());
		add(new AutoCrystalModule());
		add(new AutoTotemModule());

		add(new Esp2DModule());
		add(new Esp3DModule());

		add(new WatermarkModule());
		add(new FpsModule());
		add(new CpsModule());
		add(new CoordinatesModule());
		add(new DirectionModule());
		add(new PingModule());
		add(new ServerAddressModule());
		add(new ClockModule());
		add(new KeystrokesModule());
		add(new TargetHudModule());
		add(new ArmorStatusModule());
		add(new PotionEffectsModule());
		add(new ModuleListModule());

		add(new ToggleSprintModule());

		add(new ClickGuiModule());
	}

	private void add(Module module) {
		modules.add(module);
		if (module instanceof HudModule hudModule) {
			hudModules.add(hudModule);
		}
	}

	public void onPreTick(Minecraft client) {
		for (Module module : modules) {
			if (module.isEnabled()) {
				module.onPreTick(client);
			}
		}
	}

	public void onFrame(Minecraft client) {
		for (Module module : modules) {
			if (module.isEnabled()) {
				module.onFrame(client);
			}
		}
	}

	public void onTick(Minecraft client) {
		handleKeybinds(client);

		for (Module module : modules) {
			if (module.isEnabled()) {
				module.onTick(client);
			}
		}
	}

	private void handleKeybinds(Minecraft client) {
		//? if >=1.21.9 {
		long window = client.getWindow().handle();
		//?} else {
		/*long window = client.getWindow().getWindow();
		*///?}
		Set<Integer> down = new HashSet<>();

		for (Module module : modules) {
			int key = module.getKey();
			if (key >= GLFW.GLFW_KEY_SPACE && GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS) {
				down.add(key);
			}
		}

		// Binds only fire in-game, and only on the tick the key goes down.
		if (client.screen == null && client.player != null) {
			for (Module module : modules) {
				int key = module.getKey();
				if (down.contains(key) && !heldKeys.contains(key)) {
					module.toggle();
				}
			}
		}

		heldKeys = down;
	}

	public List<Module> getModules() {
		return Collections.unmodifiableList(modules);
	}

	/** Modules that draw world overlays (ESP) under the HUD. */
	public List<Module> getOverlayModules() {
		return modules.stream().filter(Overlay.class::isInstance).toList();
	}

	public List<HudModule> getHudModules() {
		return Collections.unmodifiableList(hudModules);
	}

	public List<Module> getModules(Category category) {
		return modules.stream().filter(module -> module.getCategory() == category).toList();
	}

	public <T extends Module> T get(Class<T> type) {
		for (Module module : modules) {
			if (type.isInstance(module)) {
				return type.cast(module);
			}
		}
		return null;
	}
}
