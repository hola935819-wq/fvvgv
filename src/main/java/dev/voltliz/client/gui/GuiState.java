package dev.voltliz.client.gui;

import dev.voltliz.client.module.Category;

/** Click GUI state kept between openings and saved in the config. */
public final class GuiState {
	public static Category selectedCategory = Category.HUD;

	private GuiState() {
	}
}
