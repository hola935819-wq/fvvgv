package dev.voltliz.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.voltliz.client.util.Projection;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	/**
	 * Keeps this frame's camera matrices so HUD overlays (ESP) can project world positions.
	 * Arguments are picked by type because renderLevel's parameter list differs between versions;
	 * the view matrix always comes first and the projection matrix second.
	 */
	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void voltliz$captureMatrices(CallbackInfo ci, @Local(argsOnly = true) Camera camera,
			@Local(argsOnly = true, ordinal = 0) Matrix4f viewMatrix, @Local(argsOnly = true, ordinal = 1) Matrix4f projectionMatrix) {
		//? if >=1.21.10 {
		Projection.capture(camera.position(), viewMatrix, projectionMatrix);
		//?} else {
		/*Projection.capture(camera.getPosition(), viewMatrix, projectionMatrix);
		*///?}
	}
}
