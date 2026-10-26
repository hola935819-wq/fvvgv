package dev.voltliz.client.gui;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.module.HudModule;
import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
import dev.voltliz.client.util.ColorUtil;
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
		graphics.fill(0, 0, width, height, 0x40000000);
		// A faint grid to line elements up against.
		for (int x = 0; x < width; x += 20) {
			graphics.fill(x, 0, x + 1, height, 0x0AFFFFFF);
		}
		for (int y = 0; y < height; y += 20) {
			graphics.fill(0, y, width, y + 1, 0x0AFFFFFF);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		int accent = Theme.accent();
		if (dragging != null) {
			drag(mouseX, mouseY);
			if (snappedX) {
				graphics.fill(width / 2, 0, width / 2 + 1, height, ColorUtil.withAlpha(accent, 0x90));
			}
			if (snappedY) {
				graphics.fill(0, height / 2, width, height / 2 + 1, ColorUtil.withAlpha(accent, 0x90));
			}
		}

		HudModule hovered = hoveredModule(mouseX, mouseY);
		for (HudModule module : modules()) {
			float x = module.getX();
			float y = module.getY();
			float w = module.getScaledWidth();
			float h = module.getScaledHeight();
			boolean active = module == dragging || (dragging == null && module == hovered);

			int border = active ? accent : 0x50FFFFFF;
			Render2D.roundedOutline(graphics, x - 2, y - 2, w + 4, h + 4, 5, border);
			if (active) {
				Render2D.roundedRect(graphics, x - 1, y - 1, w + 2, h + 2, 4, ColorUtil.withAlpha(accent, 0x22));
				String label = module.getName() + "  " + Math.round(module.getScale() * 100) + "%";
				int labelWidth = VText.width(label, true) + 10;
				float labelY = y > 18 ? y - 17 : y + h + 4;
				Render2D.roundedRect(graphics, x - 2, labelY, labelWidth, 13, 4, accent);
				VText.draw(graphics, label, x + 3, labelY + 2, 0xFFFFFFFF, true);
			}
		}

		// Help card at the bottom center.
		String title = "HUD Editor";
		String help = "Drag to move  ·  Scroll to resize  ·  Right click to reset  ·  Esc to go back";
		int cardWidth = VText.width(help) + 24;
		int cardX = (width - cardWidth) / 2;
		int cardY = height - 46;
		Render2D.shadow(graphics, cardX, cardY, cardWidth, 32, 8, 6, 0x60);
		Render2D.verticalGradient(graphics, cardX, cardY, cardWidth, 32, 8, Theme.WINDOW_TOP, Theme.WINDOW_BOTTOM);
		Render2D.roundedOutline(graphics, cardX, cardY, cardWidth, 32, 8, ColorUtil.withAlpha(accent, 0x55));
		VText.drawCentered(graphics, title, width / 2f, cardY + 6, accent, true);
		VText.drawCentered(graphics, help, width / 2f, cardY + 18, Theme.TEXT_DIM, false);
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
