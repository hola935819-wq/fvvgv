package dev.voltliz.client.module.combat;

import dev.voltliz.client.VoltlizClient;
import dev.voltliz.client.module.Category;
import dev.voltliz.client.module.Module;
import dev.voltliz.client.setting.BooleanSetting;
import dev.voltliz.client.setting.ModeSetting;
import dev.voltliz.client.setting.NumberSetting;
import dev.voltliz.client.util.CpsTracker;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Crystal PvP helper driven by the crosshair: while you hold left click, it right clicks to place an
 * end crystal on the obsidian (or bedrock) you are looking at, then left clicks the crystal to break it.
 * Every place and break is a simulated click on the use or attack key, so it goes through vanilla's own
 * input handling like a real click. At most one click per tick, and it never clicks a spot or crystal
 * again until the server has had time to answer, so it does not send repeated packets for nothing.
 */
public class AutoCrystalModule extends Module {
	/** Ticks to wait for the server before clicking the same spot or crystal again. */
	private static final int RETRY_TICKS = 10;

	private final ModeSetting activation = add(new ModeSetting("Activation", "Hold Click", "Hold Click", "Always"));
	private final BooleanSetting place = add(new BooleanSetting("Place", true));
	private final NumberSetting placeDelay = add(new NumberSetting("Place Delay", 0, 0, 10, 1));
	private final BooleanSetting breakCrystals = add(new BooleanSetting("Break", true));
	private final NumberSetting breakDelay = add(new NumberSetting("Break Delay", 0, 0, 10, 1));
	private final BooleanSetting autoSwitch = add(new BooleanSetting("Auto Switch", true));
	private final BooleanSetting switchBack = add(new BooleanSetting("Switch Back", true));
	private final BooleanSetting bedrock = add(new BooleanSetting("Bedrock", true));
	private final NumberSetting minHealth = add(new NumberSetting("Min Health", 0, 0, 20, 1));

	private long ticks;
	private long lastPlace = Long.MIN_VALUE / 2;
	private long lastBreak = Long.MIN_VALUE / 2;
	private BlockPos lastPlacePos;
	private int lastBrokenId = -1;
	private int switchedFrom = -1;
	private boolean blockMining;

	public AutoCrystalModule() {
		super("AutoCrystal", "Places and breaks end crystals on the obsidian you look at.", Category.COMBAT);
	}

	/** True while the crosshair is on a crystal spot, so holding left click doesn't start mining it. */
	public static boolean shouldBlockMining() {
		VoltlizClient voltliz = VoltlizClient.get();
		if (voltliz == null) {
			return false;
		}
		AutoCrystalModule module = voltliz.getModuleManager().get(AutoCrystalModule.class);
		return module != null && module.isEnabled() && module.blockMining;
	}

	@Override
	protected void onDisable() {
		stop(mc().player);
	}

	@Override
	public void onPreTick(Minecraft client) {
		ticks++;
		LocalPlayer player = client.player;
		ClientLevel level = client.level;
		blockMining = false;
		if (player == null || level == null || client.gameMode == null || client.screen != null || player.isSpectator()) {
			stop(player);
			return;
		}
		if (activation.is("Hold Click") && !client.options.keyAttack.isDown()) {
			stop(player);
			return;
		}

		HitResult hit = client.hitResult;
		BlockPos base = hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK ? blockHit.getBlockPos() : null;
		boolean onBase = base != null && isBase(level.getBlockState(base));
		blockMining = onBase && findCrystals(player) != Hand.NONE;

		if (minHealth.get() > 0 && player.getHealth() + player.getAbsorptionAmount() <= minHealth.get()) {
			return;
		}

		if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof EndCrystal crystal) {
			tryBreak(client, crystal);
		} else if (onBase) {
			tryPlace(client, player, level, base);
		}
	}

	private void tryBreak(Minecraft client, EndCrystal crystal) {
		if (!breakCrystals.get() || ticks - lastBreak <= breakDelay.getInt()) {
			return;
		}
		// The crystal only disappears once the server says so; don't hit it again while we wait.
		if (crystal.getId() == lastBrokenId && ticks - lastBreak < RETRY_TICKS) {
			return;
		}
		lastBreak = ticks;
		lastBrokenId = crystal.getId();
		KeyMapping.click(KeyBindingHelper.getBoundKeyOf(client.options.keyAttack));
		CpsTracker.onClick(0);
	}

	private void tryPlace(Minecraft client, LocalPlayer player, ClientLevel level, BlockPos base) {
		if (!place.get() || ticks - lastPlace <= placeDelay.getInt() || !canPlaceOn(level, base)) {
			return;
		}
		// Same as breaking: wait for the placed crystal to show up before clicking this spot again.
		if (base.equals(lastPlacePos) && ticks - lastPlace < RETRY_TICKS) {
			return;
		}
		if (!holdCrystals(player)) {
			return;
		}
		lastPlace = ticks;
		lastPlacePos = base.immutable();
		KeyMapping.click(KeyBindingHelper.getBoundKeyOf(client.options.keyUse));
		CpsTracker.onClick(1);
	}

	private boolean isBase(BlockState state) {
		return state.is(Blocks.OBSIDIAN) || bedrock.get() && state.is(Blocks.BEDROCK);
	}

	/** Vanilla's own rule: air above the block and no entity in the 1x2 space the crystal takes. */
	private static boolean canPlaceOn(ClientLevel level, BlockPos base) {
		BlockPos above = base.above();
		if (!level.isEmptyBlock(above)) {
			return false;
		}
		AABB space = new AABB(above.getX(), above.getY(), above.getZ(), above.getX() + 1, above.getY() + 2, above.getZ() + 1);
		return level.getEntities((Entity) null, space).isEmpty();
	}

	/** Gets crystals into a hand vanilla will use on the block, switching hotbar slot like pressing its number key. */
	private boolean holdCrystals(LocalPlayer player) {
		Hand hand = findCrystals(player);
		if (hand == Hand.MAIN || hand == Hand.OFF) {
			return true;
		}
		if (hand != Hand.HOTBAR || !autoSwitch.get()) {
			return false;
		}
		Inventory inventory = player.getInventory();
		int slot = crystalSlot(inventory);
		if (switchedFrom < 0) {
			switchedFrom = inventory.getSelectedSlot();
		}
		inventory.setSelectedSlot(slot);
		return true;
	}

	private Hand findCrystals(LocalPlayer player) {
		if (player.getMainHandItem().is(Items.END_CRYSTAL)) {
			return Hand.MAIN;
		}
		// Vanilla tries the main hand first, so a block in it would be placed instead of the crystal.
		if (player.getOffhandItem().is(Items.END_CRYSTAL) && !(player.getMainHandItem().getItem() instanceof BlockItem)) {
			return Hand.OFF;
		}
		return autoSwitch.get() && crystalSlot(player.getInventory()) >= 0 ? Hand.HOTBAR : Hand.NONE;
	}

	private static int crystalSlot(Inventory inventory) {
		for (int slot = 0; slot < Inventory.SELECTION_SIZE; slot++) {
			if (inventory.getItem(slot).is(Items.END_CRYSTAL)) {
				return slot;
			}
		}
		return -1;
	}

	private void stop(LocalPlayer player) {
		blockMining = false;
		if (switchedFrom >= 0 && player != null && switchBack.get()) {
			player.getInventory().setSelectedSlot(switchedFrom);
		}
		switchedFrom = -1;
	}

	private enum Hand {
		MAIN, OFF, HOTBAR, NONE
	}
}
