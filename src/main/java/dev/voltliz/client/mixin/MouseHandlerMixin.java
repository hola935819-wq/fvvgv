package dev.voltliz.client.mixin;

import dev.voltliz.client.util.CpsTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonInfo;
//?}
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	//? if >=1.21.9 {
	@Inject(method = "onButton", at = @At("HEAD"))
	private void voltliz$countClicks(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
		countClick(buttonInfo.button(), action);
	}
	//?} else {
	/*@Inject(method = "onPress", at = @At("HEAD"))
	private void voltliz$countClicks(long window, int button, int action, int modifiers, CallbackInfo ci) {
		countClick(button, action);
	}
	*///?}

	private static void countClick(int button, int action) {
		if (action == GLFW.GLFW_PRESS && Minecraft.getInstance().screen == null) {
			CpsTracker.onClick(button);
		}
	}
}
