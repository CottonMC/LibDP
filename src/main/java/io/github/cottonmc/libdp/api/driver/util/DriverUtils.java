package io.github.cottonmc.libdp.api.driver.util;

import io.github.cottonmc.libcd.api.tag.TagHelper;
import io.github.cottonmc.libdp.api.driver.DriverManager;
import io.github.cottonmc.libdp.api.driver.StackFactory;
import io.github.cottonmc.libdp.api.driver.recipe.RecipeParser;
import io.github.cottonmc.libdp.api.util.MutableStack;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

/**
 * Various utilities for writing diskettes, due to the obfuscation of minecraft code.
 */
public class DriverUtils {
	public static final DriverUtils INSTANCE = new DriverUtils();

	private DriverUtils() {}

	public MutableStack newStack(String item) {
		return newStack(item, 1);
	}

	public MutableStack newStack(String item, int count) {
		return new MutableStack(new ItemStack(getItem(item), count));
	}

	/**
	 * Get a registered item inside a script.
	 * @param id The id to search for.
	 * @return The registered item, or Items.AIR if it doesn't exist.
	 */
	public Item getItem(String id) {
		return Registry.ITEM.get(new Identifier(id));
	}

	/**
	 * Get the default item from an item tag.
	 * @param id The ID of the tag to get from.
	 * @return The default item for that tag.
	 */
	public Item getDefaultItem(String id) {
		Identifier tagId = new Identifier(id);
		Tag<Item> tag = ServerTagManagerHolder.getTagManager().getItems().getTag(tagId);
		if (tag == null) return Items.AIR;
		return TagHelper.ITEM.getDefaultEntry(tag);
	}

	/**
	 * Get the item from an item stack.
	 * @param stack The stack to check.
	 * @return The item of the stack.
	 */
	public Item getStackItem(ItemStack stack) {
		return stack.getItem();
	}

	/**
	 * Get a registered block inside a script.
	 * @param id The id to search for.
	 * @return The registered item, or Blocks.AIR if it doesn't exist.
	 */
	public Block getBlock(String id) {
		return Registry.BLOCK.get(new Identifier(id));
	}

	/**
	 * Get a registered fluid inside a script.
	 * @param id The id to search for.
	 * @return The registered fluid, or Fluids.EMPTY if it doesn't exist.
	 */
	public Fluid getFluid(String id) {
		return Registry.FLUID.get(new Identifier(id));
	}

	/**
	 * Get a registered entity type inside a script.
	 * @param id The id to search for.
	 * @return The registered entity, or EntityType.PIG if it doesn't exist.
	 */
	public EntityType<?> getEntity(String id) {
		return Registry.ENTITY_TYPE.get(new Identifier(id));
	}

	/**
	 * Get a registered sound inside a script.
	 * @param id The id to search for.
	 * @return The registered sound, or SoundEvents.ENTITY_ITEM_PICKUP if it doesn't exist.
	 */
	public SoundEvent getSound(String id) {
		return Registry.SOUND_EVENT.get(new Identifier(id));
	}

	/**
	 * Check if a DefaultedList (like the ones inventories use) is empty.
	 * Necessary because DefaultedList stays within Collection<E> spec for once.
	 * @param items The DefaultedList to check.
	 * @return Whether all the item stacks in the list are empty or not.
	 */
	public boolean isItemListEmpty(DefaultedList<ItemStack> items) {
		for (ItemStack stack : items) {
			if (!stack.isEmpty()) return false;
		}
		return true;
	}

	/**
	 * Get a specal stack like a potion from its formatted getter string.
	 * @param factory The formatted getter string ([getter:id]->[entry:id]) to use.
	 * @return the gotten stack, or an empty stack if the getter or id doesn't exist
	 */
	public ItemStack makeSpecialStack(String factory) {
		String[] split = RecipeParser.processStackFactory(factory);
		return makeSpecialStack(split[0], split[1]);
	}

	/**
	 * Get a special stack like a potion from its getter and ID.
	 * @param factory The id of the StackFactory to use.
	 * @param entry The id of the entry to get from the StackFactory.
	 * @return The gotten stack, or an empty stack if the getter or id doesn't exist.
	 */
	public ItemStack makeSpecialStack(String factory, String entry) {
		Identifier factoryId = new Identifier(factory);
		Identifier itemId = new Identifier(entry);
		if (!DriverManager.INSTANCE.getStackFactories().containsKey(factoryId)) return ItemStack.EMPTY;
		StackFactory get = DriverManager.INSTANCE.getStackFactories().get(factoryId);
		return get.getSpecialStack(itemId);
	}

	/**
	 * Get an array of string ids for items in a given tag.
	 * @param tagId The id of the tag to get items for.
	 * @return An array of items in the tag.
	 */
	public String[] getItemsInTag(String tagId) {
		Tag<Item> tag = ServerTagManagerHolder.getTagManager().getItems().getTag(new Identifier(tagId));
		if (tag == null) return new String[0];
		Object[] items = tag.values().toArray();
		String[] res = new String[items.length];
		for (int i = 0; i < items.length; i++) {
			res[i] = Registry.ITEM.getId((Item)items[i]).toString();
		}
		return res;
	}
}
