package io.github.cottonmc.libdp.api.util;

import io.github.cottonmc.libdp.api.util.nbt.NbtUtils;
import io.github.cottonmc.libdp.api.util.nbt.WrappedCompoundTag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * A wrapped version of an item stack that can be modified from scripts.
 * Has all the methods of {@link StackInfo}.
 */
public class MutableStack extends StackInfo {

	public MutableStack(ItemStack stack) {
		super(stack);
		this.stack = stack;
	}

	/**
	 * Set the count of the item stack.
	 * @param count The count of items in the stack. Will be limited to the max stack size for this item.
	 * @return The stack with the count set.
	 */
	public MutableStack setCount(int count) {
		stack.setCount(count);
		return this;
	}

	/**
	 * Set the custom name of the item stack.
	 * @param name The name for this stack as a raw string.
	 * @return The stack with the name set.
	 */
	public MutableStack setName(String name) {
		stack.setCustomName(new LiteralText(name));
		return this;
	}

	/**
	 * Set the custom name of the item stack using JSON formatting.
	 * @param name The name for this stack as JSON text.
	 * @return The stack with the name set.
	 */
	public MutableStack setFormattedName(String name) {
		stack.setCustomName(Text.Serializer.fromJson(name));
		return this;
	}

	/**
	 * Clear the stack custom name.
	 * @return The stack with the name removed.
	 */
	public MutableStack clearName() {
		stack.removeCustomName();
		return this;
	}

	/**
	 * Set the lore of the item stack.
	 * @param lore The array of lines to add as raw strings.
	 * @return The stack with the lore set.
	 */
	public MutableStack setLore(String[] lore) {
		CompoundTag display = stack.getOrCreateSubTag("display");
		ListTag list = display.getList("Lore", 8);
		for (int i = 0; i < lore.length; i++) {
			String line = lore[i];
			list.addTag(i, StringTag.of("{\"text\":\"" + line + "\"}"));
		}
		display.put("Lore", list);
		stack.putSubTag("display", display);
		return this;
	}

	/**
	 * Set the lore of the item stack, parsed from stringified JSON.
	 * @param lore The array of lines to add as JSON.
	 * @return The stack with the lore set.
	 */
	public MutableStack setFormattedLore(String[] lore) {
		CompoundTag display = stack.getOrCreateSubTag("display");
		ListTag list = display.getList("Lore", 8);
		for (int i = 0; i < lore.length; i++) {
			list.addTag(i, StringTag.of(lore[i]));
		}
		display.put("Lore", list);
		stack.putSubTag("display", display);
		return this;
	}

	/**
	 * Set the damage of the item stack.
	 * @param damage The amount of damage the item has taken.
	 * @return The stack with the damage set.
	 */
	public MutableStack setDamage(int damage) {
		stack.setDamage(damage);
		return this;
	}

	/**
	 * Set the value of a tag in the stack's main NBT tag.
	 * @param key The name of the tag to set.
	 * @param value The value to set it to.
	 * @return The stack with the tag set.
	 */
	public MutableStack setTagValue(String key, Object value) {
		stack.getOrCreateTag().put(key, NbtUtils.getTagFor(value));
		return this;
	}

	/**
	 * Set the entire stack NBT tag.
	 * @param tag The tag to set it to.
	 * @return The stack with the tag set.
	 */
	public MutableStack setTag(WrappedCompoundTag tag) {
		stack.setTag(tag.getUnderlying());
		return this;
	}

	/**
	 * Enchant the stack.
	 * @param enchantmentName The name of the enchantment to use.
	 * @param level The level to enchant with.
	 * @return The newly-enchanted stack.
	 */
	public MutableStack enchant(String enchantmentName, int level) {
		stack.addEnchantment(Registry.ENCHANTMENT.get(new Identifier(enchantmentName)), level);
		return this;
	}

	public ItemStack get() {
		return stack;
	}
}
