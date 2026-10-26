package dev.voltliz.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.List;

public class ModeSetting extends Setting<String> {
	private final List<String> modes;

	public ModeSetting(String name, String defaultValue, String... modes) {
		super(name, defaultValue);
		this.modes = List.of(modes);
	}

	public boolean is(String mode) {
		return get().equals(mode);
	}

	public void cycle(int direction) {
		int index = modes.indexOf(get());
		set(modes.get(Math.floorMod(index + direction, modes.size())));
	}

	public List<String> getModes() {
		return modes;
	}

	@Override
	public JsonElement save() {
		return new JsonPrimitive(get());
	}

	@Override
	public void load(JsonElement json) {
		String mode = json.getAsString();
		if (modes.contains(mode)) {
			set(mode);
		}
	}
}
