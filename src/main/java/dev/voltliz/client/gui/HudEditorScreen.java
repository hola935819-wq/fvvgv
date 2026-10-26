package dev.voltliz.client.gui;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.module.HudModule;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Drag HUD elements around, scroll to resize them. */
public class HudEditorScreen extends Screen {
	private static final int SNAP = 4;

	private final Screen parent;
	private HudModule dragging;
	private double dragOffsetX;
	private double dragOffsetY;
	private boolean snappedX;
	private boolean snappedY;

	public HudEditorScreen(Screen parent) {
		super(Component.literal("Voltliz HUD Editor"));
		this.parent = parent;
	}

	private List<HudModule> modules() {
		return VoltlizClient.get().getModuleManager().getHudModules().stream().filter(HudModule::isEnabled).toList();
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, width, height, 0x30000000);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		if (dragging != null) {
			drag(mouseX, mouseY);
			if (snappedX) {
				graphics.fill(width / 2, 0, width / 2 + 1, height, ColorUtil.withAlpha(Theme.accent(), 0x80));
			}
			if (snappedY) {
				graphics.fill(0, height / 2, width, height / 2 + 1, ColorUtil.withAlpha(Theme.accent(), 0x80));
			}
		}

		HudModule hovered = hoveredModule(mouseX, mouseY);
		for (HudModule module : modules()) {
			int x = Math.round(module.getX());
			int y = Math.round(module.getY());
			int w = Math.round(module.getScaledWidth());
			int h = Math.round(module.getScaledHeight());
			boolean active = module == dragging || (dragging == null && module == hovered);

			RenderUtil.outline(graphics, x - 1, y - 1, w + 2, h + 2, active ? Theme.accent() : 0x60FFFFFF);
			if (active) {
				graphics.fill(x, y, x + w, y + h, ColorUtil.withAlpha(Theme.accent(), 0x25));
				String label = module.getName() + " (" + Math.round(module.getScale() * 100) + "%)";
				int labelY = y > 12 ? y - 11 : y + h + 3;
				graphics.drawString(font, label, x, labelY, Theme.TEXT, true);
			}
		}

		String[] lines = {"HUD Editor", "Drag to move  |  Scroll to resize  |  Right click: reset size  |  ESC: back"};
		for (int i = 0; i < lines.length; i++) {
			graphics.drawCenteredString(font, lines[i], width / 2, height / 2 - 10 + i * 12, i == 0 ? Theme.accent() : Theme.TEXT_DIM);
		}
	}

	private void drag(int mouseX, int mouseY) {
		float x = (float) (mouseX - dragOffsetX);
		float y = (float) (mouseY - dragOffsetY);
		float w = dragging.getScaledWidth();
		float h = dragging.getScaledHeight();

		// Snap to the screen edges and to the center lines.
		if (Math.abs(x) < SNAP) {
			x = 0;
		} else if (Math.abs(x + w - width) < SNAP) {
			x = width - w;
		}
		if (Math.abs(y) < SNAP) {
			y = 0;
		} else if (Math.abs(y + h - height) < SNAP) {
			y = height - h;
		}
		snappedX = Math.abs(x + w / 2 - width / 2f) < SNAP;
		if (snappedX) {
			x = width / 2f - w / 2;
		}
		snappedY = Math.abs(y + h / 2 - height / 2f) < SNAP;
		if (snappedY) {
			y = height / 2f - h / 2;
		}

		dragging.setPosition(x, y);
	}

	private HudModule hoveredModule(double mouseX, double mouseY) {
		List<HudModule> modules = modules();
		for (int i = modules.size() - 1; i >= 0; i--) {
			if (modules.get(i).isHovered(mouseX, mouseY)) {
				return modules.get(i);
			}
		}
		return null;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		HudModule module = hoveredModule(event.x(), event.y());
		if (module != null) {
			if (event.button() == 0) {
				dragging = module;
				dragOffsetX = event.x() - module.getX();
				dragOffsetY = event.y() - module.getY();
			} else if (event.button() == 1) {
				module.getScaleSetting().reset();
			}
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		dragging = null;
		snappedX = false;
		snappedY = false;
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		HudModule module = hoveredModule(mouseX, mouseY);
		if (module != null) {
			module.getScaleSetting().set(module.getScaleSetting().get() + verticalAmount * 0.05);
			return true;
		}
		return false;
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void removed() {
		VoltlizClient.get().saveConfig();
	}
}
