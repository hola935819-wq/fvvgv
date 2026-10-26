package dev.voltliz.client.module.hud;

import dev.voltliz.client.module.HudModule;
import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.NumberSetting;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Shows who you are fighting: face, name, health and whether you are winning. */
public class TargetHudModule extends HudModule {
	private static final int WIDTH = 156;
	private static final int HEIGHT = 62;
	private static final int WIN = 0xFF3DFF8A;
	private static final int LOSE = 0xFFFF4D5E;
	private static final int EVEN = 0xFFFFC23D;

	private static LivingEntity target;
	private static long lastAttack;

	private final NumberSetting timeout = add(new NumberSetting("Timeout", 4, 1, 15, 0.5));
	private final BooleanSetting showArmor = add(new BooleanSetting("Count Armor", true));
	private final BooleanSetting showItems = add(new BooleanSetting("Show Items", true));
	private final BooleanSetting hurtEffect = add(new BooleanSetting("Hurt Effect", true));

	private float shownHealth = -1;
	private float shownRatio = 0.5f;
	private float visibility;
	private long lastFrame;

	public TargetHudModule() {
		super("Target HUD", "Shows the player you attack and who is winning.", 10_000, 160);
		setEnabled(true);
	}

	/** Called when the local player attacks an entity. */
	public static void onAttack(Entity entity) {
		if (entity instanceof LivingEntity living) {
			target = living;
			lastAttack = System.currentTimeMillis();
		}
	}

