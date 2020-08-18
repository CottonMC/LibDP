package io.github.cottonmc.libdp.api.driver.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.cottonmc.libdp.api.util.Gsons;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.JsonHelper;

public class MutableLootEntry {
	private JsonObject entryJson;

	public MutableLootEntry(LootPoolEntry entry) {
		this((JsonObject)Gsons.PARSER.parse(Gsons.LOOT_TABLE.toJson(entry)));
	}

	public MutableLootEntry(JsonObject json) {
		this.entryJson = json;
	}

	/**
	 * Set the type of loot entry.
	 * @param type The type of entry to set.
	 * @return This entry with the type set.
	 */
	public MutableLootEntry type(String type) {
		entryJson.addProperty("type", type);
		return this;
	}

	/**
	 * Set the name of the loot entry. Used to determine what it drops.
	 * @param name The name - typically an item, tag, or loot table ID.
	 * @return This entry with the name set.
	 */
	public MutableLootEntry name(String name) {
		entryJson.addProperty("name", name);
		return this;
	}

	/**
	 * Set the weight of the loot entry.
	 * @param weight The weight to set. Must be positive.
	 * @return This entry with the weight set.
	 */
	public MutableLootEntry weight(int weight) {
		entryJson.addProperty("weight", weight);
		return this;
	}

	/**
	 * Set the weight of the loot entry. Used with luck/unluck status effects and Luck of the Sea.
	 * @param quality The quality to set. Can be positive or negative.
	 * @return This entry with the quality set.
	 */
	public MutableLootEntry quality(int quality) {
		entryJson.addProperty("quality", quality);
		return this;
	}

	/**
	 * Add conditions to the loot entry that must be met before this can drop.
	 * @param conditions A list of conditions to meet before this can drop, each constructed in {@link Conditions} (available through `libcd.require("libcd.loot.Conditions")`)
	 * @return This entry with the conditions added.
	 */
	public MutableLootEntry addConditions(LootCondition... conditions) {
		for (LootCondition condition : conditions) {
			if (condition == null) {
				LootDriver.INSTANCE.getLogger().error("Loot entry cannot take null condition, skipping");
			}
			getConditions().add(Gsons.PARSER.parse(Gsons.LOOT_TABLE.toJson(condition)));
		}
		return this;
	}

	/**
	 * Add functions to the loot entry that are applied to whatever drops.
	 * @param functions A list of functions to apply to this entry, each constructed in {@link Functions} (available through `libcd.require("libcd.loot.Functions")`)
	 * @return This entry with the functions added.
	 */
	public MutableLootEntry addFunctions(LootFunction... functions) {
		for (LootFunction function : functions) {
			if (function == null) {
				LootDriver.INSTANCE.getLogger().error("Loot entry cannot take null function, skipping");
				continue;
			}
			getFunctions().add(Gsons.PARSER.parse(Gsons.LOOT_TABLE.toJson(function)));
		}
		return this;
	}

	/**
	 * Add a number property to the entry.
	 * @param name The name of this property.
	 * @param value The value this property should hold.
	 * @return This entry with the property added.
	 */
	public MutableLootEntry property(String name, Number value) {
		entryJson.addProperty(name, value);
		return this;
	}

	/**
	 * Add a number boolean to the entry.
	 * @param name The name of this property.
	 * @param value The value this property should hold.
	 * @return This entry with the property added.
	 */
	public MutableLootEntry property(String name, Boolean value) {
		entryJson.addProperty(name, value);
		return this;
	}

	/**
	 * Add a string property to the entry.
	 * @param name The name of this property.
	 * @param value The value this property should hold.
	 * @return This entry with the property added.
	 */
	public MutableLootEntry property(String name, String value) {
		entryJson.addProperty(name, value);
		return this;
	}

	/**
	 * Add a JSON element to the entry.
	 * @param name The name of this element.
	 * @param value The value this element should hold, as stringified JSON.
	 * @return This entry with the element added.
	 */
	public MutableLootEntry element(String name, String value) {
		entryJson.add(name, Gsons.PARSER.parse(value));
		return this;
	}

	private JsonArray getConditions() {
		if (!entryJson.has("conditions")) {
			entryJson.add("conditions", new JsonArray());
		}
		return JsonHelper.getArray(entryJson, "conditions", new JsonArray());
	}

	private JsonArray getFunctions() {
		if (!entryJson.has("functions")) {
			entryJson.add("functions", new JsonArray());
		}
		return JsonHelper.getArray(entryJson, "functions", new JsonArray());
	}

	private JsonArray getChildren() {
		if (!entryJson.has("children")) {
			entryJson.add("children", new JsonArray());
		}
		return JsonHelper.getArray(entryJson, "children", new JsonArray());
	}

	public JsonObject getJson() {
		return entryJson;
	}

	public LootPoolEntry get() {
		return Gsons.LOOT_TABLE.fromJson(entryJson, LootPoolEntry.class);
	}
}
