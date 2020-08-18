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
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class CustomShapelessRecipe extends ShapelessRecipe {
	private Diskette bridge;
	private Logger logger;

	public CustomShapelessRecipe(Diskette bridge, Identifier id, String group, DefaultedList<Ingredient> ingredients, ItemStack output) {
		super(id, group, output, ingredients);
		this.bridge = bridge;
		this.logger = LogManager.getLogger(bridge.getId().toString());
	}

	@Override
	public boolean matches(CraftingInventory inv, World world) {
		boolean matches = super.matches(inv, world);
		if (!matches) return false;
		try {
			PlayerEntity player = CraftingUtils.findPlayer(inv);
			Object result = bridge.invokeFunction("matches", CraftingUtils.getInvStacks(inv), inv.getWidth(), inv.getHeight(), player != null? new WrappedPlayer(player) : DummyPlayer.INSTANCE, new WorldInfo(world));
			if (result instanceof Boolean) return (Boolean) result;
			else {
				logger.error("Could not check match for custom shapeless recipe {}, returning standard match: function 'matches' must return a boolean, but returned {} instead", getId(), result.getClass().getName());
				return true;
			}
		} catch (Exception e) {
			logger.error("Could not check match for custom shapeless recipe {}, returning standard match: {}", getId(), e.getMessage());
		}
		return super.matches(inv, world);
	}

	@Override
	public ItemStack craft(CraftingInventory inv) {
		ItemStack stack = super.craft(inv);
		try {
			MutableStack mutableStack = new MutableStack(stack);
			PlayerEntity player = CraftingUtils.findPlayer(inv);
			Object result = bridge.invokeFunction("preview", CraftingUtils.getInvStacks(inv), inv.getWidth(), inv.getHeight(), player != null? new WrappedPlayer(player) : DummyPlayer.INSTANCE, mutableStack );
			return result == null? mutableStack.get() : RecipeParser.processItemStack(result);
		} catch (Exception e) {
			logger.error("Could not get preview output for custom shapeless recipe %s, returning standard output: %s", getId(), e.getMessage());
			return super.craft(inv);
		}
	}

	@Override
	public DefaultedList<ItemStack> getRemainingStacks(CraftingInventory inv) {
		DefaultedList<ItemStack> remainingStacks = super.getRemainingStacks(inv);
		try {
			PlayerEntity player = CraftingUtils.findPlayer(inv);
			bridge.invokeFunction("craft", CraftingUtils.getInvStacks(inv), player != null? new WrappedPlayer(player) : DummyPlayer.INSTANCE, new StackInfo(craft(inv)));
		} catch (Exception e) {
			logger.error("Could not fully craft custom shapeless recipe %s, ignoring: %s", getId(), e.getMessage());
		}
		return remainingStacks;
	}
}
