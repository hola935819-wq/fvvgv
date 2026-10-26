package dev.voltliz.client.module.hud;

import dev.voltliz.client.module.HudModule;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ModeSetting;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.render.Render2D;
import dev.voltliz.client.render.VText;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ArmorStatusModule extends HudModule {
	private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	private final ModeSetting durability = add(new ModeSetting("Durability", "Value", "Value", "Percent", "Off"));
	private final BooleanSetting showHeldItem = add(new BooleanSetting("Held Item", true));
	private final BooleanSetting background = add(new BooleanSetting("Background", false));

	public ArmorStatusModule() {
		super("Armor Status", "Shows your armor and its durability.", 10_000, 10_000);
		setEnabled(true);
	}

	@Override
	protected void renderContent(GuiGraphics graphics) {
		LocalPlayer player = mc().player;
		List<ItemStack> items = new ArrayList<>();

		if (player != null) {
			for (EquipmentSlot slot : ARMOR) {
				ItemStack stack = player.getItemBySlot(slot);
				if (!stack.isEmpty()) {
					items.add(stack);
				}
			}
			if (showHeldItem.get() && !player.getMainHandItem().isEmpty()) {
				items.add(player.getMainHandItem());
			}
		}

		if (items.isEmpty()) {
			width = 60;
			height = 16;
			if (isEditing()) {
				VText.draw(graphics, "Armor Status", 2, 4, Theme.TEXT_DIM);
			}
			return;
		}

		int textWidth = 0;
		for (ItemStack stack : items) {
			textWidth = Math.max(textWidth, VText.width(durabilityText(stack), true));
		}
		width = 18 + (textWidth > 0 ? textWidth + 2 : 0);
		height = items.size() * 17 - 1;

		if (background.get()) {
			RenderUtil.hudBackground(graphics, -3, -3, width + 6, height + 6);
		}

		int y = 0;
		for (ItemStack stack : items) {
			graphics.renderItem(stack, 0, y);
			if (stack.isBarVisible()) {
				Render2D.rect(graphics, 2, y + 13, 13, 2, 0xFF000000);
				Render2D.rect(graphics, 2, y + 13, stack.getBarWidth(), 1, 0xFF000000 | stack.getBarColor());
			}
			if (stack.getCount() > 1) {
				String count = String.valueOf(stack.getCount());
				VText.draw(graphics, count, 17 - VText.width(count, true), y + 9, Theme.TEXT, true);
			}
			String text = durabilityText(stack);
			if (!text.isEmpty()) {
				VText.draw(graphics, text, 20, y + 4, durabilityColor(stack), true);
			}
			y += 17;
		}
	}

	private String durabilityText(ItemStack stack) {
		if (durability.is("Off") || !stack.isDamageableItem()) {
			return "";
		}
		int remaining = stack.getMaxDamage() - stack.getDamageValue();
		if (durability.is("Percent")) {
			return Math.round(remaining * 100f / stack.getMaxDamage()) + "%";
		}
		return String.valueOf(remaining);
	}

	private int durabilityColor(ItemStack stack) {
		float ratio = (stack.getMaxDamage() - stack.getDamageValue()) / (float) stack.getMaxDamage();
		return ColorUtil.hsb(ratio / 3f, 0.75f, 1f, 255);
	}
}
