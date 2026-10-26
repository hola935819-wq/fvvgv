package dev.voltliz.client.module;

import dev.voltliz.client.setting.NumberSetting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * A module drawn on the HUD. Positions are stored in GUI-scaled pixels and the
 * content is drawn at (0, 0) inside a translated and scaled pose.
 */
public abstract class HudModule extends Module {
	/** Gap kept between HUD elements and the screen edges. */
	public static final int MARGIN = 8;

	protected final NumberSetting scale = add(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.05));

	private float x;
	private float y;
	protected int width = 20;
	protected int height = 10;

	protected HudModule(String name, String description, float defaultX, float defaultY) {
		super(name, description, Category.HUD);
		this.x = defaultX;
		this.y = defaultY;
	}

	public final void render(GuiGraphics graphics) {
		clampToScreen();

		float s = getScale();
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(s, s);
		renderContent(graphics);
		graphics.pose().popMatrix();
	}

	protected abstract void renderContent(GuiGraphics graphics);

	protected boolean isEditing() {
		return mc().screen instanceof dev.voltliz.client.gui.HudEditorScreen;
	}

	public void clampToScreen() {
		int screenWidth = mc().getWindow().getGuiScaledWidth();
		int screenHeight = mc().getWindow().getGuiScaledHeight();
		x = Mth.clamp(x, MARGIN, Math.max(MARGIN, screenWidth - getScaledWidth() - MARGIN));
		y = Mth.clamp(y, MARGIN, Math.max(MARGIN, screenHeight - getScaledHeight() - MARGIN));
	}

	public float getScale() {
		return scale.getFloat();
	}

	public NumberSetting getScaleSetting() {
		return scale;
	}

	public float getScaledWidth() {
		return width * getScale();
	}

	public float getScaledHeight() {
		return height * getScale();
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public boolean isHovered(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + getScaledWidth() && mouseY >= y && mouseY <= y + getScaledHeight();
	}
}
