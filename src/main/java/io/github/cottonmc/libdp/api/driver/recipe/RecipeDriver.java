package io.github.cottonmc.libdp.api.driver.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.cottonmc.libdp.api.DPSyntaxError;
import io.github.cottonmc.libdp.api.driver.Driver;
import io.github.cottonmc.libdp.api.Diskette;
import io.github.cottonmc.libdp.api.util.NbtMatchType;
import io.github.cottonmc.libdp.impl.IngredientAccessUtils;
import io.github.cottonmc.libdp.impl.RecipeMapAccessor;
import io.github.cottonmc.libdp.impl.ReloadListenersAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.concurrent.Executor;

public class RecipeDriver implements Driver {
	public static final RecipeDriver INSTANCE = new RecipeDriver();
	
	private RecipeManager recipeManager;
	private int triedRecipeCount;
	private int recipeCount;
	private final Map<RecipeType<?>, List<Recipe<?>>> toAdd = new HashMap<>();
	private String currentNamespace = "libdp";
	private boolean canAddRecipes = false;
	private Logger logger;
	private JsonObject recipeDebug;

	private RecipeDriver() {}

	/**
	 * Used during data pack loading to set up recipe adding.
	 * DO NOT CALL THIS YOURSELF, EVER. IT WILL LIKELY MESS THINGS UP.
	 */
	@Override
	public void prepareReload(ResourceManager manager) {
		recipeDebug = new JsonObject();
		triedRecipeCount = -1;
		recipeCount = 0;
		toAdd.clear();
		if (manager instanceof ReloadListenersAccessor) {
			List<ResourceReloadListener> listeners = ((ReloadListenersAccessor)manager).libdp$getListeners();
			for (ResourceReloadListener listener : listeners) {
				if (listener instanceof RecipeManager) {
					this.recipeManager = (RecipeManager)listener;
					canAddRecipes = true;
					return;
				}
			}
			logger.error("No recipe manager was found! Driver cannot register recipes!");
			throw new IllegalStateException("No recipe manager was found! Driver cannot register recipes!");
		}
		logger.error("No reload listeners accessor found! Driver cannot register recipes!");
		throw new IllegalStateException("No reload listeners accessor found! Driver cannot register recipes!");
	}

	/**
	 * Used during data pack applying to directly apply recipes.
	 * This is "safe" to call yourself, but will result in a *lot* of log spam.
	 * NOTE: for some reason, Mojang decided to make the recipe map entirely immutable!
	 *   I used to respect it, but realized this may take a lot of time in a diskette-heavy
	 *   modpack, so I make it mutable.
	 */
	@Override
	public void applyReload(ResourceManager manager, Executor executor) {
		Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipeMap = new HashMap<>(((RecipeMapAccessor)recipeManager).libdp$getRecipeMap());
		Set<RecipeType<?>> types = new HashSet<>(recipeMap.keySet());
		types.addAll(toAdd.keySet());
		JsonArray added = new JsonArray();
		//TODO: should there be a hook for other mods to be able to remove recipes here? they can probably just make their own driver
		for (RecipeType<?> type : types) {
			Identifier preTypeId = Registry.RECIPE_TYPE.getId(type);
			String typeId = preTypeId == null? "unknown" : preTypeId.toString();
			Map<Identifier, Recipe<?>> map = new HashMap<>(recipeMap.getOrDefault(type, new HashMap<>()));
			for (Recipe<?> recipe : toAdd.getOrDefault(type, new ArrayList<>())) {
				Identifier id = recipe.getId();
				if (map.containsKey(id)) {
					logger.error("Failed to add recipe from diskette - duplicate recipe ID: " + id);
				} else try {
					map.put(id, recipe);
					recipeCount++;
					added.add(typeId + " - " + id.toString());
				} catch (Exception e) {
					logger.error("Failed to add recipe from diskette - " + e.getMessage());
				}
			}
			recipeMap.put(type, map);
		}
		((RecipeMapAccessor)recipeManager).libdp$setRecipeMap(recipeMap);
		currentNamespace = "libdp";
		recipeDebug.add("added", added);
		canAddRecipes = false;
	}

