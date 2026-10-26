package dev.voltliz.client.gui.component;

import dev.voltliz.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public abstract class GuiComponent {
	public int x;
	public int y;
	public int width;

	public abstract int getHeight();

	public abstract void render(GuiGraphics graphics, int mouseX, int mouseY);

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	public void mouseReleased(double mouseX, double mouseY, int button) {
	}

	public boolean keyPressed(int key) {
		return false;
	}

	public boolean isHovered(double mouseX, double mouseY) {
		return RenderUtil.isInside(mouseX, mouseY, x, y, width, getHeight());
	}

	protected static Font font() {
		return Minecraft.getInstance().font;
	}
}
