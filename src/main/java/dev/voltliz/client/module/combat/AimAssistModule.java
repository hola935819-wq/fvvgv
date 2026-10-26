package dev.voltliz.client.module.combat;

import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.module.Overlay;
import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ModeSetting;
import dev.voltliz.client.setting.NumberSetting;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.Projection;
import dev.voltliz.client.util.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Gently pulls your view toward the nearest target, like extra mouse movement.
 *
 * <p>Rotation goes through {@code LocalPlayer.turn}, the same call the mouse uses, so it reaches the
 * server in the normal per-tick movement packet and adds no packets of its own. The motion is a
 * critically damped spring evaluated every frame: it eases in, never overshoots, and eases out when
 * you let go or land on the target. Speed is capped, so a single tick never turns more than
 * {@code Speed * 30 / 20} degrees.
 */
public class AimAssistModule extends Module implements Overlay {
	private final NumberSetting speed = add(new NumberSetting("Speed", 4, 1, 10, 0.5));
	private final NumberSetting smoothness = add(new NumberSetting("Smoothness", 75, 0, 100, 5));
	private final NumberSetting fov = add(new NumberSetting("FOV", 70, 10, 180, 5));
	private final NumberSetting range = add(new NumberSetting("Range", 4.5, 3, 8, 0.1));
	private final ModeSetting priority = add(new ModeSetting("Priority", "Angle", "Angle", "Distance", "Health"));
	private final ModeSetting aimPoint = add(new ModeSetting("Aim Point", "Chest", "Chest", "Head", "Closest"));
	private final ModeSetting vertical = add(new ModeSetting("Vertical", "Soft", "Soft", "Full", "Off"));
	private final BooleanSetting clickOnly = add(new BooleanSetting("Click Only", true));
	private final BooleanSetting stopOnTarget = add(new BooleanSetting("Stop On Target", true));
	private final BooleanSetting weaponOnly = add(new BooleanSetting("Weapon Only", false));
	private final BooleanSetting throughWalls = add(new BooleanSetting("Through Walls", false));
	private final BooleanSetting players = add(new BooleanSetting("Players", true));
	private final BooleanSetting hostiles = add(new BooleanSetting("Hostiles", true));
	private final BooleanSetting passives = add(new BooleanSetting("Passives", false));
	private final BooleanSetting invisibles = add(new BooleanSetting("Invisibles", false));
	private final BooleanSetting ignoreTeammates = add(new BooleanSetting("Ignore Teammates", true));
	private final BooleanSetting showFov = add(new BooleanSetting("Show FOV", true));
	private final BooleanSetting targetMarker = add(new BooleanSetting("Target Marker", true));

	private Entity target;
	private double yawVelocity;
	private double pitchVelocity;
	private long lastFrame;
	/** Spring velocity left by the last {@link #springStep} call. */
	private double lastVelocity;

	public AimAssistModule() {
		super("AimAssist", "Smoothly helps your aim toward nearby targets.", Category.COMBAT);
	}

	@Override
	protected void onDisable() {
		target = null;
		yawVelocity = 0;
		pitchVelocity = 0;
	}

	public Entity getTarget() {
		return target;
	}

	@Override
	public void onFrame(Minecraft client) {
		long now = System.nanoTime();
		double dt = lastFrame == 0 ? 0 : Math.min((now - lastFrame) / 1e9, 0.05);
		lastFrame = now;

		LocalPlayer player = client.player;
		if (player == null || client.level == null || client.screen != null || player.isSpectator() || dt <= 0) {
			target = null;
			yawVelocity = 0;
			pitchVelocity = 0;
			return;
		}

		float partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(true);
		Vec3 eye = player.getEyePosition(partial);
		boolean active = (!clickOnly.get() || client.options.keyAttack.isDown())
				&& (!weaponOnly.get() || Targets.isWeapon(player.getMainHandItem()));

		double yawError = 0;
		double pitchError = 0;
		if (active) {
			target = findTarget(client, player, eye, partial);
			if (target != null) {
				Vec3 point = aimPoint(target, eye, partial);
				double dx = point.x - eye.x;
				double dy = point.y - eye.y;
				double dz = point.z - eye.z;
				double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90;
				double pitch = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
				yawError = Mth.wrapDegrees(yaw - player.getYRot());
				pitchError = pitch - player.getXRot();

				boolean onTarget = client.hitResult instanceof EntityHitResult hit && hit.getEntity() == target;
				if (stopOnTarget.get() && onTarget) {
					yawError = 0;
					pitchError = 0;
				}
			}
		} else {
			target = null;
		}

		double pitchScale = vertical.is("Full") ? 1 : vertical.is("Soft") ? 0.45 : 0;
		// Smoothness 0 -> snappy spring, 100 -> very soft spring.
		double omega = Mth.lerp(smoothness.get() / 100.0, 16.0, 3.0);
		double maxSpeed = speed.get() * 30;

		double yawStep = springStep(yawError, yawVelocity, omega, maxSpeed, dt);
		yawVelocity = lastVelocity;
		double pitchStep = springStep(pitchError, pitchVelocity, omega, maxSpeed * pitchScale, dt);
		pitchVelocity = lastVelocity;

		if (pitchScale == 0) {
			pitchStep = 0;
			pitchVelocity = 0;
		}
		if (Math.abs(yawStep) < 1e-4 && Math.abs(pitchStep) < 1e-4) {
			return;
		}
		if (!Double.isFinite(yawStep) || !Double.isFinite(pitchStep)) {
			yawVelocity = 0;
			pitchVelocity = 0;
			return;
		}
		// LocalPlayer.turn takes mouse units: one unit is 0.15 degrees. It also clamps pitch to +-90.
		player.turn(yawStep / 0.15, pitchStep / 0.15);
	}

