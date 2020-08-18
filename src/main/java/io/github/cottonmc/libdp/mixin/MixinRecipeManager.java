package io.github.cottonmc.libdp.mixin;

import io.github.cottonmc.libdp.impl.RecipeMapAccessor;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(RecipeManager.class)
public class MixinRecipeManager implements RecipeMapAccessor {
	@Shadow
	private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;

	@Override
	public Map<RecipeType<?>, Map<Identifier, Recipe<?>>> libdp$getRecipeMap() {
		return recipes;
	}

	@Override
	public void libdp$setRecipeMap(Map<RecipeType<?>, Map<Identifier, Recipe<?>>> map) {
		recipes = map;
	}
}
