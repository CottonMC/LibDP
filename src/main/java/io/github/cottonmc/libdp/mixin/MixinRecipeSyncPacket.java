package io.github.cottonmc.libdp.mixin;

import io.github.cottonmc.libdp.LibDP;
import io.github.cottonmc.libdp.api.driver.recipe.CustomSpecialCraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.registry.Registry;

@Mixin(SynchronizeRecipesS2CPacket.class)
public class MixinRecipeSyncPacket {

	//I am pleased to inform you that I have gone mad with power
	//TODO: find out if this has any reprecussions
	@Inject(method = "writeRecipe", at = @At("HEAD"), cancellable = true)
	private static <T extends Recipe<?>> void injectFakeIdentity(T recipe, PacketByteBuf buf, CallbackInfo info) {
		if (LibDP.COMPATIBILITY_MODE && recipe instanceof CustomSpecialCraftingRecipe) {
			//you don't need map cloning in a crafting table anymore, right?
			//...right?
			buf.writeIdentifier(Registry.RECIPE_SERIALIZER.getId(RecipeSerializer.MAP_EXTENDING));
			buf.writeIdentifier(recipe.getId());
			//special recipe serializers only write their serializer ID and recipe ID, so safe to not have a write call
			info.cancel();
		}
	}
}
