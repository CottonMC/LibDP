package io.github.cottonmc.libdp.api.driver.recipe;

import com.google.gson.*;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.registry.Registry;

import java.util.Map;

/**
 * A builder for recipes without inherent LibCD support. Assembles a JSON object and constructs a recipe for it.
 */
public class RecipeBuilder {
	private final JsonObject json = new JsonObject();
	private final RecipeSerializer<?> serializer;
	private ItemStack idStack = ItemStack.EMPTY;
	private final LegacyRecipeDriver driver = LegacyRecipeDriver.INSTANCE;

	/**
	 * @param serializer The serializer to use for this recipe.
	 */
	public RecipeBuilder(RecipeSerializer<?> serializer) {
		this.serializer = serializer;
	}

	/**
	 * @param key The key to place this ingredient at. Typically "ingredient".
	 * @param ingredient An object to be parsed as an ingredient.
	 * @return This builder with the ingredient added.
	 */
	public RecipeBuilder ingredient(String key, Object ingredient) {
		try {
			Ingredient ing = RecipeParser.processIngredient(ingredient);
			json.add(key, ing.toJson());
		} catch (Exception e) {
			driver.getLogger().error("Error processing recipe builder ingredient - " + e.getMessage());
		}
		return this;
	}

	/**
	 * @param key The key to place this ingredient at. Typically "ingredient".
	 * @param ingredients An array of objects to be parsed as ingredients.
	 * @return This builder with the ingredient array added.
	 */
	public RecipeBuilder ingredientArray(String key, Object[] ingredients) {
		try {
			JsonArray array = new JsonArray();
			for (Object ing : ingredients) {
				array.add(RecipeParser.processIngredient(ing).toJson());
			}
			json.add(key, array);
		} catch (Exception e) {
			driver.getLogger().error("Error processing recipe builder ingredient array - " + e.getMessage());
		}
		return this;
	}

	/**
	 * Assemble an ingredient map for a shaped recipe.
	 * @param key The key to place this map at. Typically "key".
	 * @param map The map of single characters to objects to be parsed as ingredients.
	 * @return This builder with the ingredient key added.
	 */
	public RecipeBuilder ingredientMap(String key, Map<String, Object> map) {
		JsonObject obj = new JsonObject();
		try {
			for (String ch : map.keySet()) {
				Ingredient ing = RecipeParser.processIngredient(map.get(ch));
				obj.add(ch, ing.toJson());
			}
		} catch (Exception e) {
			driver.getLogger().error("Error processing recipe builder ingredient map - " + e.getMessage());
		}
		json.add(key, obj);
		return this;
	}

	/**
	 * @param key The key to place this item stack at. Typically "result".
	 * @param stack An object to be parsed as an item stack.
	 * @return This builder with the item stack added.
	 */
	public RecipeBuilder itemStack(String key, Object stack) {
		try {
			ItemStack s = RecipeParser.processItemStack(stack);
			if (idStack.isEmpty()) idStack = s;
			json.add(key, serializeStack(s));
		} catch (Exception e) {
			driver.getLogger().error("Error processing recipe builder item stack - " + e.getMessage());
		}
		return this;
	}

	/**
	 * @param key The key to place this item stack array at.
	 * @param stacks An array of objects to be parsed as item stacks.
	 * @return This builder with the item stack array added.
	 */
	public RecipeBuilder itemStackArray(String key, Object[] stacks) {
		try {
			JsonArray array = new JsonArray();
			for (Object ing : stacks) {
				array.add(serializeStack(RecipeParser.processItemStack(ing)));
			}
			json.add(key, array);
		} catch (Exception e) {
			driver.getLogger().error("Error processing recipe builder item stack array - " + e.getMessage());
		}
		return this;
	}

	/**
	 * For adding any primitive properies (numbers, booleans, or strings) needed for the recipe.
	 * @param key The key to place this property at.
	 * @param prop The number, boolean, or string to add.
	 * @return This builder with the property added.
	 */
	public RecipeBuilder property(String key, Object prop) {
		if (prop instanceof Number) {
			json.addProperty(key, (Number)prop);
		} else if (prop instanceof Boolean) {
			json.addProperty(key, (Boolean)prop);
		} else if (prop instanceof String) {
			json.addProperty(key, (String)prop);
		}
		return this;
	}

	/**
	 * @param key The key to place this property array at.
	 * @param props An array of numbers, booleans, and/or strings to add.
	 * @return This builder with the property added.
	 */
	public RecipeBuilder propertyArray(String key, Object[] props) {
		JsonArray array = new JsonArray();
		for (Object obj : props) {
			if (obj instanceof Number) {
				array.add((Number)obj);
			} else if (obj instanceof Boolean) {
				array.add((Boolean)obj);
			} else if (obj instanceof String) {
				array.add((String)obj);
			}
		}
		json.add(key, array);
		return this;
	}

	/**
	 * Add raw JSON to the recipe.
	 * @param key The key to place this JSON at.
	 * @param value The JSON to add.
	 * @return This builder with the JSON added.
	 */
	public RecipeBuilder value(String key, String value) {
		JsonParser parser = new JsonParser();
		json.add(key, parser.parse(value));
		return this;
	}

	/**
	 * Set the stack which will be used for the ID of this recipe.
	 * @param keyStack An object to be parsed as an item stack.
	 * @return This builder with the id stack set.
	 */
	public RecipeBuilder idStack(Object keyStack) {
		try {
			ItemStack stack = RecipeParser.processItemStack(keyStack);
			this.idStack = stack;
		} catch (Exception e) {
			driver.getLogger().error("Error processing recipe builder id stack - " + e.getMessage());
		}
		return this;
	}

	/**
	 * @return All the passed properties built into a recipe using the given recipe serializer. Pass this directly to {@link LegacyRecipeDriver#addRecipe(Recipe)}.
	 */
	public Recipe build() {
		return serializer.read(LegacyRecipeDriver.INSTANCE.getRecipeId(idStack), json);
	}

	private JsonObject serializeStack(ItemStack stack) {
		JsonObject ret = new JsonObject();
		ret.addProperty("item", Registry.ITEM.getId(stack.getItem()).toString());
		ret.addProperty("count", stack.getCount());
		//only add NBT for stacks if NBT Crafting is present so we don't run into any issues with `ShapedRecipe.getItemStack()`
		if (FabricLoader.getInstance().isModLoaded("nbtcrafting") && stack.hasTag()) {
			JsonObject data = Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, stack.getTag()).getAsJsonObject();
			ret.add("data", data);
		}
		return ret;
	}
}
