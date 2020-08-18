package io.github.cottonmc.libdp.api.driver.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.cottonmc.libcd.api.CustomOutputRecipe;
import io.github.cottonmc.libdp.api.DPSyntaxError;
import io.github.cottonmc.libdp.api.Diskette;
import io.github.cottonmc.libdp.api.driver.Driver;
import io.github.cottonmc.libdp.api.util.NbtMatchType;
import io.github.cottonmc.libdp.impl.IngredientAccessUtils;
import io.github.cottonmc.libdp.impl.RecipeMapAccessor;
import io.github.cottonmc.libdp.impl.ReloadListenersAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.concurrent.Executor;

/**
 * Might need some changes to properly work, I haven't linted this
 */
public class LegacyRecipeDriver implements Driver {
	public static final LegacyRecipeDriver INSTANCE = new LegacyRecipeDriver();
	private RecipeManager recipeManager;
	private int removeCount;
	private Map<RecipeType<?>, List<Identifier>> toRemove = new HashMap<>();
	private Map<RecipeType<?>, List<Item>> removeFor = new HashMap<>();
	private String currentNamespace = "libdp";
	private boolean canAddRecipes = false;
	private Logger logger;
	private JsonObject recipeDebug;

	private LegacyRecipeDriver() {}

	/**
	 * Used during data pack loading to set up recipe adding.
	 * DO NOT CALL THIS YOURSELF, EVER. IT WILL LIKELY MESS THINGS UP.
	 */
	@Override
	public void prepareReload(ResourceManager manager) {
		recipeDebug = new JsonObject();
		recipeCount = 0;
		toRemove.clear();
		removeFor.clear();
		if (manager instanceof ReloadListenersAccessor) {
			List<ResourceReloadListener> listeners = ((ReloadListenersAccessor)manager).libdp$getListeners();
			for (ResourceReloadListener listener : listeners) {
				if (listener instanceof RecipeManager) {
					this.recipeManager = (RecipeManager)listener;
					canAddRecipes = true;
					return;
				}
			}
			logger.error("No recipe manager was found! Tweaker cannot register recipes!");
			throw new IllegalStateException("No recipe manager was found! Tweaker cannot register recipes!");
		}
		logger.error("No reload listeners accessor found! Tweaker cannot register recipes!");
		throw new IllegalStateException("No reload listeners accessor found! Tweaker cannot register recipes!");
	}

	/**
	 * Used during data pack applying to directly apply recipes.
	 * This is "safe" to call yourself, but will result in a *lot* of log spam.
	 * NOTE: for some reason, Mojang decided to make the recipe map entirely immutable!
	 *   I used to respect it, but realized this may take a lot of time in a driver-heavy
	 *   modpack, so immutability is no longer preserved as of TODO: version this changes
	 */
	@Override
	public void applyReload(ResourceManager manager, Executor executor) {
		Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipeMap = new HashMap<>(((RecipeMapAccessor)recipeManager).libdp$getRecipeMap());
		Set<RecipeType<?>> types = new HashSet<>(recipeMap.keySet());
		types.addAll(toAdd.keySet());
		JsonArray removed = new JsonArray();
		for (RecipeType<?> type : types) {
			Identifier preTypeId = Registry.RECIPE_TYPE.getId(type);
			String typeId = preTypeId == null? "unknown" : preTypeId.toString();
			Map<Identifier, Recipe<?>> map = new HashMap<>(recipeMap.getOrDefault(type, new HashMap<>()));
			//remove before we add, so that we don't accidentally remove our own recipes!
			for (Identifier recipeId : toRemove.getOrDefault(type, new ArrayList<>())) {
				if (map.containsKey(recipeId)) {
					map.remove(recipeId);
					removeCount++;
					removed.add(typeId + " - " + recipeId.toString());
				} else logger.error("Could not find recipe to remove: " + recipeId.toString());
			}
			for (Identifier id : new HashSet<>(map.keySet())) {
				Recipe<?> recipe = map.get(id);
				boolean shouldRemove = false;
				if (recipe instanceof CustomOutputRecipe) {
					Collection<Item> items = ((CustomOutputRecipe)recipe).getOutputItems();
					for (Item item : items) {
						if (removeFor.getOrDefault(type, Collections.emptyList()).contains(item)) shouldRemove = true;
					}
				} else {
					if (removeFor.getOrDefault(type, Collections.emptyList()).contains(recipe.getOutput().getItem())) shouldRemove = true;
				}
				if (shouldRemove) {
					map.remove(id);
					removeCount++;
					removed.add(typeId + " - " + id.toString());
				}
			}
			recipeMap.put(type, map);
		}
		((RecipeMapAccessor)recipeManager).libdp$setRecipeMap(recipeMap);
		currentNamespace = "libdp";
		recipeDebug.add("removed", removed);
		canAddRecipes = false;
	}

