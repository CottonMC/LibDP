package io.github.cottonmc.libdp.api.driver.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.cottonmc.libdp.api.util.Gsons;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.JsonHelper;

/**
 * A representation of a loot table that's modifiable from JSR-223 code.
 */
public class MutableLootTable {
	private JsonObject tableJson;

	public MutableLootTable(LootTable table) {
		this((JsonObject)Gsons.PARSER.parse(Gsons.LOOT_TABLE.toJson(table)));
	}

	public MutableLootTable(JsonObject json) {
		this.tableJson = json;
	}

	/**
	 * Get a pool from the table's array.
	 * @param index The index of the pool to get.
	 * @return A modifiable form of that pool.
	 */
	public MutableLootPool getPool(int index) {
		return new MutableLootPool((JsonObject)getPools().get(index));
	}

	/**
	 * Add a new pool to the table.
	 * @param rolls How many rolls this pool should have.
	 * @return A modifiable form of this pool.
	 */
	public MutableLootPool addPool(int rolls) {
		JsonObject json = new JsonObject();
		json.addProperty("rolls", rolls);
		getPools().add(json);
		return new MutableLootPool(json);
	}

	/**
	 * Add a new pool to the table.
	 * @param minRolls How many rolls this pool should have at minimum.
	 * @param maxRolls How many rolls this pool should have at maximum.
	 * @return A modifiable form of this pool.
	 */
	public MutableLootPool addPool(int minRolls, int maxRolls) {
		JsonObject json = new JsonObject();
		JsonObject rolls = new JsonObject();
		rolls.addProperty("min", minRolls);
		rolls.addProperty("max", maxRolls);
		json.add("rolls", rolls);
		getPools().add(json);
		return new MutableLootPool(json);
	}

	/**
	 * Add a new pool to the table.
	 * @param minRolls How many rolls this pool should have at minimum.
	 * @param maxRolls How many rolls this pool should have at maximum.
	 * @param minBonusRolls How many bonus rolls this pool should have at minimum.
	 * @param maxBonusRolls How many bonus rolls this pool should have at maximum.
	 * @return A modifiable form of this pool.
	 */
	public MutableLootPool addPool(int minRolls, int maxRolls, int minBonusRolls, int maxBonusRolls) {
		JsonObject json = new JsonObject();
		JsonObject rolls = new JsonObject();
		JsonObject bonus = new JsonObject();
		rolls.addProperty("min", minRolls);
		rolls.addProperty("max", maxRolls);
		json.add("rolls", rolls);
		bonus.addProperty("min", minBonusRolls);
		bonus.addProperty("max", maxBonusRolls);
		json.add("bonus_rolls", rolls);
		getPools().add(json);
		return new MutableLootPool(json);
	}

	/**
	 * Remove a pool from the table array.
	 * @param index The index of the pool to remove. Will shift all other pools over.
	 * @return This table with the pool removed.
	 */
	public MutableLootTable removePool(int index) {
		getPools().remove(index);
		return this;
	}

	/**
	 * Add functions to all drops from the table.
	 * @param functions The functiosn to add, constructed in {@link Functions} (available through `libcd.require("libcd.loot.Functions")`)
	 * @return This table with the functions added.
	 */
	public MutableLootTable addFunctions(LootFunction... functions) {
		for (LootFunction function : functions) {
			if (function == null) {
				LootDriver.INSTANCE.getLogger().error("Loot table cannot take null function, ignoring");
			}
			getFunctions().add(Gsons.PARSER.parse(Gsons.LOOT_TABLE.toJson(function)));
		}
		return this;
	}

	/**
	 * Remove a function from all drops from the table.
	 * @param index The index of the function to remove.
	 * @return This table with the function removed.
	 */
	public MutableLootTable removeFunction(int index) {
		getFunctions().remove(index);
		return this;
	}

	private JsonArray getPools() {
		if (!tableJson.has("pools")) {
			tableJson.add("pools", new JsonArray());
		}
		return JsonHelper.getArray(tableJson, "pools", new JsonArray());
	}

	private JsonArray getFunctions() {
		if (!tableJson.has("functions")) {
			tableJson.add("functions", new JsonArray());
		}
		return JsonHelper.getArray(tableJson, "functions", new JsonArray());
	}

	public LootTable get() {
		return Gsons.LOOT_TABLE.fromJson(tableJson, LootTable.class);
	}
}
