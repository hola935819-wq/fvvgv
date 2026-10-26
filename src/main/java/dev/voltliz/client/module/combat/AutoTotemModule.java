package dev.voltliz.client.module.combat;

import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ModeSetting;
import dev.voltliz.client.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

/**
 * Keeps a totem in your offhand the way you would by hand: it opens your inventory, waits, swaps a totem
 * into the offhand (the same click as hovering it and pressing the offhand key), waits, and closes the
 * inventory again. One inventory click per swap, and nothing happens while another screen is open.
 */
public class AutoTotemModule extends Module {
	/** Offhand-swap button for {@link ClickType#SWAP}: inventory index 40 is the offhand. */
	private static final int OFFHAND_BUTTON = 40;
	/** Time to let the server confirm the swap before trying again. */
	private static final long RETRY_MS = 500;

	private final ModeSetting mode = add(new ModeSetting("Mode", "Health", "Health", "Always"));
	private final NumberSetting health = add(new NumberSetting("Health", 10, 1, 20, 1));
	private final NumberSetting openDelay = add(new NumberSetting("Open Delay ms", 150, 0, 1000, 10));
	private final NumberSetting closeDelay = add(new NumberSetting("Close Delay ms", 100, 0, 1000, 10));
	private final BooleanSetting closeInventory = add(new BooleanSetting("Close Inventory", true));
	private final BooleanSetting hotbar = add(new BooleanSetting("Use Hotbar", true));

	private State state = State.IDLE;
	private long stateSince;
	private boolean openedByUs;

	public AutoTotemModule() {
		super("AutoTotem", "Opens your inventory and moves a totem to your offhand.", Category.COMBAT);
	}

	@Override
	protected void onDisable() {
		state = State.IDLE;
	}

	@Override
	public void onTick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null || client.gameMode == null || player.isSpectator() || player.isCreative()) {
			state = State.IDLE;
			return;
		}
		long now = System.currentTimeMillis();

		switch (state) {
			case IDLE -> {
				if (!needsTotem(player) || totemSlot(player.getInventory()) < 0) {
					return;
				}
				if (client.screen instanceof InventoryScreen) {
					openedByUs = false;
				} else if (client.screen == null) {
					client.setScreen(new InventoryScreen(player));
					openedByUs = true;
				} else {
					// Another screen (chest, chat, menu) is open: never close it on the player.
					return;
				}
				enter(State.OPENED, now);
			}
			case OPENED -> {
				if (!(client.screen instanceof InventoryScreen)) {
					state = State.IDLE;
					return;
				}
				if (now - stateSince < openDelay.getInt()) {
					return;
				}
				int slot = totemSlot(player.getInventory());
				if (slot >= 0 && !player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
					client.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, menuSlot(slot), OFFHAND_BUTTON, ClickType.SWAP, player);
				}
				enter(State.SWAPPED, now);
			}
			case SWAPPED -> {
				if (now - stateSince < closeDelay.getInt()) {
					return;
				}
				if (openedByUs && closeInventory.get() && client.screen instanceof InventoryScreen screen) {
					screen.onClose();
				}
				enter(State.COOLDOWN, now);
			}
			case COOLDOWN -> {
				if (now - stateSince >= RETRY_MS) {
					state = State.IDLE;
				}
			}
		}
	}

	private void enter(State next, long now) {
		state = next;
		stateSince = now;
	}

	private boolean needsTotem(LocalPlayer player) {
		if (player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
			return false;
		}
		return mode.is("Always") || player.getHealth() + player.getAbsorptionAmount() <= health.get();
	}

	/** Prefers the main inventory so your hotbar stays as it is. */
	private int totemSlot(Inventory inventory) {
		for (int slot = Inventory.SELECTION_SIZE; slot < Inventory.INVENTORY_SIZE; slot++) {
			if (inventory.getItem(slot).is(Items.TOTEM_OF_UNDYING)) {
				return slot;
			}
		}
		if (hotbar.get()) {
			for (int slot = 0; slot < Inventory.SELECTION_SIZE; slot++) {
				if (inventory.getItem(slot).is(Items.TOTEM_OF_UNDYING)) {
					return slot;
				}
			}
		}
		return -1;
	}

	/** Player inventory menu layout: main inventory keeps its index (9-35), the hotbar sits at 36-44. */
	private static int menuSlot(int inventorySlot) {
		return inventorySlot < Inventory.SELECTION_SIZE ? inventorySlot + 36 : inventorySlot;
	}

	private enum State {
		IDLE, OPENED, SWAPPED, COOLDOWN
	}
}
