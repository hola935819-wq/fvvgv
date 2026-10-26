package dev.voltliz.client.module.combat;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Target rules shared by the combat and render modules. */
public final class Targets {
	private Targets() {
	}

	public static boolean isValid(Entity entity, LocalPlayer self, boolean players, boolean hostiles, boolean passives, boolean invisibles) {
		if (entity == self || !(entity instanceof LivingEntity living) || living.isDeadOrDying()) {
			return false;
		}
		if (living.isInvisible() && !invisibles) {
			return false;
		}
		if (living instanceof Player player) {
			return players && !player.isSpectator();
		}
		if (!(living instanceof Mob)) {
			return false;
		}
		return living.getType().getCategory() == MobCategory.MONSTER ? hostiles : passives;
	}

	/** Main hand, the four armor pieces (head first) and offhand, in display order. */
	public static ItemStack[] equipment(LivingEntity entity) {
		return new ItemStack[] {
				entity.getMainHandItem(),
				entity.getItemBySlot(EquipmentSlot.HEAD),
				entity.getItemBySlot(EquipmentSlot.CHEST),
				entity.getItemBySlot(EquipmentSlot.LEGS),
				entity.getItemBySlot(EquipmentSlot.FEET),
				entity.getOffhandItem()
		};
	}

	public static boolean isWeapon(ItemStack stack) {
		return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(Items.MACE) || stack.is(Items.TRIDENT);
	}
}
