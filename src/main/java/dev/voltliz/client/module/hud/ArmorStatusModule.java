package dev.voltliz.client.module.hud;

import dev.voltliz.client.module.HudModule;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ModeSetting;
import dev.voltliz.client.util.ColorUtil;
import dev.voltliz.client.util.RenderUtil;
import dev.voltliz.client.util.Theme;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
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
		Font font = mc().font;
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
				graphics.drawString(font, "Armor Status", 2, 4, Theme.TEXT_DIM, true);
			}
			return;
		}

		int textWidth = 0;
		for (ItemStack stack : items) {
			textWidth = Math.max(textWidth, font.width(durabilityText(stack)));
		}
		width = 18 + (textWidth > 0 ? textWidth + 2 : 0);
		height = items.size() * 17 - 1;

		if (background.get()) {
			RenderUtil.hudBackground(graphics, -2, -2, width + 4, height + 4);
		}

		int y = 0;
		for (ItemStack stack : items) {
			graphics.renderItem(stack, 0, y);
			graphics.renderItemDecorations(font, stack, 0, y);
			String text = durabilityText(stack);
			if (!text.isEmpty()) {
				graphics.drawString(font, text, 19, y + 4, durabilityColor(stack), true);
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