	@Override
	public String getApplyMessage() {
		return removeCount + " removed " + (removeCount == 1? "recipe" : "recipes");
	}

	@Override
	public void prepareFor(Diskette bridge) {
		Identifier scriptId = bridge.getId();
		this.currentNamespace = scriptId.getNamespace();
		this.logger = LogManager.getLogger(scriptId.getNamespace());
	}

	/**
	 * Remove a recipe from the recipe manager.
	 * @param id The id of the recipe to remove.
	 */
	public void removeRecipe(String id) {
		if (!canAddRecipes) throw new RuntimeException("Someone tried to remove recipes via LibDP outside of reload time!");
		Identifier formatted = new Identifier(id);
		Optional<? extends Recipe<?>> opt = recipeManager.get(formatted);
		if (opt.isPresent()) {
			Recipe<?> recipe = opt.get();
			RecipeType<?> type = recipe.getType();
			if (!toRemove.containsKey(type)) toRemove.put(type, new ArrayList<>());
			List<Identifier> removal = toRemove.get(type);
			removal.add(formatted);
		} else {
			getLogger().error("Tried to remove recipe %s that doesn't exist", id);
		}
	}

	/**
	 * Remove all recipes outputting a certain item from the recipe manager.
	 * @param id The id of the output item to remove recipes for.
	 */
	public void removeRecipesFor(String id) {
		if (!canAddRecipes) throw new RuntimeException("Someone tried to remove recipes via LibDP outside of reload time!");
		Identifier formatted = new Identifier(id);
		Item item = Registry.ITEM.get(formatted);
		if (item != Items.AIR) {
			for (Identifier typeId : Registry.RECIPE_TYPE.getIds()) {
				RecipeType type = Registry.RECIPE_TYPE.get(typeId);
				if (!removeFor.containsKey(type)) removeFor.put(type, new ArrayList<>());
				removeFor.get(type).add(item);
			}
		} else {
			logger.error("Couldn't find item to remove recipes for: " + id);
		}
	}

	/**
	 * Remove all recipes outputting a certain item and of a certain recipe type from the recipe manager.
	 * @param id The id of the output item to remove recipes for.
	 * @param type The type of recipe to remove recipes for.
	 */
	public void removeRecipesFor(String id, String type) {
		if (!canAddRecipes) throw new RuntimeException("Someone tried to remove recipes via LibDP outside of reload time!");
		Identifier formatted = new Identifier(id);
		Identifier typeId = new Identifier(type);
		Item item = Registry.ITEM.get(formatted);
		if (item != Items.AIR) {
			RecipeType rType = Registry.RECIPE_TYPE.get(typeId);
			if (!removeFor.containsKey(rType)) removeFor.put(rType, new ArrayList<>());
			removeFor.get(rType).add(item);
		} else {
			logger.error("Couldn't find item to remove recipes for: " + id);
		}
	}

	/**
	 * Begin building a recipe from JSON if the recipe doesn't have intrinsic LibDP support.
	 * @param type the ID of the recipe serializer to use.
	 * @return A builder to start the recipe.
	 */
	public RecipeBuilder builder(String type) {
		return new RecipeBuilder(Registry.RECIPE_SERIALIZER.get(new Identifier(type)));
	}

