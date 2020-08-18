package io.github.cottonmc.libdp.api.util.crafting;

import io.github.cottonmc.libdp.api.util.StackInfo;
import io.github.cottonmc.libdp.impl.CraftingInventoryAccessor;
import io.github.cottonmc.libdp.impl.CraftingResultSlotAccessor;
import io.github.cottonmc.libdp.impl.PlayerScreenHandlerAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;

import javax.annotation.Nullable;

public class CraftingUtils {
	@Nullable
	public static PlayerEntity findPlayer(CraftingInventory inventory) {
		try {
			ScreenHandler container = ((CraftingInventoryAccessor) inventory).libdp$getHandler();
			if (container instanceof PlayerScreenHandler) {
				return ((PlayerScreenHandlerAccessor) container).libdp$getOwner();
			} else if (container instanceof CraftingScreenHandler) {
				return ((CraftingResultSlotAccessor) container.getSlot(0)).libdp$getPlayer();
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not access player due to mixin failures", e);
		}
	}

	public static StackInfo[] getInvStacks(CraftingInventory inv) {
		StackInfo[] stacks = new StackInfo[inv.size()];
		for (int i = 0; i < inv.size(); i++) {
			stacks[i] = new StackInfo(inv.getStack(i));
		}
		return stacks;
	}
}
