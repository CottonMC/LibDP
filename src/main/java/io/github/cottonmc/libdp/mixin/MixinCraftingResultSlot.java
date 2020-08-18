package io.github.cottonmc.libdp.mixin;

import io.github.cottonmc.libdp.impl.CraftingResultSlotAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.CraftingResultSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CraftingResultSlot.class)
public class MixinCraftingResultSlot implements CraftingResultSlotAccessor {
	@Shadow @Final private PlayerEntity player;

	@Override
	public PlayerEntity libdp$getPlayer() {
		return player;
	}
}
