package dev.voltliz.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.voltliz.client.config.ConfigManager;
import dev.voltliz.client.gui.ClickGuiScreen;
import dev.voltliz.client.module.HudRenderer;
import dev.voltliz.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoltlizClient implements ClientModInitializer {
	public static final String MOD_ID = "voltliz";
	public static final String NAME = "Voltliz Client";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	private static VoltlizClient instance;

	private ModuleManager moduleManager;
	private ConfigManager configManager;
	private KeyMapping clickGuiKey;

	@Override
	public void onInitializeClient() {
		instance = this;

		moduleManager = new ModuleManager();
		configManager = new ConfigManager(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).resolve("config.json"));
		configManager.load(moduleManager);

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.voltliz.clickgui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));

		ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "hud"), HudRenderer::render);
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> saveConfig());

		LOGGER.info("{} initialized with {} modules", NAME, moduleManager.getModules().size());
	}

	private void onEndTick(Minecraft client) {
		while (clickGuiKey.consumeClick()) {
			if (client.screen == null) {
				client.setScreen(new ClickGuiScreen());
			}
		}

		moduleManager.onTick(client);
	}

	public void saveConfig() {
		configManager.save(moduleManager);
	}

	public static VoltlizClient get() {
		return instance;
	}

	public boolean isClickGuiKey(int key) {
		return KeyBindingHelper.getBoundKeyOf(clickGuiKey).getValue() == key;
	}

	public ModuleManager getModuleManager() {
		return moduleManager;
	}

	public static String version() {
		return FabricLoader.getInstance().getModContainer(MOD_ID)
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse("dev");
	}
}