	/** Advances a critically damped spring toward {@code error} and returns this frame's rotation. */
	private double springStep(double error, double velocity, double omega, double maxSpeed, double dt) {
		double acceleration = omega * omega * error - 2 * omega * velocity;
		velocity = Mth.clamp(velocity + acceleration * dt, -maxSpeed, maxSpeed);
		double step = velocity * dt;
		// Never rotate past the target.
		if (error != 0 && Math.signum(step) == Math.signum(error) && Math.abs(step) > Math.abs(error)) {
			step = error;
			velocity = 0;
		}
		lastVelocity = velocity;
		return step;
	}

	private Entity findTarget(Minecraft client, LocalPlayer player, Vec3 eye, float partial) {
		Entity best = null;
		double bestScore = Double.MAX_VALUE;
		for (Entity entity : client.level.entitiesForRendering()) {
			if (!Targets.isValid(entity, player, players.get(), hostiles.get(), passives.get(), invisibles.get())) {
				continue;
			}
			if (ignoreTeammates.get() && entity.isAlliedTo(player)) {
				continue;
			}
			double distance = player.distanceTo(entity);
			if (distance > range.get() || (!throughWalls.get() && !player.hasLineOfSight(entity))) {
				continue;
			}
			Vec3 center = entity.getPosition(partial).add(0, entity.getBbHeight() / 2, 0);
			double angle = angleTo(player, eye, center);
			if (angle > fov.get() / 2) {
				continue;
			}
			double score = switch (priority.get()) {
				case "Distance" -> distance;
				case "Health" -> ((LivingEntity) entity).getHealth() + ((LivingEntity) entity).getAbsorptionAmount();
				default -> angle;
			};
			// Prefer keeping the current target so the assist doesn't hop between entities.
			if (entity == target) {
				score *= 0.6;
			}
			if (score < bestScore) {
				bestScore = score;
				best = entity;
			}
		}
		return best;
	}

	@Override
	public void renderOverlay(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		if (client.screen != null) {
			return;
		}
		float centerX = graphics.guiWidth() / 2f;
		float centerY = graphics.guiHeight() / 2f;

		if (showFov.get() && fov.get() < 170) {
			// Screen radius of the FOV cone for the current camera field of view.
			double cameraFov = Math.toRadians(client.options.fov().get());
			float radius = (float) (Math.tan(Math.toRadians(fov.get() / 2)) / Math.tan(cameraFov / 2) * centerY);
			if (radius < Math.max(centerX, centerY) * 1.5f) {
				int color = target != null ? Theme.accent() : 0xFFFFFFFF;
				Render2D.roundedOutline(graphics, centerX - radius, centerY - radius, radius * 2, radius * 2, radius, ColorUtil.withAlpha(color, target != null ? 0x70 : 0x30));
			}
		}

		if (targetMarker.get() && target != null && target.isAlive()) {
			Vec3 point = target.getPosition(partialTick).add(0, target.getBbHeight() * 0.62, 0);
			float[] screen = new float[2];
			if (Projection.project(point.x, point.y, point.z, screen)) {
				float pulse = (float) (Math.sin(System.currentTimeMillis() / 180.0) + 1) / 2;
				float size = 7 + pulse * 2;
				Render2D.circle(graphics, screen[0], screen[1], size, ColorUtil.withAlpha(Theme.accent(), 0x28));
				Render2D.roundedOutline(graphics, screen[0] - size, screen[1] - size, size * 2, size * 2, size, ColorUtil.withAlpha(Theme.accent2(), 0xC0));
				Render2D.circle(graphics, screen[0], screen[1], 1.6f, 0xFFFFFFFF);
			}
		}
	}

	private static double angleTo(LocalPlayer player, Vec3 eye, Vec3 point) {
		double dx = point.x - eye.x;
		double dy = point.y - eye.y;
		double dz = point.z - eye.z;
		double yaw = Mth.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90 - player.getYRot());
		double pitch = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))) - player.getXRot();
		return Math.sqrt(yaw * yaw + pitch * pitch);
	}

	private Vec3 aimPoint(Entity entity, Vec3 eye, float partial) {
		Vec3 base = entity.getPosition(partial);
		double height = entity.getBbHeight();
		if (aimPoint.is("Head")) {
			return base.add(0, entity.getEyeHeight() - 0.05, 0);
		}
		if (aimPoint.is("Closest")) {
			AABB box = entity.getBoundingBox().move(base.subtract(entity.position()));
			return new Vec3(
					Mth.clamp(eye.x, box.minX, box.maxX),
					Mth.clamp(eye.y, box.minY + height * 0.15, box.maxY - height * 0.1),
					Mth.clamp(eye.z, box.minZ, box.maxZ));
		}
		return base.add(0, height * 0.62, 0);
	}
}
