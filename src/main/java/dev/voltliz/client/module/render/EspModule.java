package dev.voltliz.client.module.render;

import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.module.Overlay;
import dev.voltliz.client.module.combat.Targets;
import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ModeSetting;
import dev.voltliz.client.setting.NumberSetting;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.Projection;
import dev.voltliz.client.util.Theme;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Shared target filters, colors and box projection for the 2D and 3D ESP. */
public abstract class EspModule extends Module implements Overlay {
	protected final ModeSetting color = add(new ModeSetting("Color", "Theme", "Theme", "Health", "Team"));
	protected final NumberSetting range = add(new NumberSetting("Range", 64, 8, 256, 8));
	protected final BooleanSetting players = add(new BooleanSetting("Players", true));
	protected final BooleanSetting hostiles = add(new BooleanSetting("Hostiles", true));
	protected final BooleanSetting passives = add(new BooleanSetting("Passives", false));
	protected final BooleanSetting invisibles = add(new BooleanSetting("Invisibles", true));
	protected final BooleanSetting items = add(new BooleanSetting("Items", true));
	protected final BooleanSetting ignoreTeammates = add(new BooleanSetting("Ignore Teammates", false));

	protected EspModule(String name, String description) {
		super(name, description, Category.RENDER);
	}

	@Override
	public void renderOverlay(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || client.level == null) {
			return;
		}

		List<LivingEntity> targets = new ArrayList<>();
		double maxDistance = range.get() * range.get();
		for (Entity entity : client.level.entitiesForRendering()) {
			if (entity instanceof LivingEntity living && player.distanceToSqr(entity) <= maxDistance
					&& Targets.isValid(entity, player, players.get(), hostiles.get(), passives.get(), invisibles.get())
					&& !(ignoreTeammates.get() && entity.isAlliedTo(player))) {
				targets.add(living);
			}
		}
		// Far first, so nearer boxes draw on top.
		targets.sort(Comparator.comparingDouble((LivingEntity living) -> -player.distanceToSqr(living)));
		for (LivingEntity living : targets) {
			renderEntity(graphics, living, player, partialTick);
		}
	}

	protected abstract void renderEntity(GuiGraphics graphics, LivingEntity entity, LocalPlayer player, float partialTick);

	/** The entity's hitbox at its interpolated position, slightly padded. */
	protected static AABB box(Entity entity, float partialTick) {
		Vec3 offset = entity.getPosition(partialTick).subtract(entity.position());
		return entity.getBoundingBox().move(offset).inflate(0.06, 0.04, 0.06);
	}

	/** Projects the 8 corners of {@code box}; returns null if any is behind the camera. */
	protected static float[][] corners(AABB box) {
		float[][] points = new float[8][2];
		for (int i = 0; i < 8; i++) {
			double x = (i & 1) == 0 ? box.minX : box.maxX;
			double y = (i & 2) == 0 ? box.minY : box.maxY;
			double z = (i & 4) == 0 ? box.minZ : box.maxZ;
			if (!Projection.project(x, y, z, points[i])) {
				return null;
			}
		}
		return points;
	}

	/**
	 * Draws the target's main hand, armor and offhand in a row centered on {@code centerX}, ending
	 * at {@code bottom}, each with a small durability bar. Returns the row's top edge.
	 */
	protected float drawEquipment(GuiGraphics graphics, LivingEntity entity, float centerX, float bottom) {
		if (!items.get()) {
			return bottom;
		}
		List<ItemStack> stacks = new ArrayList<>();
		for (ItemStack stack : Targets.equipment(entity)) {
			if (!stack.isEmpty()) {
				stacks.add(stack);
			}
		}
		if (stacks.isEmpty()) {
			return bottom;
		}

		float scale = 0.625f;
		float slot = 16 * scale + 2;
		float rowWidth = stacks.size() * slot + 4;
		float rowHeight = 16 * scale + 6;
		float x = centerX - rowWidth / 2;
		float y = bottom - rowHeight;
		Render2D.roundedRect(graphics, x, y, rowWidth, rowHeight, 4, Theme.HUD_BACKGROUND);

		float itemX = x + 3;
		for (ItemStack stack : stacks) {
			graphics.pose().pushMatrix();
			graphics.pose().translate(itemX, y + 2);
			graphics.pose().scale(scale, scale);
			graphics.renderItem(stack, 0, 0);
			graphics.pose().popMatrix();

			if (stack.isDamageableItem() && stack.getMaxDamage() > 0) {
				float left = 1 - (float) stack.getDamageValue() / stack.getMaxDamage();
				float barWidth = 16 * scale;
				Render2D.rect(graphics, itemX, y + rowHeight - 3, barWidth, 1.5f, 0xA0000000);
				Render2D.rect(graphics, itemX, y + rowHeight - 3, barWidth * left, 1.5f, healthColor(left));
			} else if (stack.getCount() > 1) {
				String count = String.valueOf(stack.getCount());
				VText.drawScaled(graphics, count, itemX + 16 * scale - VText.width(count) * 0.55f, y + rowHeight - 6, Theme.TEXT, true, 0.55f);
			}
			itemX += slot;
		}
		return y;
	}

	protected static float healthFraction(LivingEntity entity) {
		return Math.max(0, Math.min(1, entity.getHealth() / Math.max(1, entity.getMaxHealth())));
	}

	/** Green when full, through amber, to red when low. */
	protected static int healthColor(float fraction) {
		return fraction > 0.5f
				? ColorUtil.lerp(0xFFFFC23D, 0xFF3DFF8A, (fraction - 0.5f) * 2)
				: ColorUtil.lerp(0xFFFF4D5E, 0xFFFFC23D, fraction * 2);
	}

	protected int topColor(LivingEntity entity) {
		if (color.is("Health")) {
			return healthColor(healthFraction(entity));
		}
		return color.is("Team") ? teamColor(entity, Theme.accent()) : Theme.accent();
	}

	protected int bottomColor(LivingEntity entity) {
		if (color.is("Health")) {
			return healthColor(healthFraction(entity));
		}
		return color.is("Team") ? ColorUtil.lerp(teamColor(entity, Theme.accent2()), 0xFF000000, 0.25f) : Theme.accent2();
	}

	/** The scoreboard team color, or {@code fallback} when the entity has no colored team. */
	private static int teamColor(LivingEntity entity, int fallback) {
		int rgb = entity.getTeamColor() & 0xFFFFFF;
		return rgb == 0xFFFFFF ? fallback : 0xFF000000 | rgb;
	}
}
