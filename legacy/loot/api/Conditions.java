package io.github.cottonmc.libdp.api.driver.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cottonmc.libdp.api.util.Gsons;
import io.github.cottonmc.libdp.mixin.ReferenceLootConditionAccessor;
import net.minecraft.loot.condition.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;

public class Conditions {
	public static final Conditions INSTANCE = new Conditions();

	private Conditions() {}

	/**
	 * Parse Stringified JSON into a special loot condition. Useful for complex or third-party conditions.
	 * @param json Stringified JSON of the condition to add.
	 * @return The parsed condition, ready to add to a pool or entry.
	 */
	public LootCondition parse(String json) {
		try {
			return Gsons.LOOT_TABLE.fromJson(json, LootCondition.class);
		} catch (JsonSyntaxException e) {
			LootDriver.INSTANCE.getLogger().error("Could not parse loot condition, returning null: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Assemble a group condition which will return true if any child condition is met.
	 * @param conditions The conditions to test.
	 * @return An assembled condition, ready to add to a pool or entry.
	 */
	public LootCondition or(LootCondition... conditions) {
		JsonObject json = new JsonObject();
		json.addProperty("condition", "minecraft:alternative");
		JsonArray children = new JsonArray();
		for (LootCondition condition : conditions) {
			if (condition == null) {
				LootDriver.INSTANCE.getLogger().error("Loot table `or` condition cannot take null condition, skipping");
				continue;
			}
			String cond = Gsons.LOOT_TABLE.toJson(condition);
			children.add(Gsons.PARSER.parse(cond));
		}
		json.add("terms", children);
		return Gsons.LOOT_TABLE.fromJson(json, LootCondition.class);
	}

	/**
	 * Create a loot condition that's an inversion of another condition.
	 * @param condition The condition to invert.
	 * @return An assembled condition, ready to add to a pool or entry.
	 */
	public LootCondition not(LootCondition condition) {
		if (condition == null) {
			LootDriver.INSTANCE.getLogger().error("Loot table `not` condition cannot take null condition, returning null");
			return null;
		}
		String json = Gsons.LOOT_TABLE.toJson(condition);
		JsonObject cond = new JsonObject();
		cond.addProperty("condition", "minecraft:inverted");
		cond.add("term", Gsons.PARSER.parse(json));
		return Gsons.LOOT_TABLE.fromJson(cond, LootCondition.class);
	}

	/**
	 * Check whether an entity was killed by a player.
	 * @return An assembled condition, ready to add to a pool or entry.
	 */
	public LootCondition killedByPlayer() {
		return KilledByPlayerLootCondition.builder().build();
	}

	/**
	 * A random chance to drop.
	 * @param chance The chance, as a percentage.
	 * @return An assembled condition, ready to add to a pool or entry.
	 */
	public LootCondition chance(float chance) {
		return RandomChanceLootCondition.builder(chance).build();
	}

	/**
	 * A random chance to drop, affected by looting.
	 * @param chance The base chance, as a percentage.
	 * @param multiplier The multiplier for each level of looting on a weapon.
	 * @return An assembled condition, ready to add to a pool or entry.
	 */
	public LootCondition chanceWithLooting(float chance, float multiplier) {
		return RandomChanceWithLootingLootCondition.builder(chance, multiplier).build();
	}

	/**
	 * Check whether a block survives a creeper explosion.
	 * @return An assembled condition, ready to add to a pool or entry.
	 */
	public LootCondition survivesExplosion() {
		return SurvivesExplosionLootCondition.builder().build();
	}

	/**
	 * Check whether a given tool will cause a drop.
	 * @param item An item or tag ID to test. Tag ID must be prepended with `#`.
	 * @param nbt Any NBT required for the tool to have.
	 * @return An assembled condition, ready to add to a pool or entry.
	 */
	//TODO: enchantment
	public LootCondition matchTool(String item, String nbt) {
		ItemPredicate.Builder builder = ItemPredicate.Builder.create();
		if (item.indexOf('#') == 0) {
			Identifier id = new Identifier(item.substring(1));
			builder.tag(ServerTagManagerHolder.getTagManager().getItems().getTag(id));
		} else {
			Identifier id = new Identifier(item);
			builder.item(Registry.ITEM.get(id));
		}
		if (!nbt.equals("")) {
			try {
				CompoundTag tag = StringNbtReader.parse(nbt);
				builder.nbt(tag);
			} catch (CommandSyntaxException e) {

			}

		}
		return MatchToolLootCondition.builder(builder).build();
	}

	/**
	 * Use an enchantment on a tool to determine whether something should drop.
	 * @param enchantment The enchantment to test with.
	 * @param chances The float percentage chance that this will drop for each level of the enchantment.
	 * @return An assembled condition, ready to add to a pool or entry.
	 */
	public LootCondition enchantBonus(String enchantment, float[] chances) {
		return TableBonusLootCondition.builder(Registry.ENCHANTMENT.get(new Identifier(enchantment)), chances).build();
	}

	/**
	 * Use the weather to determine whether something should drop.
	 * @param raining The state of rain required to drop (use null to ignore).
	 * @param thundering The state of thunder required to drop (use null to ignore).
	 * @return An assembled condition, ready to add to a pool or entry.
	 */
	public LootCondition weather(@Nullable Boolean raining, @Nullable Boolean thundering) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "minecraft:weather_check");
		if (raining != null) json.addProperty("raining", raining);
		if (thundering != null) json.addProperty("thundering", thundering);
		return Gsons.LOOT_TABLE.fromJson(json, LootCondition.class);
	}

	/**
	 * Use a predicate JSON as a condition.
	 * @param id The ID of the predicate JSON to use.
	 * @return An assembled condition, ready to add to a pool or entry.
	 */
	public LootCondition predicate(String id) {
		return ReferenceLootConditionAccessor.callConstructor(new Identifier(id));
	}

}
