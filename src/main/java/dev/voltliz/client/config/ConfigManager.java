package dev.voltliz.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.gui.GuiState;
import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.HudModule;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.module.ModuleManager;
import dev.voltliz.client.setting.Setting;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final Path file;

	public ConfigManager(Path file) {
		this.file = file;
	}

	public void load(ModuleManager modules) {
		if (!Files.exists(file)) {
			return;
		}

		try {
			JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
			JsonObject moduleData = root.has("modules") ? root.getAsJsonObject("modules") : new JsonObject();

			for (Module module : modules.getModules()) {
				if (moduleData.has(module.getName())) {
					loadModule(module, moduleData.getAsJsonObject(module.getName()));
				}
			}

			if (root.has("panels")) {
				JsonObject panels = root.getAsJsonObject("panels");
				for (Category category : Category.values()) {
					if (panels.has(category.name())) {
						JsonObject panel = panels.getAsJsonObject(category.name());
						GuiState.PanelState state = GuiState.get(category);
						state.x = panel.get("x").getAsInt();
						state.y = panel.get("y").getAsInt();
						state.open = panel.get("open").getAsBoolean();
					}
				}
			}
		} catch (Exception e) {
			VoltlizClient.LOGGER.error("Could not read config {}", file, e);
		}
	}

	private void loadModule(Module module, JsonObject json) {
		if (json.has("enabled")) {
			module.setEnabled(json.get("enabled").getAsBoolean());
		}
		if (json.has("key")) {
			module.setKey(json.get("key").getAsInt());
		}
		if (module instanceof HudModule hud && json.has("x") && json.has("y")) {
			hud.setPosition(json.get("x").getAsFloat(), json.get("y").getAsFloat());
		}
		if (json.has("settings")) {
			JsonObject settings = json.getAsJsonObject("settings");
			for (Setting<?> setting : module.getSettings()) {
				JsonElement value = settings.get(setting.getName());
				if (value == null) {
					continue;
				}
				try {
					setting.load(value);
				} catch (Exception e) {
					VoltlizClient.LOGGER.warn("Ignoring bad value for {} > {}", module.getName(), setting.getName());
				}
			}
		}
	}

	public void save(ModuleManager modules) {
		JsonObject root = new JsonObject();

		JsonObject moduleData = new JsonObject();
		for (Module module : modules.getModules()) {
			JsonObject json = new JsonObject();
			json.addProperty("enabled", module.isEnabled());
			json.addProperty("key", module.getKey());
			if (module instanceof HudModule hud) {
				json.addProperty("x", hud.getX());
				json.addProperty("y", hud.getY());
			}
			JsonObject settings = new JsonObject();
			for (Setting<?> setting : module.getSettings()) {
				settings.add(setting.getName(), setting.save());
			}
			json.add("settings", settings);
			moduleData.add(module.getName(), json);
		}
		root.add("modules", moduleData);

		JsonObject panels = new JsonObject();
		for (Category category : Category.values()) {
			GuiState.PanelState state = GuiState.get(category);
			JsonObject panel = new JsonObject();
			panel.addProperty("x", state.x);
			panel.addProperty("y", state.y);
			panel.addProperty("open", state.open);
			panels.add(category.name(), panel);
		}
		root.add("panels", panels);

		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(root));
		} catch (IOException e) {
			VoltlizClient.LOGGER.error("Could not save config {}", file, e);
		}
	}
}