	@Override
	public String getApplyMessage() {
		return recipeCount + " " + (recipeCount == 1? "recipe" : "recipes");
	}

	@Override
	public void prepareFor(Diskette diskette) {
		Identifier scriptId = diskette.getId();
		this.currentNamespace = scriptId.getNamespace();
		this.logger = LogManager.getLogger(scriptId.getNamespace());
	}

	/**
	 * Generate a recipe ID. Call this from Java driver classes.
	 * @param output The output stack of the recipe.
	 * @return A unique identifier for the recipe.
	 */
	public Identifier getRecipeId(ItemStack output) {
		String resultName = Registry.ITEM.getId(output.getItem()).getPath();
		triedRecipeCount++;
		return new Identifier(currentNamespace, "diskette/"+resultName+"-"+triedRecipeCount);
	}
	
	public Identifier getRecipeId(ItemStack output, Diskette diskette) {
		String resultName = Registry.ITEM.getId(output.getItem()).getPath();
		triedRecipeCount++;
		return new Identifier(currentNamespace, diskette.getId().getPath()+"/"+resultName+"-"+triedRecipeCount);
	}

	/**
	 * Register a recipe to the recipe manager.
	 * @param recipe A constructed recipe.
	 * @throws RuntimeException if called outside of resource-reload time.
	 */
	public void addRecipe(Recipe<?> recipe) {
		if (!canAddRecipes) throw new RuntimeException("Someone tried to add recipes via LibDP outside of reload time!");
		RecipeType<?> type = recipe.getType();
		if (!toAdd.containsKey(type)) {
			toAdd.put(type, new ArrayList<>());
		}
		List<Recipe<?>> recipeList = toAdd.get(type);
		recipeList.add(recipe);
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
				ItemStack[] in = ((IngredientAccessUtils) (Object) RecipeParser.processIngredient(input))
						.libdp$getStackArray();
				stacks.addAll(Arrays.asList(in));
			} catch (DPSyntaxError e) {
				logger.error("Could not add stack to ingredient: malformed stack string {}", input);
				return Ingredient.EMPTY;
			}
		}
		Ingredient ret = RecipeParser.hackStackIngredients(stacks.toArray(new ItemStack[]{}));
		((IngredientAccessUtils) (Object) ret).libdp$setMatchType(match);
		return ret;
	}

	public void addShaped(Diskette diskette, Object[][] inputs, Object output) {
		addShaped(diskette, inputs, output, "");
	}

	/**
	 * Add a custom, script-based shaped recipe from a 2D array of inputs, like a standard CraftTweaker recipe.
	 * @param diskette The Diskette to use, obtained via `diskette.importScript()`.
	 * @param inputs the 2D array (array of arrays) of inputs to use.
	 * @param output The output of the recipe.
	 * @param group The recipe group to go in, or "" for none.
	 */
	public void addShaped(Diskette diskette, Object[][] inputs, Object output, String group) {
		try {
			Object[] processed = RecipeParser.processGrid(inputs);
			int width = inputs[0].length;
			int height = inputs.length;
			addShaped(diskette, processed, output, width, height, group);
		} catch (Exception e) {
			logger.error("Error parsing 2D array custom shaped recipe - " + e.getMessage());
		}
	}

	public void addShaped(Diskette diskette, Object[] inputs, Object output, int width, int height) {
		addShaped(diskette, inputs, output, width, height, "");
	}

	/**
	 * Register a custom, script-based shaped crafting recipe from a 1D array of inputs.
	 * @param diskette The Diskette to use, obtained via `diskette.importScript()`.
	 * @param inputs The input item or tag ids required in order: left to right, top to bottom.
	 * @param output The output of the recipe.
	 * @param width How many rows the recipe needs.
	 * @param height How many columns the recipe needs.
	 * @param group The recipe group to go in, or "" for none.
	 */
	public void addShaped(Diskette diskette, Object[] inputs, Object output, int width, int height, String group) {
		try {
			ItemStack stack = RecipeParser.processItemStack(output);
			Identifier recipeId = getRecipeId(stack, diskette);
			DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
			for (int i = 0; i < Math.min(inputs.length, width * height); i++) {
				Object id = inputs[i];
				if (id == null || id.equals("") || id.equals("minecraft:air")) continue;
				ingredients.set(i, RecipeParser.processIngredient(id));
			}
			addRecipe(new CustomShapedRecipe(diskette, recipeId, group, width, height, ingredients, stack));
		} catch (Exception e) {
			logger.error("Error parsing 1D array custom shaped recipe - " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void addDictShaped(Diskette diskette, String[] pattern, Map<String, Object> dictionary, Object output) {
		addDictShaped(diskette, pattern, dictionary, output, "");
	}

	/**
	 * Register a custom, script-based shaped crafting recipe from a pattern and dictionary.
	 * @param diskette The Diskette to use, obtained via `diskette.importScript()`.
	 * @param pattern A crafting pattern like one you'd find in a vanilla recipe JSON.
	 * @param dictionary A map of single characters to item or tag ids.
	 * @param output The output of the recipe.
	 * @param group The recipe group to go in, or "" for none.
	 */
	public void addDictShaped(Diskette diskette, String[] pattern, Map<String, Object> dictionary, Object output,
							  String group) {
		try {
			ItemStack stack = RecipeParser.processItemStack(output);
			Identifier recipeId = getRecipeId(stack, diskette);
			pattern = RecipeParser.processPattern(pattern);
			Map<String, Ingredient> map = RecipeParser.processDictionary(dictionary);
			int x = pattern[0].length();
			int y = pattern.length;
			DefaultedList<Ingredient> ingredients = RecipeParser.getIngredients(pattern, map, x, y);
			addRecipe(new CustomShapedRecipe(diskette, recipeId, group, x, y, ingredients, stack));
		} catch (Exception e) {
			logger.error("Error parsing custom dictionary shaped recipe - " + e.getMessage());
		}
	}

	public void addShapeless(Diskette diskette, Object[] inputs, Object output) {
		addShapeless(diskette, inputs, output, "");
	}

	/**
	 * Register a custom, script-based shapeless crafting recipe from an array of inputs.
	 * @param diskette The Diskette to use, obtained via `diskette.importScript()`.
	 * @param inputs A list of input item or tag ids required for the recipe.
	 * @param output The output of the recipe.
	 * @param group The recipe group to go in, or "" for none.
	 */
	public void addShapeless(Diskette diskette, Object[] inputs, Object output, String group) {
		try {
			ItemStack stack = RecipeParser.processItemStack(output);
			Identifier recipeId = getRecipeId(stack, diskette);
			DefaultedList<Ingredient> ingredients = DefaultedList.of();
			for (int i = 0; i < Math.min(inputs.length, 9); i++) {
				Object id = inputs[i];
				if (id.equals("")) continue;
				ingredients.add(i, RecipeParser.processIngredient(id));
			}
			addRecipe(new CustomShapelessRecipe(diskette, recipeId, group, ingredients, stack));
		} catch (Exception e) {
			logger.error("Error custom parsing shapeless recipe - " + e.getMessage());
		}
	}

	/**
	 * Register a fully custom, dynamic, and script-based crafting recipe.
	 * @param diskette The Diskette to use, obtained via `diskette.importScript()`.
	 */
	public void addSpecialCrafting(Diskette diskette) {
		triedRecipeCount++;
		Identifier recipeId = new Identifier(currentNamespace, diskette.getId().getPath() + "/special-" + triedRecipeCount);
		addRecipe(new CustomSpecialCraftingRecipe(diskette, recipeId));
	}

	public Logger getLogger() {
		return logger;
	}

	@Override
	public JsonObject getDebugInfo() {
		return recipeDebug;
	}
}
