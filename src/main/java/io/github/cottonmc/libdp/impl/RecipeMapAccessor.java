package io.github.cottonmc.libdp.impl;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface RecipeMapAccessor {
	Map<RecipeType<?>, Map<Identifier, Recipe<?>>> libdp$getRecipeMap();
	void libdp$setRecipeMap(Map<RecipeType<?>, Map<Identifier, Recipe<?>>> map);
}
