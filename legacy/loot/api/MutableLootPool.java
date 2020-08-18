package io.github.cottonmc.libdp.api.driver.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.cottonmc.libdp.api.util.Gsons;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A representation of a loot pool that's modifiable from JSR-223 code.
 */
public class MutableLootPool {
    private JsonObject poolJson;

    public MutableLootPool(LootPool pool) {
        this((JsonObject)Gsons.PARSER.parse(Gsons.LOOT_TABLE.toJson(pool)));
    }

    public MutableLootPool(JsonObject json) {
        this.poolJson = json;
    }

    /**
     * Set the number of rolls this pool makes.
     * @param rolls How many rolls to make.
     * @return This pool with the rolls set.
     */
    public MutableLootPool rolls(int rolls) {
        poolJson.addProperty("rolls", rolls);
        return this;
    }

    /**
     * Set the range of rolls this pool can make.
     * @param minRolls The minimum number of rolls to make.
     * @param maxRolls The maximum number of rolls to make.
     * @return This pool with the rolls set.
     */
    public MutableLootPool rolls(int minRolls, int maxRolls) {
        JsonObject rolls = new JsonObject();
        rolls.addProperty("min", minRolls);
        rolls.addProperty("max", maxRolls);
        poolJson.add("rolls", rolls);
        return this;
    }

    /**
     * Set the number of bonus rolls this pool makes.
     * @param rolls How many bonus rolls to make.
     * @return This pool with the bonus rolls set.
     */
    public MutableLootPool bonusRolls(int rolls) {
        poolJson.addProperty("bonus_rolls", rolls);
        return this;
    }

    /**
     * Set the range of bonus rolls this pool can make.
     * @param minRolls The minimum number of bonus rolls to make.
     * @param maxRolls The maximum number of bonus rolls to make.
     * @return This pool with the bonus rolls set.
     */
    public MutableLootPool bonusRolls(int minRolls, int maxRolls) {
        JsonObject rolls = new JsonObject();
        rolls.addProperty("min", minRolls);
        rolls.addProperty("max", maxRolls);
        poolJson.add("bonus_rolls", rolls);
        return this;
    }

    /**
     * Remove an entry from the pool. Currently does not work with combined entries.
     * @param type The type of the entry.
     * @param name The name of the entry. Typically an item, tag, or loot table ID.
     * @return This pool with the entry removed.
     */
    public MutableLootPool removeEntry(String type, String name) {
        List<JsonElement> toRemove = new ArrayList<>();
        for (JsonElement elem : getEntries()) {
            if (elem instanceof JsonObject) {
                JsonObject obj = (JsonObject)elem;
                String objType = JsonHelper.getString(obj, "type", "");
                String objName = JsonHelper.getString(obj, "name", "");
                if (objType.equals(type) && objName.equals(name)) toRemove.add(elem);
            }
        }
        for (JsonElement elem : toRemove) {
            getEntries().remove(elem);
        }
        return this;
    }

    public MutableLootPool removeEntry(int index) {
        getEntries().remove(index);
        return this;
    }

    /**
     * Get an entry based on the type and name.
     * @param type The type of entry to get.
     * @param name The name of the entry to get.
     * @return The first entry that matchis this type and name, or null.
     */
    public MutableLootEntry getEntry(String type, String name) {
        for (JsonElement elem: getEntries()) {
            if (elem instanceof JsonObject) {
                JsonObject obj = (JsonObject)elem;
                String objType = JsonHelper.getString(obj, "type", "");
                String objName = JsonHelper.getString(obj, "name", "");
                if (objType.equals(type) && objName.equals(name)) return new MutableLootEntry(obj);
            }
        }
        return null;
    }

    /**
     * Get an entry from the pool's entry array.
     * @param index The index of the entry to get.
     * @return The entry at this index.
     */
    public MutableLootEntry getEntry(int index) {
        return new MutableLootEntry(getEntries().get(index).getAsJsonObject());
    }

    /**
     * Add entries to the loot pool.
     * @param entries The entries to add.
     * @return This pool with the entries added.
     */
    public MutableLootPool addEntries(MutableLootEntry... entries) {
        for (MutableLootEntry entry : entries) {
            getEntries().add(entry.getJson());
        }
        return this;
    }

