package dev.voltliz.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.Mth;

public class NumberSetting extends Setting<Double> {
	private final double min;
	private final double max;
	private final double step;

	public NumberSetting(String name, double defaultValue, double min, double max, double step) {
		super(name, defaultValue);
		this.min = min;
		this.max = max;
		this.step = step;
	}

	@Override
	public void set(Double value) {
		double snapped = Math.round(value / step) * step;
		super.set(Mth.clamp(snapped, min, max));
	}

	public float getFloat() {
		return get().floatValue();
	}

	public int getInt() {
		return (int) Math.round(get());
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	/** Position of the value inside its range, from 0 to 1. */
	public double getProgress() {
		return (get() - min) / (max - min);
	}

	public void setProgress(double progress) {
		set(min + Mth.clamp(progress, 0, 1) * (max - min));
	}

	public String format() {
		if (step >= 1) {
			return String.valueOf(getInt());
		}
		return step >= 0.1 ? String.format("%.1f", get()) : String.format("%.2f", get());
	}

	@Override
	public JsonElement save() {
		return new JsonPrimitive(get());
	}

	@Override
	public void load(JsonElement json) {
		set(json.getAsDouble());
	}
}