	/**
	 * Get a recipe ingredient from an item stack. Call this from java driver classes.
	 * @param stack The item stack to make an ingredient for.
	 * @return The wrapped ingredient of the stack.
	 */
	public Ingredient ingredientForStack(ItemStack stack) {
		return RecipeParser.hackStackIngredients(stack);
	}

	/**
	 * Make an Ingredient object to pass to recipes from a string of inputs.
	 * @param nbtMatch The NBT matching type to use: "none", "fuzzy", or "exact".
	 * @param inputs The string forms of inputs to add to the Ingredient.
	 * @return An Ingredient object to pass to recipes, or an empty ingredient if there's a malformed input..
	 */
	public Ingredient makeIngredient(String nbtMatch, String...inputs) {
		List<ItemStack> stacks = new ArrayList<>();
		NbtMatchType match = NbtMatchType.forName(nbtMatch);
		for (String input : inputs) {
			try {
				ItemStack[] in = ((IngredientAccessUtils)(Object)RecipeParser.processIngredient(input)).libdp$getStackArray();
				stacks.addAll(Arrays.asList(in));
			} catch (DPSyntaxError e) {
				logger.error("Could not add stack to ingredient: malformed stack string %s", input);
				return Ingredient.EMPTY;
			}
		}
		Ingredient ret = RecipeParser.hackStackIngredients(stacks.toArray(new ItemStack[]{}));
		((IngredientAccessUtils)(Object)ret).libdp$setMatchType(match);
		return ret;
	}

	public void addShaped(Object[][] inputs, Object output) {
		addShaped(inputs, output, "");
	}

	/**
	 * Add a shaped recipe from a 2D array of inputs, like a standard CraftTweaker recipe.
	 * @param inputs the 2D array (array of arrays) of inputs to use.
	 * @param output The output of the recipe.
	 * @param group The recipe group to go in, or "" for none.
	 */
	public void addShaped(Object[][] inputs, Object output, String group) {
		try {
			Object[] processed = RecipeParser.processGrid(inputs);
			int width = inputs[0].length;
			int height = inputs.length;
			addShaped(processed, output, width, height, group);
		} catch (Exception e) {
			logger.error("Error parsing 2D array shaped recipe - " + e.getMessage());
		}
	}

	public void addShaped(Object[] inputs, Object output, int width, int height) {
		addShaped(inputs, output, width, height, "");
	}

