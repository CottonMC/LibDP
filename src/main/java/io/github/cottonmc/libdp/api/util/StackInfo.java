package io.github.cottonmc.libdp.api.util;

import io.github.cottonmc.libdp.api.util.nbt.NbtUtils;
import io.github.cottonmc.libdp.api.util.nbt.WrappedCompoundTag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;
import java.util.Optional;

/**
 * A class with read-only info about a stack, accessible outside of obf. The stack and its NBT cannot be modified from a StackInfo.
 */
public class StackInfo {
	protected ItemStack stack;
	public StackInfo(ItemStack stack) {
		this.stack = stack.copy();
	}

	/**
	 * @return Whether the stack is empty.
	 */
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	/**
	 * @return The ID of the stack's item.
	 */
	public String getItem() {
		return Registry.ITEM.getId(stack.getItem()).toString();
	}

	/**
	 * @return The count of items in the stack.
	 */
	public int getCount() {
		return stack.getCount();
	}

	public int getMaxCount() {
		return stack.getMaxCount();
	}

	public boolean isStackable() {
		return stack.isStackable();
	}

	/**
	 * @return The stack's name.
	 */
	public String getName() {
		return stack.getName().asString();
	}

	/**
	 * @return How much damage the item has taken.
	 */
	public int getDamage() {
		return stack.getDamage();
	}

	public int getMaxDamage() {
		return stack.getMaxDamage();
	}

	public boolean isDamageable() {
		return stack.isDamageable();
	}

	public boolean isDamaged() {
		return stack.isDamaged();
	}

	/**
	 * @param enchantId The enchantment to check for.
	 * @return The level of that enchantment, or 0 if it's not there.
	 */
	public int getEnchantmentLevel(String enchantId) {
		if (!stack.hasEnchantments()) return 0;
		Optional<Enchantment> opt = Registry.ENCHANTMENT.getOrEmpty(new Identifier(enchantId));
		if (!opt.isPresent()) return 0;
		Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
		return enchants.getOrDefault(opt.get(), 0);
	}

	/**
	 * @param key The key to check the value of.
	 * @return The object at that key.
	 */
	public Object getTagValue(String key) {
		CompoundTag tag = stack.getOrCreateTag();
		return NbtUtils.getObjectFor(tag.get(key));
	}

	/**
	 * NOTE: This is modifiable in {@link MutableStack}, but not in StackInfo.
	 * @return a view of the object's NBT, wrapped for usability.
	 */
	public WrappedCompoundTag getTag() {
		return new WrappedCompoundTag(stack.getOrCreateTag());
	}

}
