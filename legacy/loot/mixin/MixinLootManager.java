package io.github.cottonmc.libdp.mixin;

import io.github.cottonmc.libdp.impl.LootTableMapAccessor;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(LootManager.class)
public class MixinLootManager implements LootTableMapAccessor {
	@Shadow private Map<Identifier, LootTable> tables;

	@Shadow @Final private LootConditionManager conditionManager;

	@Override
	public Map<Identifier, LootTable> libdp$getLootTableMap() {
		return tables;
	}

	@Override
	public void libdp$setLootTableMap(Map<Identifier, LootTable> map) {
		this.tables = map;
	}

	@Override
	public LootConditionManager libdp$getConditionManager() {
		return conditionManager;
	}
}
