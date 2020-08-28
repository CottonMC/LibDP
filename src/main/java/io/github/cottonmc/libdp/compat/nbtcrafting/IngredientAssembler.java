package io.github.cottonmc.libdp.compat.nbtcrafting;

//import de.siphalor.nbtcrafting.ingredient.IIngredient;
//import de.siphalor.nbtcrafting.ingredient.IngredientStackEntry;
//import de.siphalor.nbtcrafting.util.duck.ICloneable;
import io.github.cottonmc.libdp.api.driver.recipe.RecipeDriver;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class IngredientAssembler {
//	public static Ingredient constructFromStacks(ItemStack... stacks) {
//		List<IngredientStackEntry> entries = new ArrayList<>();
//		for (ItemStack stack : stacks) {
//			entries.add(new IngredientStackEntry(stack));
//		}
//		Stream<IngredientStackEntry> entryStream = entries.stream();
//		try {
//			Ingredient ingredient = (Ingredient)((ICloneable)(Object)Ingredient.EMPTY).clone();
//			((IIngredient)(Object)ingredient).setAdvancedEntries(entryStream);
//			return ingredient;
//		} catch (CloneNotSupportedException e) {
//			RecipeDriver.INSTANCE.getLogger().error("Failed to assemble ingredient with NBT Crafting: " + e.getMessage());
//			return Ingredient.EMPTY;
//		}
//	}
}
