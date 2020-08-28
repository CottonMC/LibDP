package io.github.cottonmc.libdp.api.driver.recipe;

import com.google.gson.JsonObject;
import io.github.cottonmc.libdp.LibDP;
import io.github.cottonmc.libdp.api.Diskette;
import io.github.cottonmc.libdp.api.util.DummyPlayer;
import io.github.cottonmc.libdp.api.util.StackInfo;
import io.github.cottonmc.libdp.api.util.WorldInfo;
import io.github.cottonmc.libdp.api.util.WrappedPlayer;
import io.github.cottonmc.libdp.api.util.crafting.CraftingUtils;
import io.github.cottonmc.libdp.loader.DisketteLoader;
import io.github.cottonmc.libdp.loader.NullDiskette;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class CustomSpecialCraftingRecipe extends SpecialCraftingRecipe {
	private final Diskette diskette;
	private final Logger logger;

	public CustomSpecialCraftingRecipe(Diskette diskette, Identifier id) {
		super(id);
		this.diskette = diskette;
		this.logger = LogManager.getLogger(diskette.getId().toString());
	}

	@Override
	public boolean matches(CraftingInventory inv, World world) {
		try {
			PlayerEntity player = CraftingUtils.findPlayer(inv);
			Object result = diskette.invokeFunction("matches", CraftingUtils.getInvStacks2d(inv), inv.getWidth(), inv.getHeight(), player != null? new WrappedPlayer(player) : DummyPlayer.INSTANCE, new WorldInfo(world));
			if (result instanceof Boolean) return (Boolean) result;
			else {
				logger.error("Could not check match for custom special crafting recipe {}, returning false: function 'matches' must return a boolean, but returned {} instead", getId(), result.getClass().getName());
				return false;
			}
		} catch (Exception e) {
			logger.error("Could not check match for custom special crafting recipe {}, returning false: {}", getId(), e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public ItemStack craft(CraftingInventory inv) {
		try {
			PlayerEntity player = CraftingUtils.findPlayer(inv);
			Object result = diskette.invokeFunction("preview", CraftingUtils.getInvStacks2d(inv), inv.getWidth(), inv.getHeight(), player != null? new WrappedPlayer(player) : DummyPlayer.INSTANCE);
			if (result == null) {
				logger.error("Could not get preview output for custom special crafting recipe {}, returning empty stack: function 'preview' must not return null", getId());
				return ItemStack.EMPTY;
			} else {
				return RecipeParser.processItemStack(result);
			}
		} catch (Exception e) {
			logger.error("Could not get preview output for custom special crafting recipe {}, returning empty stack: {}", getId(), e.getMessage());
			return ItemStack.EMPTY;
		}
	}

	@Override
	public boolean fits(int width, int height) {
		return true; //this doesn't matter, since it's a special crafting recipe
	}

	//this *should* only be called on server, from what I can tell
	@Override
	public DefaultedList<ItemStack> getRemainingStacks(CraftingInventory inv) {
		DefaultedList<ItemStack> remainingStacks = super.getRemainingStacks(inv);
		try {
			PlayerEntity player = CraftingUtils.findPlayer(inv);
			diskette.invokeFunction("craft", CraftingUtils.getInvStacks2d(inv), player != null? new WrappedPlayer(player) : DummyPlayer.INSTANCE, new StackInfo(craft(inv)));
		} catch (Exception e) {
			logger.error("Could not fully craft custom special crafting recipe {}, ignoring: {}", getId(), e.getMessage());
		}
		return remainingStacks;
	}

	@Override
	public RecipeSerializer<CustomSpecialCraftingRecipe> getSerializer() {
		return LibDP.CUSTOM_SPECIAL_SERIALIZER;
	}

	public static class Serializer implements RecipeSerializer<CustomSpecialCraftingRecipe> {

		@Override
		public CustomSpecialCraftingRecipe read(Identifier id, JsonObject json) {
			Diskette diskette = DisketteLoader.DISKETTES.get(new Identifier(JsonHelper.getString(json, "diskette")));
			return new CustomSpecialCraftingRecipe(diskette, id);
		}

		@Override
		public CustomSpecialCraftingRecipe read(Identifier id, PacketByteBuf buf) {
			return new CustomSpecialCraftingRecipe(NullDiskette.INSTANCE, id);
		}

		@Override
		public void write(PacketByteBuf buf, CustomSpecialCraftingRecipe recipe) { }
	}
}
