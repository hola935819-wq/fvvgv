package dev.voltliz.gametest;

import dev.voltliz.client.gui.ClickGuiScreen;
import dev.voltliz.client.gui.HudEditorScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import org.lwjgl.glfw.GLFW;

/** Opens the Click GUI and HUD editor in a real world and takes screenshots of each. */
public class VoltlizClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getClientWorld().waitForChunksRender();
			context.waitTicks(20);
			context.takeScreenshot("voltliz-hud");

			context.getInput().pressKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
			context.waitForScreen(ClickGuiScreen.class);
			context.waitTicks(10);
			context.takeScreenshot("voltliz-clickgui");

			// Right click the first module of the HUD panel to expand its settings.
			context.getInput().setCursorPos(40 * guiScale(context), 42 * guiScale(context));
			context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
			context.waitTicks(10);
			context.takeScreenshot("voltliz-clickgui-settings");

			context.setScreen(() -> new HudEditorScreen(new ClickGuiScreen()));
			context.waitTicks(5);
			context.takeScreenshot("voltliz-hud-editor");

			context.setScreen(() -> null);
			context.getInput().holdKeyFor(options -> options.keyUp, 5);
			context.takeScreenshot("voltliz-keystrokes");
		}
	}

	private static double guiScale(ClientGameTestContext context) {
		return context.computeOnClient(client -> (double) client.getWindow().getGuiScale());
	}
}