	@Override
	protected void renderContent(GuiGraphics graphics) {
		width = WIDTH;
		height = HEIGHT;
		LocalPlayer self = mc().player;
		if (self == null) {
			return;
		}

		long now = System.currentTimeMillis();
		float dt = lastFrame == 0 ? 0 : Math.min((now - lastFrame) / 1000f, 0.1f);
		lastFrame = now;

		LivingEntity shown = target;
		boolean active = shown != null && shown.isAlive() && now - lastAttack < timeout.get() * 1000
				&& shown.level() == self.level() && self.distanceTo(shown) < 16;
		if (!active) {
			shown = isEditing() ? self : null;
		}
		visibility = approach(visibility, shown != null ? 1 : 0, dt * 6);
		if (shown == null) {
			shownHealth = -1;
			if (visibility <= 0.01f) {
				return;
			}
			shown = target != null ? target : self;
		}

		float theirHealth = shown.getHealth() + shown.getAbsorptionAmount();
		float myHealth = self.getHealth() + self.getAbsorptionAmount();
		float theirMax = Math.max(1, shown.getMaxHealth() + shown.getAbsorptionAmount());
		shownHealth = shownHealth < 0 ? theirHealth : approach(shownHealth, theirHealth, dt * Math.max(8, Math.abs(theirHealth - shownHealth) * 6));

		float mine = effective(self, myHealth);
		float theirs = effective(shown, theirHealth);
		float ratio = mine + theirs <= 0 ? 0.5f : mine / (mine + theirs);
		shownRatio = approach(shownRatio, ratio, dt * 2.5f);
		String status = ratio > 0.56f ? "WINNING" : ratio < 0.44f ? "LOSING" : "EVEN";
		int statusColor = ratio > 0.56f ? WIN : ratio < 0.44f ? LOSE : EVEN;

		float previousAlpha = Render2D.getAlpha();
		Render2D.setAlpha(previousAlpha * visibility);

		RenderUtil.hudBackground(graphics, 0, 0, WIDTH, HEIGHT);
		Render2D.horizontalGradient(graphics, 6, 0, WIDTH - 12, 1, 0, Theme.accent(), Theme.accent2());

		// Face, which flashes red and shakes a little when the target is hurt.
		int face = 30;
		float hurt = hurtEffect.get() ? Math.max(0, shown.hurtTime / 10f) : 0;
		float shake = hurt * 1.5f * (float) Math.sin(now / 25.0);
		int faceX = 7 + Math.round(shake);
		int faceY = 7;
		Render2D.shadow(graphics, faceX, faceY, face, face, 6, 4, 0x50, statusColor);
		if (shown instanceof AbstractClientPlayer player) {
			PlayerFaceRenderer.draw(graphics, player.getSkin(), faceX, faceY, face);
			Render2D.roundedOutline(graphics, faceX - 1, faceY - 1, face + 2, face + 2, 4, ColorUtil.withAlpha(statusColor, 0xC0));
		} else {
			Render2D.verticalGradient(graphics, faceX, faceY, face, face, 6, Theme.accent(), Theme.accent2());
			String initial = shown.getName().getString().substring(0, 1).toUpperCase();
			VText.drawScaled(graphics, initial, faceX + face / 2f - VText.width(initial, true) * 0.75f, faceY + 8, 0xFFFFFFFF, true, 1.5f);
		}
		if (hurt > 0) {
			Render2D.roundedRect(graphics, faceX, faceY, face, face, 4, ColorUtil.withAlpha(LOSE, Math.round(hurt * 0x90)));
		}

		// Name and status pill.
		int textX = faceX + face + 7;
		int statusWidth = VText.width(status, true) + 10;
		String name = VText.trim(shown.getName().getString(), WIDTH - textX - statusWidth - 10, true);
		VText.draw(graphics, name, textX, 7, Theme.TEXT, true);
		float pillX = WIDTH - statusWidth - 6;
		Render2D.roundedRect(graphics, pillX, 6, statusWidth, 11, 5.5f, ColorUtil.withAlpha(statusColor, 0x30));
		VText.drawScaled(graphics, status, pillX + 5, 8.5f, statusColor, true, 0.8f);

		// Their health bar.
		int barX = textX;
		int barWidth = WIDTH - barX - 7;
		float fraction = Math.max(0, Math.min(1, shownHealth / theirMax));
		Render2D.roundedRect(graphics, barX, 21, barWidth, 5, 2.5f, Theme.FIELD);
		if (fraction > 0) {
			int healthColor = ColorUtil.lerp(LOSE, WIN, fraction);
			Render2D.horizontalGradient(graphics, barX, 21, Math.max(5, barWidth * fraction), 5, 2.5f, ColorUtil.lerp(healthColor, 0xFFFFFFFF, 0.15f), healthColor);
		}
		String healthText = String.format("%.1f HP", theirHealth);
		String distance = String.format("%.1fm", self.distanceTo(shown));
		if (shown instanceof AbstractClientPlayer player && mc().getConnection() != null) {
			PlayerInfo info = mc().getConnection().getPlayerInfo(player.getUUID());
			if (info != null) {
				distance = info.getLatency() + "ms  ·  " + distance;
			}
		}
		VText.drawScaled(graphics, healthText, barX, 29, Theme.TEXT, true, 0.75f);
		VText.drawScaled(graphics, distance, WIDTH - 7 - VText.width(distance) * 0.75f, 29, Theme.TEXT_DIM, false, 0.75f);

		// Their armor and weapon.
		if (showItems.get()) {
			int itemX = 7;
			for (ItemStack stack : equipment(shown)) {
				if (!stack.isEmpty()) {
					graphics.pose().pushMatrix();
					graphics.pose().translate(itemX, 41);
					graphics.pose().scale(0.75f, 0.75f);
					graphics.renderItem(stack, 0, 0);
					graphics.pose().popMatrix();
					itemX += 14;
				}
			}
			if (itemX == 7) {
				VText.drawScaled(graphics, "No armor", 7, 44, Theme.TEXT_DIM, false, 0.7f);
			}
		}

		// Tug-of-war bar: your share of the fight on the left, theirs on the right.
		int tugY = 55;
		int tugWidth = WIDTH - 14;
		float split = tugWidth * shownRatio;
		Render2D.roundedRect(graphics, 7, tugY, tugWidth, 4, 2, ColorUtil.withAlpha(LOSE, 0xB0));
		Render2D.horizontalGradient(graphics, 7, tugY, Math.max(4, split), 4, 2, Theme.accent(), Theme.accent2());
		Render2D.circle(graphics, 7 + split, tugY + 2, 2.6f, 0xFFFFFFFF);

		Render2D.setAlpha(previousAlpha);
	}

	private static ItemStack[] equipment(LivingEntity entity) {
		return new ItemStack[] {
				entity.getMainHandItem(),
				entity.getItemBySlot(EquipmentSlot.HEAD),
				entity.getItemBySlot(EquipmentSlot.CHEST),
				entity.getItemBySlot(EquipmentSlot.LEGS),
				entity.getItemBySlot(EquipmentSlot.FEET),
				entity.getOffhandItem()
		};
	}

	/** Health weighted by armor, so a full netherite player counts as tougher than a naked one. */
	private float effective(LivingEntity entity, float health) {
		if (!showArmor.get()) {
			return health;
		}
		return health * (1 + Math.min(20, entity.getArmorValue()) / 25f);
	}

	private static float approach(float value, float goal, float step) {
		return value < goal ? Math.min(goal, value + step) : Math.max(goal, value - step);
	}
}
