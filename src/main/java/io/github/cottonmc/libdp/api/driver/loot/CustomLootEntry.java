package io.github.cottonmc.libdp.api.driver.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import io.github.cottonmc.libdp.LibDP;
import io.github.cottonmc.libdp.api.DPSyntaxError;
import io.github.cottonmc.libdp.api.Diskette;
import io.github.cottonmc.libdp.api.driver.recipe.RecipeParser;
import io.github.cottonmc.libdp.api.util.WrappedLootContext;
import io.github.cottonmc.libdp.loader.DisketteLoader;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CustomLootEntry extends LeafEntry {
	private final Identifier disketteId;

	public CustomLootEntry(Identifier diskette, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
		super(weight, quality, conditions, functions);
		this.disketteId = diskette;
	}

	@Override
	protected void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
		Diskette diskette = DisketteLoader.DISKETTES.get(disketteId);
		Object result = diskette.invokeFunction("generateLoot", new WrappedLootContext(context));
		List<ItemStack> generated = new ArrayList<>();
		if (result instanceof Object[]) {
			for (Object obj : (Object[]) result) {
				try {
					generated.add(RecipeParser.processItemStack(obj));
				} catch (DPSyntaxError e) {
					LibDP.LOGGER.error("Cannot parse item stack from loot generator in {}: {}", disketteId, e.getMessage());
				}
			}
		} else if (result instanceof Iterable) {
			for (Object obj : (Iterable) result) {
				try {
					generated.add(RecipeParser.processItemStack(obj));
				} catch (DPSyntaxError e) {
					LibDP.LOGGER.error("Cannot parse item stack from loot generator in {}: {}", disketteId, e.getMessage());
				}
			}
			//uuuuuggggghhhhh fuck you nashorn why do I have to hardcode this
			//TODO: add a sanitizer system to Parchment, especially because java 15 kills nashorn
		} else if (result instanceof ScriptObjectMirror && ((ScriptObjectMirror) result).isArray()) {
			for (Object obj : ((ScriptObjectMirror) result).values()) {
				try {
					generated.add(RecipeParser.processItemStack(obj));
				} catch (DPSyntaxError e) {
					LibDP.LOGGER.error("Cannot parse item stack from loot generator in {}: {}", disketteId, e.getMessage());
				}
			}
		} else {
			try {
				generated.add(RecipeParser.processItemStack(result));
			} catch (DPSyntaxError e) {
				LibDP.LOGGER.error("Cannot parse item stack from loot generator in {}: {}", disketteId, e.getMessage());
			}
		}
		for (ItemStack stack : generated) {
			lootConsumer.accept(stack);
		}
	}

	@Override
	public LootPoolEntryType getType() {
		return LibDP.CUSTOM_ENTRY;
	}

	public static class Serializer extends LeafEntry.Serializer<CustomLootEntry> {
		public static final Serializer INSTANCE = new CustomLootEntry.Serializer();

		private Serializer() { }

		@Override
		protected CustomLootEntry fromJson(JsonObject json, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
			Identifier id = new Identifier(JsonHelper.getString(json, "name"));
			return new CustomLootEntry(id, weight, quality, conditions, functions);
		}

	}
}
