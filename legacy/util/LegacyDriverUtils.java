import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cottonmc.libdp.api.CDCommons;
import io.github.cottonmc.libdp.api.tag.TagHelper;
import io.github.cottonmc.libdp.api.tweaker.DriverManager;
import io.github.cottonmc.libdp.api.tweaker.DriverStackFactory;
import io.github.cottonmc.libdp.api.tweaker.recipe.RecipeParser;
import io.github.cottonmc.libdp.api.util.StackInfo;
import io.github.cottonmc.libdp.api.util.nbt.WrappedCompoundTag;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.nbt.StringTag;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

/**
 * Various utilities for writing tweakers, due to the obfuscation of minecraft code.
 */
public class DriverUtils {
	public static final DriverUtils INSTANCE = new DriverUtils();

	private DriverUtils() {}

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
	public EntityType getEntity(String id) {
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
	 * Create an item stack from an item id.
	 * @param id The id of the item to get, along with any NBT.
	 * @return An item stack of the specified item, with an amount of 1.
	 */
	public ItemStack createItemStack(String id) {
		return createItemStack(id, 1);
	}

	/**
	 * Create an item stack from an item id.
	 * @param id The id of the item to get, along with any NBT.
	 * @param amount The amount of the item in the stack.
	 * @return An item stack of the specified item and amount.
	 */
	public ItemStack createItemStack(String id, int amount) {
		int index = id.indexOf('{');
		if (index == -1) {
			return new ItemStack(getItem(id), amount);
		} else {
			Item item = getItem(id.substring(0, index));
			ItemStack stack = new ItemStack(item, amount);
			return addNbtToStack(stack, id.substring(index));
		}
	}

	/**
	 * Create an item stack from an item.
	 * @param item The item to have a stack of.
	 * @return An item stack of the specified item, with an amount of 1.
	 */
	public ItemStack createItemStack(Item item) {
		return createItemStack(item, 1);
	}

	/**
	 * Create an item stack from an item.
	 * @param item The item to have a stack of.
	 * @param amount The amount of the item in the stack.
	 * @return An item stack of the specified item and amount.
	 */
	public ItemStack createItemStack(Item item, int amount) {
		return new ItemStack(item, amount);
	}

	/**
	 * Add NBT to an item stack.
	 * @param stack The stack to add NBT to.
	 * @param nbt The string version of NBT to add.
	 * @return The stack with added NBT.
	 */
	public ItemStack addNbtToStack(ItemStack stack, String nbt) {
		StringNbtReader reader = new StringNbtReader(new StringReader(nbt));
		try {
			CompoundTag tag = reader.parseCompoundTag();
			stack.setTag(tag);
		} catch (CommandSyntaxException e) {
			LibDP.LOGGER.error("Error adding NBT to stack: " + e.getMessage());
		}
		return stack;
	}

	/**
	 * Add NBT to an item stack.
	 * @param stack The stack to add NBT to.
	 * @param tag The wrapped compound tag to add.
	 * @return The stack with added NBT.
	 */
	public ItemStack addNbtToStack(ItemStack stack, WrappedCompoundTag tag) {
		stack.setTag(tag.getUnderlying());
		return stack;
	}

	/**
	 * @param stack The stack to get info for.
	 * @return A wrapper class with read-only info about the tag.
	 */
	public StackInfo getStackInfo(ItemStack stack) {
		return new StackInfo(stack);
	}

	/**
	 * @param stack The stack to get the tag for.
	 * @return A wrapper around the mutable tag for the stack.
	 */
	public WrappedCompoundTag getStackTag(ItemStack stack) {
		return new WrappedCompoundTag(stack.getOrCreateTag());
	}

	/**
	 * Add an enchantment to an ItemStack. Will ignore whether the enchantment fits on the stack.
	 * @param stack The stack to enchant.
	 * @param enchantment The ID of the enchantment to add.
	 * @param level The level of the enchantment to add.
	 * @return The stack with the new enchantment.
	 */
	public ItemStack enchant(ItemStack stack, String enchantment, int level) {
		Enchantment ench = Registry.ENCHANTMENT.get(new Identifier(enchantment));
		stack.addEnchantment(ench, level);
		return stack;
	}

	/**
	 * Add lore messages to an ItemStack.
	 * @param stack The stack to add lroe to.
	 * @param lore The lines to add to lore. Use § to change the color of the messages.
	 * @return The stack with the new lore.
	 */
	public ItemStack addLore(ItemStack stack, String[] lore) {
		CompoundTag display = stack.getOrCreateSubTag("display");
		ListTag list = display.getList("Lore", 8);
		for (int i = 0; i < lore.length; i++) {
			String line = lore[i];
			list.addTag(i, StringTag.of("{\"text\":\"" + line + "\"}"));
		}
		display.put("Lore", list);
		stack.putSubTag("display", display);
		return stack;
	}

	/**
	 * Set the damage on an ItemStack. Counts up from 0 to the item's max damage.
	 * @param stack The stack to set damage on.
	 * @param amount How much damage to apply, or -1 to make unbreakable.
	 * @return The stack with the new damage.
	 */
	public ItemStack setDamage(ItemStack stack, int amount) {
		if (amount == -1) stack.getOrCreateTag().putBoolean("Unbreakable", true);
		else stack.setDamage(amount);
		return stack;
	}

	/**
	 * Set the custom name on an ItemStack.
	 * @param stack The stack to set the name on.
	 * @param name The name to set to. Use § to change the color of the name.
	 * @return The stack with the new name.
	 */
	public ItemStack setName(ItemStack stack, String name) {
		stack.setCustomName(new LiteralText(name));
		return stack;
	}

	/**
	 * Get a specal stack like a potion from its formatted getter string.
	 * @param getter The formatted getter string ([getter:id]->[entry:id]) to use.
	 * @return the gotten stack, or an empty stack if the getter or id doesn't exist
	 */
	public ItemStack getSpecialStack(String getter) {
		String[] split = RecipeParser.processStackFactory(getter);
		return getSpecialStack(split[0], split[1]);
	}

	/**
	 * Get a special stack like a potion from its getter and ID.
	 * @param getter The id of the StackFactory to use.
	 * @param entry The id of the entry to get from the StackFactory.
	 * @return The gotten stack, or an empty stack if the getter or id doesn't exist.
	 */
	public ItemStack getSpecialStack(String getter, String entry) {
		Identifier getterId = new Identifier(getter);
		Identifier itemId = new Identifier(entry);
		if (!DriverManager.INSTANCE.getStackFactories().containsKey(getterId)) return ItemStack.EMPTY;
		DriverStackFactory get = DriverManager.INSTANCE.getStackFactories().get(getterId);
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