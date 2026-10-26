package dev.voltliz.gametest;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.gui.ClickGuiScreen;
import dev.voltliz.client.gui.GuiState;
import dev.voltliz.client.gui.HudEditorScreen;
import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.module.combat.AimAssistModule;
import dev.voltliz.client.module.combat.AutoCrystalModule;
import dev.voltliz.client.module.combat.TriggerBotModule;
import dev.voltliz.client.module.render.Esp2DModule;
import dev.voltliz.client.module.render.Esp3DModule;
import dev.voltliz.client.setting.Setting;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
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
			singleplayer.getServer().runCommand("item replace entity @e[type=husk] weapon.mainhand with iron_sword");
			singleplayer.getServer().runCommand("item replace entity @e[type=husk] armor.head with iron_helmet");
			singleplayer.getServer().runCommand("item replace entity @e[type=husk] armor.chest with diamond_chestplate");
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

			// AimAssist: look 25 degrees away from the husk and check the view eases back toward it.
			singleplayer.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ 25 0");
			context.waitTicks(5);
			context.runOnClient(client -> {
				AimAssistModule aimAssist = VoltlizClient.get().getModuleManager().get(AimAssistModule.class);
				setSetting(aimAssist, "Click Only", false);
				setSetting(aimAssist, "Range", 8.0);
				aimAssist.setEnabled(true);
			});
			context.waitTicks(40);
			context.takeScreenshot("voltliz-aimassist");
			float yaw = context.computeOnClient(client -> Mth.wrapDegrees(client.player.getYRot()));
			context.runOnClient(client -> VoltlizClient.get().getModuleManager().get(AimAssistModule.class).setEnabled(false));
			if (Math.abs(yaw) > 15) {
				throw new AssertionError("AimAssist did not turn toward the husk (yaw " + yaw + ", husk at 0)");
			}

			// ESP: a second husk off to the side, then the 2D and 3D styles.
			singleplayer.getServer().runCommand("execute at @a run summon husk ~3 ~ ~7 {NoAI:1b,PersistenceRequired:1b,Silent:1b,Rotation:[160f,0f]}");
			singleplayer.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~-3 10 5");
			context.waitTicks(10);
			context.runOnClient(client -> {
				Esp2DModule esp = VoltlizClient.get().getModuleManager().get(Esp2DModule.class);
				setSetting(esp, "Tracers", true);
				esp.setEnabled(true);
			});
			context.waitTicks(5);
			context.takeScreenshot("voltliz-esp-2d");
			context.runOnClient(client -> {
				VoltlizClient.get().getModuleManager().get(Esp2DModule.class).setEnabled(false);
				VoltlizClient.get().getModuleManager().get(Esp3DModule.class).setEnabled(true);
			});
			context.waitTicks(5);
			context.takeScreenshot("voltliz-esp-3d");

			// AutoCrystal: stand on an obsidian floor with crystals in the hotbar, hold left click and check
			// that crystals keep getting placed and broken. Resistance V and full explosion knockback
			// resistance keep the player alive and in place.
			context.runOnClient(client -> VoltlizClient.get().getModuleManager().get(Esp3DModule.class).setEnabled(false));
			singleplayer.getServer().runCommand("kill @e[type=husk]");
			singleplayer.getServer().runCommand("gamemode survival @a");
			singleplayer.getServer().runCommand("effect give @a resistance infinite 4 true");
			singleplayer.getServer().runCommand("attribute @a minecraft:explosion_knockback_resistance base set 1");
			singleplayer.getServer().runCommand("execute at @a run fill ~-4 ~-1 ~-4 ~4 ~-1 ~6 obsidian");
			singleplayer.getServer().runCommand("execute at @a run fill ~-4 ~ ~-4 ~4 ~3 ~6 air");
			singleplayer.getServer().runCommand("item replace entity @a hotbar.0 with diamond_sword");
			singleplayer.getServer().runCommand("item replace entity @a hotbar.2 with end_crystal 64");
			singleplayer.getServer().runCommand("execute as @a at @s run tp @s ~ ~ ~ 0 50");
			context.waitTicks(10);
			context.runOnClient(client -> {
				client.player.getInventory().setSelectedSlot(0);
				VoltlizClient.get().getModuleManager().get(AutoCrystalModule.class).setEnabled(true);
			});
			context.waitTick();

			Set<Integer> crystalsSeen = new HashSet<>();
			context.getInput().holdMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
			for (int tick = 0; tick < 60; tick++) {
				context.waitTick();
				crystalsSeen.addAll(context.computeOnClient(VoltlizClientGameTest::crystalIds));
				if (tick == 30) {
					context.takeScreenshot("voltliz-autocrystal");
				}
			}
			context.getInput().releaseMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
			context.waitTicks(10);
			int selectedSlot = context.computeOnClient(client -> client.player.getInventory().getSelectedSlot());
			context.runOnClient(client -> VoltlizClient.get().getModuleManager().get(AutoCrystalModule.class).setEnabled(false));
			// A new crystal can only go down once the last one is gone, so several distinct crystals
			// means it both placed and broke them.
			if (crystalsSeen.size() < 3) {
				throw new AssertionError("AutoCrystal only produced " + crystalsSeen.size() + " crystals in 3 seconds");
			}
			if (selectedSlot != 0) {
				throw new AssertionError("AutoCrystal did not switch back to the original slot (now " + selectedSlot + ")");
			}
		}
	}

	private static List<Integer> crystalIds(Minecraft client) {
		List<Integer> ids = new ArrayList<>();
		for (Entity entity : client.level.entitiesForRendering()) {
			if (entity instanceof EndCrystal) {
				ids.add(entity.getId());
			}
		}
		return ids;
	}

	@SuppressWarnings("unchecked")
	private static void setSetting(Module module, String name, Object value) {
		for (Setting<?> setting : module.getSettings()) {
			if (setting.getName().equals(name)) {
				((Setting<Object>) setting).set(value);
				return;
			}
		}
		throw new AssertionError("No setting " + name + " on " + module.getName());
	}
}
