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
		// CI renders on the CPU; capping the frame rate and view distance leaves enough CPU for
		// the integrated server to generate the spawn area before the world load times out.
		context.runOnClient(client -> {
			client.options.framerateLimit().set(30);
			client.options.renderDistance().set(4);
			client.options.simulationDistance().set(5);
		});

		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getClientWorld().waitForChunksRender();
			context.waitTicks(20);
			context.takeScreenshot("voltliz-hud");

			context.getInput().pressKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
			context.waitForScreen(ClickGuiScreen.class);
			context.waitTicks(10);
			context.takeScreenshot("voltliz-clickgui");

			// Right click the first module card to expand its settings.
			double[] card = context.computeOnClient(client -> {
				int width = client.getWindow().getGuiScaledWidth();
				int height = client.getWindow().getGuiScaledHeight();
				int windowWidth = Math.min(440, width - 24);
				int windowHeight = Math.min(280, height - 24);
				double scale = client.getWindow().getGuiScale();
				return new double[] {((width - windowWidth) / 2 + 140) * scale, ((height - windowHeight) / 2 + 52) * scale};
			});
			context.getInput().setCursorPos(card[0], card[1]);
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
}
