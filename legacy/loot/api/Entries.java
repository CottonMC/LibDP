package io.github.cottonmc.libdp.api.driver.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Entries {
	public static final Entries INSTANCE = new Entries();

	private Entries() {}

	/**
	 * Create an item entry.
	 * @param name The ID of the item to drop.
	 * @return A modifiable form of an entry for this item.
	 */
	public MutableLootEntry item(String name) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "minecraft:item");
		json.addProperty("name", name);
		return new MutableLootEntry(json);
	}

	/**
	 * Create a tag entry.
	 * @param name The ID of the tag to drop from.
	 * @return A modifiable form of an entry for this tag.
	 */
	public MutableLootEntry tag(String name) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "minecraft:tag");
		json.addProperty("name", name);
		return new MutableLootEntry(json);
	}

	/**
	 * Create a defaulted tag entry.
	 * @param name The ID of the tag to get the default drop from.
	 * @return A modifiable form of an entry for this defaulted tag
	 */
	public MutableLootEntry defaultedTag(String name) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "libcd:defaulted_tag");
		json.addProperty("name", name);
		return new MutableLootEntry(json);
	}

	/**
	 * Create a table entry.
	 * @param name The ID of the loot table to drop from.
	 * @return A modifiable form of an entry for this table.
	 */
	public MutableLootEntry table(String name) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "minecraft:loot_table");
		json.addProperty("name", name);
		return new MutableLootEntry(json);
	}

	/**
	 * Create a dynamic entry.
	 * @param name The ID of the formula to use to drop.
	 * @return A modifiable form of an entry for this formula.
	 */
	public MutableLootEntry dynamic(String name) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "minecraft:dynamic");
		json.addProperty("name", name);
		return new MutableLootEntry(json);
	}

	/**
	 * Create a combined entry.
	 * @param type The name of the combination format.
	 * @param children The entries to combine.
	 * @return A modifiable form of an entry for this combination.
	 */
	public MutableLootEntry combined(String type, MutableLootEntry... children) {
		JsonObject json = new JsonObject();
		json.addProperty("type", type);
		JsonArray childArray = new JsonArray();
		for (MutableLootEntry child : children) {
			childArray.add(child.getJson());
		}
		json.add("children", childArray);
		return new MutableLootEntry(json);
	}
}
