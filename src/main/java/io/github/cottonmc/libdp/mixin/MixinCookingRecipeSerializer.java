package io.github.cottonmc.libdp.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CookingRecipeSerializer.class)
public class MixinCookingRecipeSerializer<T extends AbstractCookingRecipe> {
	@Shadow @Final private int cookingTime;

	@Shadow @Final private CookingRecipeSerializer.RecipeFactory<T> recipeFactory;

	@Inject(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/AbstractCookingRecipe;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/JsonHelper;getString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;", ordinal = 0),
			cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void read(Identifier id, JsonObject json, CallbackInfoReturnable<T> info,
					  String group, JsonElement ingElem, Ingredient ingredient) {
		JsonElement elem = json.get("result");
		if (elem instanceof JsonObject) {
			ItemStack stack = ShapedRecipe.getItemStack((JsonObject)elem);
			float experience = JsonHelper.getFloat(json, "experience", 0.0F);
			int cookingtime = JsonHelper.getInt(json, "cookingtime", this.cookingTime);
			info.setReturnValue(this.recipeFactory.create(id, group, ingredient, stack, experience, cookingtime));
		}
	}

}
