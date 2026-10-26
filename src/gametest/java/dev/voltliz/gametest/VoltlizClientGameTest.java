package dev.voltliz.gametest;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.gui.ClickGuiScreen;
import dev.voltliz.client.gui.GuiState;
import dev.voltliz.client.gui.HudEditorScreen;
import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.combat.TriggerBotModule;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
				int windowWidth = Math.min(480, width - 24);
				int windowHeight = Math.min(300, height - 24);
				double scale = client.getWindow().getGuiScale();
				// Sidebar (118) + content padding (14) + a bit into the card; content starts 46 below the top.
				return new double[] {((width - windowWidth) / 2 + 118 + 14 + 40) * scale, ((height - windowHeight) / 2 + 46 + 18) * scale};
			});
			context.getInput().setCursorPos(card[0], card[1]);
			context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
			context.waitTicks(10);
			context.takeScreenshot("voltliz-clickgui-settings");

			context.getInput().typeChars("ar");
			context.waitTicks(10);
			context.takeScreenshot("voltliz-clickgui-search");

			context.setScreen(() -> new HudEditorScreen(new ClickGuiScreen()));
			context.waitTicks(5);
			context.takeScreenshot("voltliz-hud-editor");

			context.setScreen(() -> null);
			context.getInput().holdKeyFor(options -> options.keyUp, 5);
			context.takeScreenshot("voltliz-keystrokes");

			// Combat tab with the TriggerBot settings expanded.
			context.runOnClient(client -> GuiState.selectedCategory = Category.COMBAT);
			context.setScreen(ClickGuiScreen::new);
			context.waitTicks(10);
			context.getInput().setCursorPos(card[0], card[1]);
			context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
			context.waitTicks(10);
			context.takeScreenshot("voltliz-triggerbot-settings");
			context.setScreen(() -> null);

			// TriggerBot: face a husk standing in front of the player and check that it gets hit.
			singleplayer.getServer().runCommand("difficulty normal");
			singleplayer.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ 0 0");
			singleplayer.getServer().runCommand("execute at @a run summon husk ~ ~ ~2.5 {NoAI:1b,PersistenceRequired:1b,Silent:1b}");
			context.waitTicks(20);
			context.runOnClient(client -> VoltlizClient.get().getModuleManager().get(TriggerBotModule.class).setEnabled(true));
			context.waitTicks(40);
			context.takeScreenshot("voltliz-triggerbot");
			float[] health = context.computeOnClient(client -> {
				for (Entity entity : client.level.entitiesForRendering()) {
					if (entity.getType() == EntityType.HUSK && entity instanceof LivingEntity living) {
						return new float[] {living.getHealth(), living.getMaxHealth()};
					}
				}
				return null;
			});
			context.runOnClient(client -> VoltlizClient.get().getModuleManager().get(TriggerBotModule.class).setEnabled(false));
			if (health == null) {
				throw new AssertionError("The husk for the TriggerBot test never appeared");
			}
			if (health[0] >= health[1]) {
				throw new AssertionError("TriggerBot did not attack the husk (health " + health[0] + "/" + health[1] + ")");
			}
		}
	}
}
