package io.github.cottonmc.libdp.api.driver.recipe;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import io.github.cottonmc.libcd.api.tag.TagHelper;
import io.github.cottonmc.libdp.LibDP;
import io.github.cottonmc.libdp.api.DPSyntaxError;
import io.github.cottonmc.libdp.api.driver.util.DriverUtils;
import io.github.cottonmc.libdp.api.util.NbtMatchType;
import io.github.cottonmc.libdp.api.util.MutableStack;
import io.github.cottonmc.libdp.impl.IngredientAccessUtils;
import io.netty.buffer.Unpooled;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

import java.util.*;

/**
 * Helper class to make public versions of private recipe methods
 */
public class RecipeParser {

	/**
	 * Get an Ingredient from a string item or tag id.
	 * @param input The id to use, with a # at the front if it's a tag or -> between two ids for a getter
	 * @return the Ingredient for the given id
	 */
	public static Ingredient processIngredient(Object input) throws DPSyntaxError {
		if (input instanceof Ingredient) return (Ingredient) input;
		else if (input instanceof MutableStack) {
			ItemStack stack = ((MutableStack) input).get();
			Ingredient ing = hackStackIngredients(stack);
			if (stack.hasTag()) {
				((IngredientAccessUtils) (Object) ing).libdp$setMatchType(NbtMatchType.EXACT);
			}
			return ing;
		} else if (input instanceof Item) {
			return Ingredient.ofItems((Item) input);
		} else if (input instanceof Item[]) {
			return Ingredient.ofItems((Item[]) input);
		} else if (input instanceof ItemStack) {
			ItemStack stack = (ItemStack) input;
			Ingredient ing = hackStackIngredients(stack);
			if (stack.hasTag()) {
				((IngredientAccessUtils) (Object) ing).libdp$setMatchType(NbtMatchType.EXACT);
			}
			return ing;
		} else if (input instanceof ItemStack[]) {
			ItemStack[] stacks = (ItemStack[]) input;
			boolean needsTags = false;
			for (int i = 0; i < stacks.length; i++) {
				ItemStack stack = stacks[i];
				if (stack.hasTag()) {
					needsTags = true;
				}
			}
			Ingredient ing = hackStackIngredients(stacks);
			if (needsTags) {
				((IngredientAccessUtils) (Object) ing).libdp$setMatchType(NbtMatchType.EXACT);
			}
			return ing;
		} else if (input instanceof String) {
			String in = (String) input;
			int index = in.indexOf('{');
			String nbt = "";
			NbtMatchType type = NbtMatchType.NONE;
			List<ItemStack> stacks = new ArrayList<>();
			if (index != -1) {
				int andIndex = in.indexOf('&');
				if (andIndex != -1) {
					type = NbtMatchType.forName(in.substring(andIndex+1));
					in = in.substring(0, andIndex);
				}
				nbt = in.substring(index);
				in = in.substring(0, index);
			}
			if (in.indexOf('#') == 0) {
				String tag = in.substring(1);
				Tag<Item> itemTag = ServerTagManagerHolder.getTagManager().getItems().getTag(new Identifier(tag));
				if (itemTag == null) throw new DPSyntaxError("Failed to get item tag for input: " + in);
				for (Item item : itemTag.values()) {
					stacks.add(new ItemStack(item));
				}
			} else if (in.contains("->")) {
				ItemStack stack = DriverUtils.INSTANCE.makeSpecialStack(in);
				if (stack.isEmpty())
					throw new DPSyntaxError("Failed to get special stack for input: " + in);
				stacks.add(stack);
				type = NbtMatchType.EXACT;
			} else {
				Item item = DriverUtils.INSTANCE.getRawItem(in);
				if (item == Items.AIR) throw new DPSyntaxError("Failed to get item for input: " + in);
				stacks.add(new ItemStack(item));
			}
			if (!nbt.equals("")) {
				for (ItemStack stack : stacks) {
					if (!stack.hasTag() || stack.getTag().isEmpty()) {
						appendNbt(stack, nbt);
					}
				}
			}
			Ingredient ret = hackStackIngredients(stacks.toArray(new ItemStack[]{}));
			((IngredientAccessUtils)(Object)ret).libdp$setMatchType(type);
			return ret;
		}
		else throw new DPSyntaxError("Illegal object passed to recipe parser of type " + input.getClass().getName());
	}

