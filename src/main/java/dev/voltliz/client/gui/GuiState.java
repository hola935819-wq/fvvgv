package dev.voltliz.client.gui;

import dev.voltliz.client.module.Category;
import java.util.EnumMap;
import java.util.Map;

/** Panel positions of the Click GUI, kept between openings and saved in the config. */
public final class GuiState {
	private static final Map<Category, PanelState> PANELS = new EnumMap<>(Category.class);

	private GuiState() {
	}

	public static PanelState get(Category category) {
		return PANELS.computeIfAbsent(category, c -> new PanelState(16 + c.ordinal() * 122, 16, true));
	}

	public static final class PanelState {
		public int x;
		public int y;
		public boolean open;

		public PanelState(int x, int y, boolean open) {
			this.x = x;
			this.y = y;
			this.open = open;
		}
	}
}
