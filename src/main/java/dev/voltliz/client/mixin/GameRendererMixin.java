package dev.voltliz.client.mixin;

import dev.voltliz.client.VoltlizClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	/** Runs once per frame, before the world is drawn, so camera-moving modules stay frame-smooth. */
	@Inject(method = "render", at = @At("HEAD"))
	private void voltliz$onFrame(CallbackInfo ci) {
		VoltlizClient voltliz = VoltlizClient.get();
		if (voltliz != null) {
			voltliz.getModuleManager().onFrame(Minecraft.getInstance());
		}
	}
}
