package dev.voltliz.client.mixin;

import dev.voltliz.client.util.CpsTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Inject(method = "onButton", at = @At("HEAD"))
	private void voltliz$countClicks(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
		if (action == GLFW.GLFW_PRESS && Minecraft.getInstance().screen == null) {
			CpsTracker.onClick(buttonInfo.button());
		}
	}
}
