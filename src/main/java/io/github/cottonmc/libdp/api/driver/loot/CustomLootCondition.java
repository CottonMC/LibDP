package io.github.cottonmc.libdp.api.driver.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.cottonmc.libdp.LibDP;
import io.github.cottonmc.libdp.api.Diskette;
import io.github.cottonmc.libdp.api.util.WrappedLootContext;
import io.github.cottonmc.libdp.loader.DisketteLoader;
import io.github.cottonmc.libdp.loader.NullDiskette;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class CustomLootCondition implements LootCondition {
	private final Identifier disketteId;

	public CustomLootCondition(Identifier disketteId) {
		this.disketteId = disketteId;
	}

	@Override
	public LootConditionType getType() {
		return LibDP.CUSTOM_CONDITION;
	}

	@Override
	public boolean test(LootContext context) {
		Diskette diskette = DisketteLoader.DISKETTES.getOrDefault(disketteId, NullDiskette.INSTANCE);
		Object result = diskette.invokeFunction("test", new WrappedLootContext(context));
		if (result instanceof Boolean) return (boolean) result;
		LibDP.LOGGER.error("Could not test custom loot condition {}, returning false: function 'test' must return a boolean, but returned {} instead", disketteId, result.getClass().getName());
		return false;
	}

	public static class Serializer implements JsonSerializer<CustomLootCondition> {
		public static final Serializer INSTANCE = new Serializer();

		private Serializer() { }

		@Override
		public void toJson(JsonObject json, CustomLootCondition object, JsonSerializationContext context) {
			json.addProperty("name", object.disketteId.toString());
		}

		@Override
		public CustomLootCondition fromJson(JsonObject json, JsonDeserializationContext context) {
			Identifier id = new Identifier(JsonHelper.getString(json, "name"));
			return new CustomLootCondition(id);
		}
	}
}
