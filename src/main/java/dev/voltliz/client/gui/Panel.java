package dev.voltliz.client.gui;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.gui.component.ModuleButton;
import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.util.Animator;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** One draggable column of the Click GUI, holding the modules of a category. */
public class Panel {
	public static final int WIDTH = 114;
	public static final int HEADER = 18;

	private final Category category;
	private final GuiState.PanelState state;
	private final Animator animator;
	private final List<ModuleButton> buttons = new ArrayList<>();

	private boolean dragging;
	private double dragOffsetX;
	private double dragOffsetY;
	private float openProgress;

	public Panel(Category category, Animator animator) {
		this.category = category;
		this.state = GuiState.get(category);
		this.animator = animator;
		this.openProgress = state.open ? 1 : 0;

		for (Module module : VoltlizClient.get().getModuleManager().getModules(category)) {
			buttons.add(new ModuleButton(module, animator));
		}
	}

	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		if (dragging) {
			state.x = (int) (mouseX - dragOffsetX);
			state.y = (int) (mouseY - dragOffsetY);
		}
		openProgress = animator.approach(openProgress, state.open ? 1 : 0, 14);

		int x = state.x;
		int y = state.y;
		int contentHeight = 0;
		for (ModuleButton button : buttons) {
			contentHeight += button.getHeight();
		}
		int shown = Math.round((contentHeight + 2) * openProgress);

		RenderUtil.roundedRect(graphics, x, y, WIDTH, HEADER + shown, Theme.PANEL);

		Font font = Minecraft.getInstance().font;
		int accent = Theme.accent();
		graphics.fill(x, y + HEADER - 1, x + WIDTH, y + HEADER, accent);
		graphics.drawString(font, category.getDisplayName(), x + 7, y + 5, Theme.TEXT, true);
		String count = String.valueOf(buttons.size());
		graphics.drawString(font, count, x + WIDTH - 7 - font.width(count), y + 5, Theme.TEXT_DIM, false);

		if (shown > 0) {
			graphics.enableScissor(x, y + HEADER, x + WIDTH, y + HEADER + shown);
			int buttonY = y + HEADER + 1;
			for (ModuleButton button : buttons) {
				button.x = x + 2;
				button.y = buttonY;
				button.width = WIDTH - 4;
				button.render(graphics, mouseX, mouseY);
				buttonY += button.getHeight();
			}
			graphics.disableScissor();
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (RenderUtil.isInside(mouseX, mouseY, state.x, state.y, WIDTH, HEADER)) {
			if (button == 0) {
				dragging = true;
				dragOffsetX = mouseX - state.x;
				dragOffsetY = mouseY - state.y;
			} else if (button == 1) {
				state.open = !state.open;
			}
			return true;
		}

		if (!state.open || openProgress < 0.95f) {
			return false;
		}
		for (ModuleButton moduleButton : buttons) {
			if (moduleButton.isHovered(mouseX, mouseY) && moduleButton.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
		}
		return false;
	}

	public void mouseReleased(double mouseX, double mouseY, int button) {
		dragging = false;
		for (ModuleButton moduleButton : buttons) {
			moduleButton.mouseReleased(mouseX, mouseY, button);
		}
	}

	public boolean keyPressed(int key) {
		for (ModuleButton button : buttons) {
			if (button.keyPressed(key)) {
				return true;
			}
		}
		return false;
	}

	public void scroll(int amount) {
		state.y += amount;
	}
}