	public static ItemStack processItemStack(Object input) throws DPSyntaxError {
		if (input instanceof ItemStack) return (ItemStack) input;
		else if (input instanceof MutableStack) return ((MutableStack) input).get();
		else if (input instanceof Item) return (new ItemStack((Item) input));
		else if (input instanceof String) {
			String in = (String) input;
			int atIndex = in.lastIndexOf('@');
			int nbtIndex = in.indexOf('{');
			int count = 1;
			if (atIndex != -1 && atIndex > in.lastIndexOf('}')) {
				count = Integer.parseInt(in.substring(atIndex + 1));
				in = in.substring(0, atIndex);
			}
			String nbt = "";
			if (nbtIndex != -1) {
				nbt = in.substring(nbtIndex);
				in = in.substring(0, nbtIndex);
			}
			Item item;
			if (in.indexOf('#') == 0) {
				String tag = in.substring(1);
				Tag<Item> itemTag = ServerTagManagerHolder.getTagManager().getItems().getTag(new Identifier(tag));
				if (itemTag == null) throw new DPSyntaxError("Failed to get item tag for output: " + in);
				item = TagHelper.ITEM.getDefaultEntry(itemTag);
			} else if (in.contains("->")) {
				ItemStack stack = DriverUtils.INSTANCE.makeSpecialStack(in);
				if (stack.isEmpty())
					throw new DPSyntaxError("Failed to get special stack for output: " + in);
				if (stack.isStackable()) {
					stack.setCount(count);
				}
				return stack;
			} else {
				item = DriverUtils.INSTANCE.getRawItem(in);
			}

			ItemStack stack = new ItemStack(item, count);
			if (!nbt.equals("")) {
				appendNbt(stack, nbt);
			}

			return stack;
		}
		else throw new DPSyntaxError("Illegal object passed to recipe parser of type " + input.getClass().getName());
	}

	/**
	 * Split a factory string into the ids of the factory/id.
	 * @param base The base factory string to split.
	 * @return A two-item array of the two parts of the getter.
	 */
	public static String[] processStackFactory(String base) {
		String[] split = new String[2];
		int splitter = base.indexOf("->");
		split[0] = base.substring(0, splitter);
		split[1] = base.substring(splitter + 2);
		return split;
	}

	/**
	 * Process a grid of inputs CraftTweaker-style. Max of 3 width and height.
	 * @param inputs The array of string arrays to process inputs from
	 * @return The inputs converted into a single string array if the grid is valid
	 */
	public static Object[] processGrid(Object[][] inputs) throws DPSyntaxError {
		return processGrid(inputs, 3, 3);
	}

	/**
	 * Process a grid of inputs CraftTweaker-style.
	 * @param inputs The array of string arrays to process inputs from
	 * @param maxWidth The maximum number of columns allowed
	 * @param maxHeight The maximum number of rows allowed
	 * @return The inputs converted into a single string array if the grid is valid
	 */
	public static Object[] processGrid(Object[][] inputs, int maxWidth, int maxHeight) throws DPSyntaxError {
		if (inputs.length > maxHeight) throw new DPSyntaxError("Invalid pattern: too many rows, " + maxHeight + " is maximum");
		if (inputs.length == 0) throw new DPSyntaxError("Invalid pattern: empty pattern is not allowed");
		int width = inputs[0].length;
		List<Object> output = new ArrayList<>();
		for (int i = 0; i < inputs.length; i++) {
			Object[] row = inputs[i];
			if (row.length > maxWidth) throw new DPSyntaxError("Invalid pattern: too many columns, " + maxWidth + " is maximum");
			if (row.length != width) throw new DPSyntaxError("Invalid pattern: each row must be the same width");
			for (int j = 0; j < width; j++) {
				output.add(inputs[i][j]);
			}
		}
		return output.toArray();
	}

	/**
	 * validate and parse a recipe pattern. Max of 3 width and height.
	 * @param pattern up to three strings of up to three characters each for the pattern
	 * @return processed pattern
	 */
	public static String[] processPattern(String... pattern) throws DPSyntaxError {
		return processPattern(3, 3, pattern);
	}

	/**
	 * Validate and parse a recipe pattern.
	 * @param maxWidth The maximum number of columns allowed
	 * @param maxHeight The maximum number of rows allowed
	 * @param pattern Up to <height> strings of up to <width> characters each for the pattern
	 * @return processed pattern
	 */
	public static String[] processPattern(int maxWidth, int maxHeight, String... pattern) throws DPSyntaxError {
		if (pattern.length > 3) {
			throw new DPSyntaxError("Invalid pattern: too many rows, " + maxHeight + " is maximum");
		} else if (pattern.length == 0) {
			throw new DPSyntaxError("Invalid pattern: empty pattern not allowed");
		} else {
			for (int i = 0; i < pattern.length; i++) {
				String row = pattern[i];
				if (row.length() > 3) {
					throw new DPSyntaxError("Invalid pattern: too many columns, " + maxWidth + " is maximum");
				}

				if (i > 0 && pattern[0].length() != row.length()) {
					throw new DPSyntaxError("Invalid pattern: each row must be the same width");
				}

				pattern[i] = row;
			}
			int nextIndex = 2147483647;
			int highIndex = 0;
			int checked = 0;
			int sinceLastEmpty = 0;

			for (int i = 0; i < pattern.length; ++i) {
				String input = pattern[i];
				nextIndex = Math.min(nextIndex, findNextIngredient(input));
				int lastIndex = findNextIngredientReverse(input);
				highIndex = Math.max(highIndex, lastIndex);
				if (lastIndex < 0) {
					if (checked == i) {
						++checked;
					}

					++sinceLastEmpty;
				} else {
					sinceLastEmpty = 0;
				}
			}

			if (pattern.length == sinceLastEmpty) {
				return new String[0];
			} else {
				String[] combined = new String[pattern.length - sinceLastEmpty - checked];

				for (int i = 0; i < combined.length; ++i) {
					combined[i] = pattern[i + checked].substring(nextIndex, highIndex + 1);
				}

				return combined;
			}
		}
	}

