package io.github.cottonmc.libdp.impl;

import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface LootTableMapAccessor {
	Map<Identifier, LootTable> libdp$getLootTableMap();
	void libdp$setLootTableMap(Map<Identifier, LootTable> map);
	LootConditionManager libdp$getConditionManager();
}
