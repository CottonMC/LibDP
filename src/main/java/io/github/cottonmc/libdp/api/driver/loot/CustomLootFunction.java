package io.github.cottonmc.libdp.api.driver.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.cottonmc.libdp.LibDP;
import io.github.cottonmc.libdp.api.DPSyntaxError;
import io.github.cottonmc.libdp.api.Diskette;
import io.github.cottonmc.libdp.api.driver.recipe.RecipeParser;
import io.github.cottonmc.libdp.api.util.MutableStack;
import io.github.cottonmc.libdp.api.util.WrappedLootContext;
import io.github.cottonmc.libdp.loader.DisketteLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class CustomLootFunction implements LootFunction {
	private final Identifier disketteId;

	public CustomLootFunction(Identifier disketteId) {
		this.disketteId = disketteId;
	}

	@Override
	public LootFunctionType getType() {
		return LibDP.CUSTOM_FUNCTION;
	}

	@Override
	public ItemStack apply(ItemStack stack, LootContext context) {
		Diskette diskette = DisketteLoader.DISKETTES.get(disketteId);
		Object result = diskette.invokeFunction("apply", new MutableStack(stack), new WrappedLootContext(context));
		try {
			return RecipeParser.processItemStack(result);
		} catch (DPSyntaxError e) {
			LibDP.LOGGER.error("Could not apply custom loot function {}, returning original stack: function 'test' must return an item stack, but returned {} instead", disketteId, result.getClass().getName());

			return stack;
		}
	}

	public static class Serializer implements JsonSerializer<CustomLootFunction> {
		public static final Serializer INSTANCE = new CustomLootFunction.Serializer();

		private Serializer() { }

		@Override
		public void toJson(JsonObject json, CustomLootFunction object, JsonSerializationContext context) {
			json.addProperty("name", object.disketteId.toString());
		}

		@Override
		public CustomLootFunction fromJson(JsonObject json, JsonDeserializationContext context) {
			Identifier id = new Identifier(JsonHelper.getString(json, "name"));
			return new CustomLootFunction(id);
		}
	}
}
