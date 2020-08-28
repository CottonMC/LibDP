package io.github.cottonmc.libdp.api.driver.recipe;

import io.github.cottonmc.libdp.api.Diskette;
import io.github.cottonmc.libdp.api.util.*;
import io.github.cottonmc.libdp.api.util.crafting.CraftingUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class CustomShapedRecipe extends ShapedRecipe {
	private final Diskette diskette;
	private final Logger logger;

	public CustomShapedRecipe(Diskette diskette, Identifier id, String group, int width, int height, DefaultedList<Ingredient> ingredients, ItemStack output) {
		super(id, group, width, height, ingredients, output);
		this.diskette = diskette;
		this.logger = LogManager.getLogger(diskette.getId().toString());
	}

	@Override
	public boolean matches(CraftingInventory inv, World world) {
		boolean matches = super.matches(inv, world);
		if (!matches) return false;
		try {
			PlayerEntity player = CraftingUtils.findPlayer(inv);
			Object result = diskette.invokeFunction("matches", CraftingUtils.getInvStacks2d(inv), inv.getWidth(), inv.getHeight(), player != null? new WrappedPlayer(player) : DummyPlayer.INSTANCE, new WorldInfo(world));
			if (result instanceof Boolean) return (Boolean) result;
			else {
				logger.error("Could not check match for custom shaped recipe {}, returning standard match: function 'matches' must return a boolean, but returned {} instead", getId(), result.getClass().getName());
				return true;
			}
		} catch (Exception e) {
			logger.error("Could not check match for custom shaped recipe {}, returning standard match: {}", getId(), e.getMessage());
		}
		return super.matches(inv, world);
	}

	@Override
	public ItemStack craft(CraftingInventory inv) {
		ItemStack stack = super.craft(inv);
		try {
			MutableStack mutableStack = new MutableStack(stack);
			PlayerEntity player = CraftingUtils.findPlayer(inv);
			Object result = diskette.invokeFunction("preview", CraftingUtils.getInvStacks2d(inv), inv.getWidth(), inv.getHeight(), player != null? new WrappedPlayer(player) : DummyPlayer.INSTANCE, mutableStack );
			return result == null? mutableStack.get() : RecipeParser.processItemStack(result);
		} catch (Exception e) {
			logger.error("Could not get preview output for custom shaped recipe {}, returning standard output: {}", getId(), e.getMessage());
			return super.craft(inv);
		}
	}

	@Override
	public DefaultedList<ItemStack> getRemainingStacks(CraftingInventory inv) {
		DefaultedList<ItemStack> remainingStacks = super.getRemainingStacks(inv);
		try {
			PlayerEntity player = CraftingUtils.findPlayer(inv);
			diskette.invokeFunction("craft", CraftingUtils.getInvStacks2d(inv), player != null? new WrappedPlayer(player) : DummyPlayer.INSTANCE, new StackInfo(craft(inv)));
		} catch (Exception e) {
			logger.error("Could not fully craft custom shaped recipe {}, ignoring: {}", getId(), e.getMessage());
		}
		return remainingStacks;
	}
}
