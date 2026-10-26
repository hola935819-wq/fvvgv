package dev.voltliz.client.module;

import dev.voltliz.client.setting.Setting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;

public abstract class Module {
	private final String name;
	private final String description;
	private final Category category;
	private final List<Setting<?>> settings = new ArrayList<>();
	private boolean enabled;
	private int key = -1;

	protected Module(String name, String description, Category category) {
		this.name = name;
		this.description = description;
		this.category = category;
	}

	protected <S extends Setting<?>> S add(S setting) {
		settings.add(setting);
		return setting;
	}

	protected static Minecraft mc() {
		return Minecraft.getInstance();
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	public void setEnabled(boolean enabled) {
		if (!isToggleable() || this.enabled == enabled) {
			return;
		}

		this.enabled = enabled;

		if (enabled) {
			onEnable();
		} else {
			onDisable();
		}
	}

	protected void onEnable() {
	}

	protected void onDisable() {
	}

	/** Called at the start of each client tick, before vanilla handles input. */
	public void onPreTick(Minecraft client) {
	}

	/** Called at the end of each client tick. */
	public void onTick(Minecraft client) {
	}

	/** Modules that only hold settings (like the Click GUI theme) can't be switched off. */
	public boolean isToggleable() {
		return true;
	}

	public boolean isEnabled() {
		return enabled || !isToggleable();
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Category getCategory() {
		return category;
	}

	public List<Setting<?>> getSettings() {
		return Collections.unmodifiableList(settings);
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}
}
