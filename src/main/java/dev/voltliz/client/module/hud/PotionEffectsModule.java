package dev.voltliz.client.module.hud;

import dev.voltliz.client.module.HudModule;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

public class PotionEffectsModule extends HudModule {
	private final BooleanSetting background = add(new BooleanSetting("Background", true));
	private final BooleanSetting effectColors = add(new BooleanSetting("Effect Colors", true));

	public PotionEffectsModule() {
		super("Potion Effects", "Lists your active effects and their time left.", 10_000, 120);
		setEnabled(true);
	}

	@Override
	protected void renderContent(GuiGraphics graphics) {
		LocalPlayer player = mc().player;
		List<MobEffectInstance> effects = player != null ? new ArrayList<>(player.getActiveEffects()) : List.of();

		if (effects.isEmpty()) {
			width = 70;
			height = 12;
			if (isEditing()) {
				VText.draw(graphics, "Potion Effects", 2, 2, Theme.TEXT_DIM);
			}
			return;
		}

		List<String> names = new ArrayList<>();
		List<String> times = new ArrayList<>();
		int nameWidth = 0;
		int timeWidth = 0;
		for (MobEffectInstance effect : effects) {
			String name = effect.getEffect().value().getDisplayName().getString();
			if (effect.getAmplifier() > 0) {
				name += " " + Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)).getString();
			}
			String time = effect.isInfiniteDuration() ? "**:**" : formatTicks(effect.getDuration());
			names.add(name);
			times.add(time);
			nameWidth = Math.max(nameWidth, VText.width(name, true));
			timeWidth = Math.max(timeWidth, VText.width(time));
		}

		int padding = background.get() ? 5 : 0;
		int lineHeight = VText.HEIGHT + 3;
		width = 8 + nameWidth + 10 + timeWidth + padding * 2;
		height = effects.size() * lineHeight - 2 + padding * 2;

		if (background.get()) {
			RenderUtil.hudBackground(graphics, 0, 0, width, height);
		}

		for (int i = 0; i < effects.size(); i++) {
			int y = padding + i * lineHeight;
			int color = effectColors.get() ? 0xFF000000 | effects.get(i).getEffect().value().getColor() : Theme.accent();
			Render2D.circle(graphics, padding + 2.5f, y + VText.HEIGHT / 2f, 2.5f, color);
			VText.draw(graphics, names.get(i), padding + 8, y, Theme.TEXT, true);
			VText.draw(graphics, times.get(i), width - padding - VText.width(times.get(i)), y, Theme.TEXT_DIM);
		}
	}

	private static String formatTicks(int ticks) {
		int seconds = ticks / 20;
		return String.format("%d:%02d", seconds / 60, seconds % 60);
	}
}
