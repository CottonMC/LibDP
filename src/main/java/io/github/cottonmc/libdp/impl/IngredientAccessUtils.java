package io.github.cottonmc.libdp.impl;

import io.github.cottonmc.libdp.api.util.NbtMatchType;
import net.minecraft.item.ItemStack;

public interface IngredientAccessUtils {
	void libdp$setMatchType(NbtMatchType type);
	ItemStack[] libdp$getStackArray();
}
