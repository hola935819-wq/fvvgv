package dev.voltliz.client.gui.component;

import com.mojang.blaze3d.platform.InputConstants;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.setting.Setting;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

/** Module keybind. Click, then press a key; Escape or Backspace clears it. */
public class BindComponent extends SettingComponent<Setting<?>> {
	private final Module module;
	private boolean listening;

	public BindComponent(Module module) {
		super(null);
		this.module = module;
	}

	public boolean isListening() {
		return listening;
	}

	@Override
	public int getHeight() {
		return 17;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		drawName(graphics, "Keybind", y + 4);

		String text;
		if (listening) {
			text = "Press a key...";
		} else if (module.getKey() < GLFW.GLFW_KEY_SPACE) {
			text = "None";
		} else {
			text = InputConstants.Type.KEYSYM.getOrCreate(module.getKey()).getDisplayName().getString();
		}
		drawPill(graphics, text, y + 1, listening ? Theme.accent() : Theme.TEXT);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			listening = !listening;
			return true;
		}
		if (button == 1) {
			module.setKey(-1);
			listening = false;
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(int key) {
		if (!listening) {
			return false;
		}
		boolean clear = key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE;
		module.setKey(clear ? -1 : key);
		listening = false;
		return true;
	}
}
