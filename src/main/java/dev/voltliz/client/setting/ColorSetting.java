package dev.voltliz.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.voltliz.client.util.ColorUtil;

/** An ARGB color edited through its hue, with an optional animated rainbow mode. */
public class ColorSetting extends Setting<Integer> {
	private boolean rainbow;

	public ColorSetting(String name, int defaultColor) {
		super(name, defaultColor);
	}

	public int getColor() {
		return getColor(0);
	}

	/** The current color; {@code offset} shifts the rainbow so lists can show a wave. */
	public int getColor(long offset) {
		if (rainbow) {
			float[] hsb = ColorUtil.toHsb(get());
			float saturation = hsb[1] < 0.1f ? 0.7f : hsb[1];
			float brightness = hsb[2] < 0.1f ? 1f : hsb[2];
			return ColorUtil.rainbow(offset, saturation, brightness);
		}
		return get();
	}

	public float getHue() {
		return ColorUtil.toHsb(get())[0];
	}

	public void setHue(float hue) {
		float[] hsb = ColorUtil.toHsb(get());
		// A white or black color has no hue to edit, so give it some saturation first.
		float saturation = hsb[1] < 0.1f ? 0.8f : hsb[1];
		float brightness = hsb[2] < 0.1f ? 1f : hsb[2];
		set(ColorUtil.hsb(hue, saturation, brightness, (get() >>> 24) & 0xFF));
	}

	public boolean isRainbow() {
		return rainbow;
	}

	public void setRainbow(boolean rainbow) {
		this.rainbow = rainbow;
	}

	@Override
	public void reset() {
		super.reset();
		rainbow = false;
	}

	@Override
	public JsonElement save() {
		JsonObject json = new JsonObject();
		json.addProperty("color", String.format("#%08X", get()));
		json.addProperty("rainbow", rainbow);
		return json;
	}

	@Override
	public void load(JsonElement json) {
		JsonObject object = json.getAsJsonObject();
		set((int) Long.parseLong(object.get("color").getAsString().substring(1), 16));
		rainbow = object.has("rainbow") && object.get("rainbow").getAsBoolean();
	}
}