	/**
	 * Register a shaped crafting recipe from a 1D array of inputs.
	 * @param inputs The input item or tag ids required in order: left to right, top to bottom.
	 * @param output The output of the recipe.
	 * @param width How many rows the recipe needs.
	 * @param height How many columns the recipe needs.
	 * @param group The recipe group to go in, or "" for none.
	 */
	public void addShaped(Object[] inputs, Object output, int width, int height, String group){
		try {
			ItemStack stack = RecipeParser.processItemStack(output);
			Identifier recipeId = RecipeDriver.INSTANCE.getRecipeId(stack);
			DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
			for (int i = 0; i < Math.min(inputs.length, width * height); i++) {
				Object id = inputs[i];
				if (id == null || id.equals("") || id.equals("minecraft:air")) continue;
				ingredients.set(i, RecipeParser.processIngredient(id));
			}
			RecipeDriver.INSTANCE.addRecipe(new ShapedRecipe(recipeId, group, width, height, ingredients, stack));
		} catch (Exception e) {
			logger.error("Error parsing 1D array shaped recipe - " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void addDictShaped(String[] pattern, Map<String, Object> dictionary, Object output) {
		addDictShaped(pattern, dictionary, output, "");
	}

	/**
	 * Register a shaped crafting recipe from a pattern and dictionary.
	 * @param pattern A crafting pattern like one you'd find in a vanilla recipe JSON.
	 * @param dictionary A map of single characters to item or tag ids.
	 * @param output The output of the recipe.
	 * @param group The recipe group to go in, or "" for none.
	 */
	public void addDictShaped(String[] pattern, Map<String, Object> dictionary, Object output, String group) {
		try {
			ItemStack stack = RecipeParser.processItemStack(output);
			Identifier recipeId = RecipeDriver.INSTANCE.getRecipeId(stack);
			pattern = RecipeParser.processPattern(pattern);
			Map<String, Ingredient> map = RecipeParser.processDictionary(dictionary);
			int x = pattern[0].length();
			int y = pattern.length;
			DefaultedList<Ingredient> ingredients = RecipeParser.getIngredients(pattern, map, x, y);
			RecipeDriver.INSTANCE.addRecipe(new ShapedRecipe(recipeId, group, x, y, ingredients, stack));
		} catch (Exception e) {
			logger.error("Error parsing dictionary shaped recipe - " + e.getMessage());
		}
	}

	public void addShapeless(Object[] inputs, Object output) {
		addShapeless(inputs, output, "");
	}

	/**
	 * Register a shapeless crafting recipe from an array of inputs.
	 * @param inputs A list of input item or tag ids required for the recipe.
	 * @param output The output of the recipe.
	 * @param group The recipe group to go in, or "" for none.
	 */
	public void addShapeless(Object[] inputs, Object output, String group) {
		try {
			ItemStack stack = RecipeParser.processItemStack(output);
			Identifier recipeId = RecipeDriver.INSTANCE.getRecipeId(stack);
			DefaultedList<Ingredient> ingredients = DefaultedList.of();
			for (int i = 0; i < Math.min(inputs.length, 9); i++) {
				Object id = inputs[i];
				if (id.equals("")) continue;
				ingredients.add(i, RecipeParser.processIngredient(id));
			}
			RecipeDriver.INSTANCE.addRecipe(new ShapelessRecipe(recipeId, group, stack, ingredients));
		} catch (Exception e) {
			logger.error("Error parsing shapeless recipe - " + e.getMessage());
		}
	}

	public void addSmelting(Object input, Object output, int ticks, float xp) {
		addSmelting(input, output, ticks, xp, "");
	}

	/**
	 * Register a recipe to smelt in a standard furnace.
	 * @param input The input item or tag id.
	 * @param output The output of the recipe.
	 * @param cookTime How many ticks (1/20 of a second) to cook for. Standard value: 200
	 * @param xp How many experience points to drop per item, on average.
	 * @param group The recipe group to go in, or "" for none.
	 */
	public void addSmelting(Object input, Object output, int cookTime, float xp, String group) {
		try {
			ItemStack stack = RecipeParser.processItemStack(output);
			Identifier recipeId = RecipeDriver.INSTANCE.getRecipeId(stack);
			Ingredient ingredient = RecipeParser.processIngredient(input);
			RecipeDriver.INSTANCE.addRecipe(new SmeltingRecipe(recipeId, group, ingredient, stack, xp, cookTime));
		} catch (Exception e) {
			logger.error("Error parsing smelting recipe - " + e.getMessage());
		}
	}

	public void addBlasting(Object input, Object output, int ticks, float xp) {
		addBlasting(input, output, ticks, xp, "");
	}

	/**
	 * Register a recipe to smelt in a blast furnace.
	 * @param input The input item or tag id.
	 * @param output The output of the recipe.
	 * @param cookTime How many ticks (1/20 of a second) to cook for. Standard value: 100
	 * @param xp How many experience points to drop per item, on average.
	 * @param group The recipe group to go in, or "" for none.
	 */
	public void addBlasting(Object input, Object output, int cookTime, float xp, String group) {
		try {
			ItemStack stack = RecipeParser.processItemStack(output);
			Identifier recipeId = RecipeDriver.INSTANCE.getRecipeId(stack);
			Ingredient ingredient = RecipeParser.processIngredient(input);
			RecipeDriver.INSTANCE.addRecipe(new BlastingRecipe(recipeId, group, ingredient, stack, xp, cookTime));
		} catch (Exception e) {
			logger.error("Error parsing blasting recipe - " + e.getMessage());
		}
	}

	public void addSmoking(Object input, Object output, int ticks, float xp) {
		addSmoking(input, output, ticks, xp, "");
	}

	/**
	 * Register a recipe to cook in a smoker.
	 * @param input The input item or tag id.
	 * @param output The output of the recipe.
	 * @param cookTime How many ticks (1/20 of a second) to cook for. Standard value: 100
	 * @param xp How many experience points to drop per item, on average.
	 * @param group The recipe group to go in, or "" for none.
	 */
	public void addSmoking(Object input, Object output, int cookTime, float xp, String group) {
		try {
			ItemStack stack = RecipeParser.processItemStack(output);
			Identifier recipeId = RecipeDriver.INSTANCE.getRecipeId(stack);
			Ingredient ingredient = RecipeParser.processIngredient(input);
			RecipeDriver.INSTANCE.addRecipe(new SmokingRecipe(recipeId, group, ingredient, stack, xp, cookTime));
		} catch (Exception e) {
			logger.error("Error parsing smokig recipe - " + e.getMessage());
		}
	}

	public void addCampfire(Object input, Object output, int ticks, float xp) {
		addCampfire(input, output, ticks, xp, "");
	}

	/**
	 * Register a recipe to cook on a campfire.
	 * @param input The input item or tag id.
	 * @param output The output of the recipe.
	 * @param cookTime How many ticks (1/20 of a second) to cook for. Standard value: 600
	 * @param xp How many experience points to drop per item, on average.
	 * @param group The recipe group to go in, or "" for none.
	 */
	public void addCampfire(Object input, Object output, int cookTime, float xp, String group) {
		try {
			ItemStack stack = RecipeParser.processItemStack(output);
			Identifier recipeId = RecipeDriver.INSTANCE.getRecipeId(stack);
			Ingredient ingredient = RecipeParser.processIngredient(input);
			RecipeDriver.INSTANCE.addRecipe(new CampfireCookingRecipe(recipeId, group, ingredient, stack, xp, cookTime));
		} catch (Exception e) {
			logger.error("Error parsing campfire recipe - " + e.getMessage());
		}
	}

	public void addStonecutting(Object input, Object output) {
		addStonecutting(input, output, "");
	}

	/**
	 * Register a recipe to cut in the stonecutter.
	 * @param input The input item or tag id.
	 * @param output The output of the recipe.
	 * @param group The recipe group to go in, or "" for none.
	 */
	public void addStonecutting(Object input, Object output, String group) {
		try {
			ItemStack stack = RecipeParser.processItemStack(output);
			Identifier recipeId = RecipeDriver.INSTANCE.getRecipeId(stack);
			Ingredient ingredient = RecipeParser.processIngredient(input);
			RecipeDriver.INSTANCE.addRecipe(new StonecuttingRecipe(recipeId, group, ingredient, stack));
		} catch (Exception e) {
			logger.error("Error parsing stonecutter recipe - " + e.getMessage());
		}
	}

	/**
	 * Register a recipe to forge in the smithing table.
	 * @param base The base input item or tag id.
	 * @param addition The addition input item or tag id.
	 * @param output The output of the recipe.
	 */
	public void addSmithing(Object base, Object addition, Object output) {
		try {
			ItemStack stack = RecipeParser.processItemStack(output);
			Identifier recipeId = RecipeDriver.INSTANCE.getRecipeId(stack);
			Ingredient baseIng = RecipeParser.processIngredient(base);
			Ingredient addIng = RecipeParser.processIngredient(addition);
			RecipeDriver.INSTANCE.addRecipe(new SmithingRecipe(recipeId, baseIng, addIng, stack));
		} catch (Exception e) {
			logger.error("Error parsing smithing recipe - " + e.getMessage());
		}
	}

	public Logger getLogger() {
		return logger;
	}

	public JsonObject getRecipeDebug() {
		return recipeDebug;
	}

	@Override
	public JsonObject getDebugInfo() {
		return recipeDebug;
	}
}