	/**
	 * Process dictionaries into a Recipe-readable form.
	 * @param dictionary a map of keys to values for a recipe to parse.
	 * @return A map of string keys to ingredient values that a Recipe can read.
	 */
	public static Map<String, Ingredient> processDictionary(Map<String, Object> dictionary) throws DPSyntaxError {
		Map<String, Ingredient> map = new HashMap<>();
		for (Map.Entry<String, Object> entry : dictionary.entrySet()) {
			if (entry.getKey().length() != 1) {
				throw new DPSyntaxError("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
			}

			if (" ".equals(entry.getKey())) {
				throw new DPSyntaxError("Invalid key entry: ' ' is a reserved symbol.");
			}
			map.put(entry.getKey(), processIngredient(entry.getValue()));
		}
		map.put(" ", Ingredient.EMPTY);
		return map;
	}

	/**
	 * Compile a pattern and dictionary into a full ingredient list.
	 * @param pattern A patern parsed by processPattern.
	 * @param dictionary A dictionary parsed by processDictionary.
	 * @param x How many columns there are.
	 * @param y How many rows there are.
	 * @return A defaulted list of ingredients.
	 */
	public static DefaultedList<Ingredient> getIngredients(String[] pattern, Map<String, Ingredient> dictionary, int x, int y) throws DPSyntaxError {
		DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(x * y, Ingredient.EMPTY);
		Set<String> keys = Sets.newHashSet(dictionary.keySet());
		keys.remove(" ");

		for(int i = 0; i < pattern.length; i++) {
			for(int j = 0; j < pattern[i].length(); j++) {
				String key = pattern[i].substring(j, j + 1);
				Ingredient ingredient = dictionary.get(key);
				if (ingredient == null) {
					throw new DPSyntaxError("Pattern references symbol '" + key + "' but it's not defined in the key");
				}

				keys.remove(key);
				ingredients.set(j + x * i, ingredient);
			}
		}

		if (!keys.isEmpty()) {
			throw new DPSyntaxError("Key defines symbols that aren't used in pattern: " + keys);
		} else {
			return ingredients;
		}
	}

	/**
	 * Skip forwards through a recipe row to find an ingredient key
	 * @param input a recipe row to parse
	 * @return index for the next ingredient character
	 */
	private static int findNextIngredient(String input) {
		int i;
		for (i = 0; i < input.length() && input.charAt(i) == ' '; i++) { }
		return i;
	}

	/**
	 * Skip backwards through a recipe row to find an ingredient key
	 * @param input a recipe row to parse
	 * @return index for the next ingredient character
	 */
	private static int findNextIngredientReverse(String input) {
		int i;
		for (i = input.length() - 1; i >= 0 && input.charAt(i) == ' '; i--) { }
		return i;
	}

	/**
	 * Thanks, ProGuard! The `Ingredient.ofStacks()` method is currently only in the client environment,
	 * so I have to write this ugly, terrible hack to make it work!
	 * Serializes the input stacks into a PacketByteBuf,
	 * then tricks the Ingredient class into deserializing them.
	 * However, if NBT Crafting is here, I can just do it with that!
	 * @param stacks The input stacks to support.
	 * @return The ingredient object for the input stacks.
	 */
	public static Ingredient hackStackIngredients(ItemStack...stacks) {
		if (FabricLoader.getInstance().isModLoaded("nbtcrafting")) {
			if (stacks.length > 1) {
				JsonArray array = new JsonArray();
				for (ItemStack stack : stacks) {
					array.add(serializeStack(stack));
				}
				return Ingredient.fromJson(array);
			} else {
				return Ingredient.fromJson(serializeStack(stacks[0]));
			}
//			return IngredientAssembler.constructFromStacks(stacks);
		} else {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeVarInt(stacks.length);
			for (ItemStack stack : stacks) {
				buf.writeItemStack(stack);
			}
			return Ingredient.fromPacket(buf);
		}
	}

	/*
	 * Helpful if the NBT Crafting `constructFromStacks` doesn't work. It doesn't currently, so here it is.
	 */
	private static JsonObject serializeStack(ItemStack stack) {
		JsonObject ret = new JsonObject();
		ret.addProperty("item", Registry.ITEM.getId(stack.getItem()).toString());
		ret.addProperty("count", stack.getCount());
		if (stack.hasTag()) {
			JsonObject data = Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, stack.getTag()).getAsJsonObject();
			ret.add("data", data);
		}
		return ret;
	}

	private static void appendNbt(ItemStack stack, String nbt) {
		StringNbtReader reader = new StringNbtReader(new StringReader(nbt));
		try {
			CompoundTag tag = reader.parseCompoundTag();
			stack.setTag(tag);
		} catch (CommandSyntaxException e) {
			LibDP.LOGGER.error("Error adding NBT to stack: " + e.getMessage());
		}
	}
}