    /**
     * Add a new leaf entry to the loot pool.
     * @param type The type of entry to add.
     * @param name The ID used to decide the drop.
     * @param weight The weight of this entry in the pool.
     * @param quality The quality of this entry in the pool. Used for luck/unluck status effects, along with Luck of the Sea enchantment.
     * @param functions A list of functions to apply to this entry, each constructed in {@link Functions} (available through `libcd.require("libcd.loot.Functions")`)
     * @param conditions A list of conditions to meet before this can drop, each constructed in {@link Conditions} (available through `libcd.require("libcd.loot.Conditions")`)
     * @return This pool with the entry added.
     */
    //TODO: remove?
    public MutableLootPool addLeafEntry(String type, String name, int weight, int quality, LootFunction[] functions, LootCondition[] conditions) {
        return addLeafEntry(type, name, weight, quality, functions, conditions, new JsonObject());
    }

    /**
     * Add a new leaf entry to the loot pool.
     * @param type The type of entry to add.
     * @param name The ID used to decide the drop.
     * @param weight The weight of this entry in the pool.
     * @param quality The quality of this entry in the pool. Used for luck/unluck status effects, along with Luck of the Sea enchantment.
     * @param functions A list of functions to apply to this entry, each constructed in {@link Functions} (available through `libcd.require("libcd.loot.Functions")`)
     * @param conditions A list of conditions to meet before this can drop, each constructed in {@link Conditions} (available through `libcd.require("libcd.loot.Conditions")`)
     * @param extra Any extra JSON needed for this type of entry to function, as stringified JSON.
     * @return This pool with the entry added.
     */
    //TODO: remove?
    public MutableLootPool addLeafEntry(String type, String name, int weight, int quality, LootFunction[] functions, LootCondition[] conditions, String extra) {
        return addLeafEntry(type, name, weight, quality, functions, conditions, (JsonObject) Gsons.PARSER.parse(extra));
    }

    /**
     * Add a new leaf entry to the loot pool.
     * @param type The type of entry to add.
     * @param name The ID used to decide the drop.
     * @param weight The weight of this entry in the pool.
     * @param quality The quality of this entry in the pool. Used for luck/unluck status effects, along with Luck of the Sea enchantment.
     * @param functions A list of functions to apply to this entry, each constructed in {@link Functions} (available through `libcd.require("libcd.loot.Functions")`)
     * @param conditions A list of conditions to meet before this can drop, each constructed in {@link Conditions} (available through `libcd.require("libcd.loot.Conditions")`)
     * @param extra Any extra JSON needed for this type of entry to function, as a JSON object.
     * @return This pool with the entry added.
     */
    //TODO: remove?
    public MutableLootPool addLeafEntry(String type, String name, int weight, int quality, LootFunction[] functions, LootCondition[] conditions, JsonObject extra) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", type);
        entry.addProperty("name", name);
        entry.addProperty("weight", weight);
        entry.addProperty("quality", quality);
        if (functions.length != 0) {
            JsonArray funs = new JsonArray();
            for (LootFunction fun : functions) {
                funs.add(Gsons.PARSER.parse(Gsons.LOOT_TABLE.toJson(fun)));
            }
            entry.add("functions", funs);
        }
        if (conditions.length != 0) {
            JsonArray cons = new JsonArray();
            for (LootCondition con : conditions) {
                cons.add(Gsons.PARSER.parse(Gsons.LOOT_TABLE.toJson(con)));
            }
            entry.add("conditions", cons);
        }
        for (Map.Entry<String, JsonElement> exEntry : extra.entrySet()) {
            entry.add(exEntry.getKey(), exEntry.getValue());
        }
        getEntries().add(entry);
        return this;
    }

    /**
     * Add a condition that must be met for this pool to drop anything.
     * @param conditions Condition to meet, constructed in {@link Conditions} (available through `libcd.require("libcd.loot.Conditions")`)
     * @return This pool with the conditions added.
     */
    public MutableLootPool addConditions(LootCondition... conditions) {
        for (LootCondition condition : conditions) {
            if (condition == null) {
                LootDriver.INSTANCE.getLogger().error("Loot pool cannot take null condition, ignoring");
                continue;
            }
            getConditions().add(Gsons.PARSER.parse(Gsons.LOOT_TABLE.toJson(condition)));
        }
        return this;
    }

    /**
     * Remove a condition from this pool.
     * @param index The index of the condition to remove.
     * @return This pool with the conditions removed.
     */
    public MutableLootPool removeCondition(int index) {
        getConditions().remove(index);
        return this;
    }

    private JsonArray getEntries() {
        if (!poolJson.has("entries")) {
            poolJson.add("entries", new JsonArray());
        }
        return JsonHelper.getArray(poolJson, "entries", new JsonArray());
    }

    private JsonArray getConditions() {
        if (!poolJson.has("conditions")) {
            poolJson.add("conditions", new JsonArray());
        }
        return JsonHelper.getArray(poolJson, "conditions", new JsonArray());
    }

    public LootPool get() {
        return Gsons.LOOT_TABLE.fromJson(poolJson, LootPool.class);
    }
}
