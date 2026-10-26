package dev.voltliz.client.module.combat;

import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ModeSetting;
import dev.voltliz.client.setting.NumberSetting;
import dev.voltliz.client.util.CpsTracker;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Attacks the entity under the crosshair by pressing the attack key for you: the click goes through
 * vanilla's own input handling (swing, cooldown, miss timer), exactly like a real left click.
 * At most one click per tick, so it never sends more than a player clicking could.
 */
public class TriggerBotModule extends Module {
	private final ModeSetting timing = add(new ModeSetting("Timing", "Cooldown", "Cooldown", "CPS"));
	private final NumberSetting cooldown = add(new NumberSetting("Cooldown %", 100, 50, 100, 1));
	private final NumberSetting cps = add(new NumberSetting("CPS", 12, 1, 20, 1));
	private final NumberSetting reaction = add(new NumberSetting("Reaction ms", 0, 0, 300, 10));
	private final BooleanSetting players = add(new BooleanSetting("Players", true));
	private final BooleanSetting hostiles = add(new BooleanSetting("Hostiles", true));
	private final BooleanSetting passives = add(new BooleanSetting("Passives", false));
	private final BooleanSetting invisibles = add(new BooleanSetting("Invisibles", true));
	private final BooleanSetting weaponOnly = add(new BooleanSetting("Weapon Only", false));
	private final BooleanSetting critsOnly = add(new BooleanSetting("Crits Only", false));

	private Entity target;
	private long targetSince;
	private long lastClick;

	public TriggerBotModule() {
		super("TriggerBot", "Clicks for you when your crosshair is on a target.", Category.COMBAT);
	}

	@Override
	protected void onDisable() {
		target = null;
	}

	/** Runs at the start of the tick so vanilla handles the click in this same tick. */
	@Override
	public void onPreTick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null || client.screen != null || client.gameMode == null || player.isSpectator() || player.isUsingItem()) {
			target = null;
			return;
		}

		Entity looked = client.hitResult instanceof EntityHitResult hit ? hit.getEntity() : null;
		if (looked == null || !isValidTarget(looked, player)) {
			target = null;
			return;
		}

		long now = System.currentTimeMillis();
		if (looked != target) {
			target = looked;
			targetSince = now;
		}
		if (now - targetSince < reaction.getInt()) {
			return;
		}
		if (weaponOnly.get() && !isWeapon(player.getMainHandItem())) {
			return;
		}
		if (critsOnly.get() && !canCrit(player)) {
			return;
		}

		boolean ready = timing.is("Cooldown")
				? player.getAttackStrengthScale(0.5f) >= cooldown.get() / 100.0
				: now - lastClick >= 1000.0 / cps.get();
		if (ready) {
			lastClick = now;
			KeyMapping.click(KeyBindingHelper.getBoundKeyOf(client.options.keyAttack));
			CpsTracker.onClick(0);
		}
	}

	private boolean isValidTarget(Entity entity, LocalPlayer self) {
		if (entity == self || !(entity instanceof LivingEntity living) || living.isDeadOrDying()) {
			return false;
		}
		if (living.isInvisible() && !invisibles.get()) {
			return false;
		}
		if (living instanceof Player) {
			return players.get();
		}
		if (!(living instanceof Mob)) {
			return false;
		}
		return living.getType().getCategory() == MobCategory.MONSTER ? hostiles.get() : passives.get();
	}

	private static boolean isWeapon(ItemStack stack) {
		return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(Items.MACE) || stack.is(Items.TRIDENT);
	}

	/** Vanilla only crits while falling, off the ground, out of water and not climbing. */
	private static boolean canCrit(LocalPlayer player) {
		return player.fallDistance > 0 && !player.onGround() && !player.onClimbable() && !player.isInWater();
	}
}
