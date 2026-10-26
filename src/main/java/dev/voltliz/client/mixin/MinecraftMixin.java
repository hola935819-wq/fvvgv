package dev.voltliz.client.mixin;

import dev.voltliz.client.module.combat.AutoCrystalModule;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	/**
	 * Holding left click on obsidian would normally start mining it, and vanilla refuses to use an item
	 * while mining. While AutoCrystal is working that spot, treat the button as released for mining only.
	 */
	@ModifyVariable(method = "continueAttack", at = @At("HEAD"), argsOnly = true)
	private boolean voltliz$pauseMiningForCrystals(boolean leftClick) {
		return leftClick && !AutoCrystalModule.shouldBlockMining();
	}
}
