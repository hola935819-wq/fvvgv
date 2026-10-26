package dev.voltliz.client.gui;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.client.ClickGuiModule;
import dev.voltliz.client.util.Animator;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ClickGuiScreen extends Screen {
	private static final int BUTTON_WIDTH = 80;
	private static final int BUTTON_HEIGHT = 16;

	private static String hoveredDescription;

	private final List<Panel> panels = new ArrayList<>();
	private final Animator animator = new Animator();
	private float buttonHover;

	public ClickGuiScreen() {
		super(Component.literal(VoltlizClient.NAME));
		for (Category category : Category.values()) {
			panels.add(new Panel(category, animator));
		}
	}

	public static void setHoveredDescription(String description) {
		hoveredDescription = description;
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		ClickGuiModule gui = Theme.gui();
		if (gui == null || gui.dimBackground.get()) {
			graphics.fillGradient(0, 0, width, height, 0x70000000, 0xA0000000);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		animator.tick();
		hoveredDescription = null;

		for (Panel panel : panels) {
			panel.render(graphics, mouseX, mouseY);
		}

		renderHudButton(graphics, mouseX, mouseY);

		ClickGuiModule gui = Theme.gui();
		if (hoveredDescription != null && (gui == null || gui.descriptions.get())) {
			int textWidth = font.width(hoveredDescription);
			int x = Math.min(mouseX + 10, width - textWidth - 8);
			int y = mouseY + 10;
			RenderUtil.roundedRect(graphics, x - 4, y - 4, textWidth + 8, 16, 0xF0101014);
			RenderUtil.outline(graphics, x - 4, y - 4, textWidth + 8, 16, ColorUtil.withAlpha(Theme.accent(), 0x90));
			graphics.drawString(font, hoveredDescription, x, y, Theme.TEXT, false);
		}

		String brand = VoltlizClient.NAME + " v" + VoltlizClient.version();
		graphics.drawString(font, brand, width - font.width(brand) - 6, height - 14, Theme.accent(), true);
		String hint = "Left click: toggle  |  Right click: settings  |  Drag headers to move";
		graphics.drawString(font, hint, 6, height - 14, Theme.TEXT_DIM, true);
	}

	private void renderHudButton(GuiGraphics graphics, int mouseX, int mouseY) {
		int x = (width - BUTTON_WIDTH) / 2;
		int y = height - BUTTON_HEIGHT - 24;
		boolean hovered = RenderUtil.isInside(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
		buttonHover = animator.approach(buttonHover, hovered ? 1 : 0, 20);

		int accent = Theme.accent();
		RenderUtil.roundedRect(graphics, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, ColorUtil.lerp(Theme.PANEL, ColorUtil.withAlpha(accent, 0xE0), buttonHover));
		RenderUtil.outline(graphics, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, accent);
		String label = "Edit HUD";
		graphics.drawString(font, label, x + (BUTTON_WIDTH - font.width(label)) / 2, y + 4, Theme.TEXT, true);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();

		int buttonX = (width - BUTTON_WIDTH) / 2;
		int buttonY = height - BUTTON_HEIGHT - 24;
		if (button == 0 && RenderUtil.isInside(mouseX, mouseY, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
			minecraft.setScreen(new HudEditorScreen(this));
			return true;
		}

		// Topmost panel first; a clicked panel moves to the front.
		for (int i = panels.size() - 1; i >= 0; i--) {
			Panel panel = panels.get(i);
			if (panel.mouseClicked(mouseX, mouseY, button)) {
				panels.remove(i);
				panels.add(panel);
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		for (Panel panel : panels) {
			panel.mouseReleased(event.x(), event.y(), event.button());
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		for (Panel panel : panels) {
			panel.scroll((int) Math.round(verticalAmount * 14));
		}
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		for (Panel panel : panels) {
			if (panel.keyPressed(event.key())) {
				return true;
			}
		}
		if (VoltlizClient.get().isClickGuiKey(event.key())) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
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
