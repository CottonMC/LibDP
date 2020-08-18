package io.github.cottonmc.libdp.api.driver.loot;

import com.google.gson.JsonObject;
import io.github.cottonmc.libdp.api.driver.Driver;
import io.github.cottonmc.libdp.api.Diskette;
import io.github.cottonmc.libdp.impl.LootTableMapAccessor;
import io.github.cottonmc.libdp.impl.ReloadListenersAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * The driver for loot tables. Legacy.
 */
public class LootDriver implements Driver {
	public static final LootDriver INSTANCE = new LootDriver();
	private LootManager lootManager;
	private int tableCount;
	private final Map<Identifier, MutableLootTable> tables = new HashMap<>();
	private Logger logger;
	private JsonObject tableDebug;

	private LootDriver() {}

	@Override
	public void prepareReload(ResourceManager manager) {
		tableDebug = new JsonObject();
		tables.clear();
		tableCount = 0;
		if (manager instanceof ReloadListenersAccessor) {
			List<ResourceReloadListener> listeners = ((ReloadListenersAccessor)manager).libdp$getListeners();
			for (ResourceReloadListener listener : listeners) {
				if (listener instanceof LootManager) {
					this.lootManager = (LootManager)listener;
					return;
				}
			}
			logger.error("No loot manager was found! Tweaker cannot edit loot tables!");
			throw new IllegalStateException("No loot manager was found! Tweaker cannot edit loot tables!");
		}
		logger.error("No reload listeners accessor found! Tweaker cannot edit loot tables!");
		throw new IllegalStateException("No reload listeners accessor found! Tweaker cannot edit loot tables!");
	}

	@Override
	public void applyReload(ResourceManager manager, Executor executor) {
		Map<Identifier, LootTable> tableMap = new HashMap<>(((LootTableMapAccessor)lootManager).libdp$getLootTableMap());
		Map<Identifier, LootTable> toAdd = new HashMap<>();
		for (Identifier id : tables.keySet()) {
			toAdd.put(id, tables.get(id).get());
		}
		if (toAdd.containsKey(LootTables.EMPTY)) {
			toAdd.remove(LootTables.EMPTY);
			logger.error("Tried to redefine empty loot table, ignoring");
		}
		LootTableReporter reporter = new LootTableReporter(LootContextTypes.GENERIC, ((LootTableMapAccessor)lootManager).libdp$getConditionManager()::get, toAdd::get);
		toAdd.forEach((id, table) -> validate(reporter, id, table));
		reporter.getMessages().forEach((context, message) -> {
			logger.error("Found validation problem in modified table {}: {}", context, message);
			Identifier id = new Identifier(context.substring(1, context.indexOf('}')));
			toAdd.remove(id);
		});
		tableCount = toAdd.size();
		tableMap.putAll(toAdd);
		((LootTableMapAccessor)lootManager).libdp$setLootTableMap(tableMap);
	}

	private void validate(LootTableReporter reporter, Identifier id, LootTable table) {
		table.validate(reporter.withContextType(table.getType()).withTable("{" + id + "}", id));
	}

	@Override
	public String getApplyMessage() {
		return tableCount + " modified loot " + (tableCount == 1? "table" : "tables");
	}

	@Override
	public void prepareFor(Diskette diskette) {
		this.logger = LogManager.getLogger(diskette.getId().getNamespace());
	}

	/**
	 * Get a new loot table, or create one if it doesn't yet exist.
	 * @param id The ID of the table to get or create.
	 * @return A modifiable form of that table.
	 */
	public MutableLootTable getTable(String id) {
		Identifier tableId = new Identifier(id);
		if (tables.containsKey(tableId)) {
			return tables.get(tableId);
		} else {
			LootTable table = lootManager.getTable(tableId);
			MutableLootTable mutable = new MutableLootTable(table);
			tables.put(tableId, mutable);
			return mutable;
		}
	}

	@Override
	public JsonObject getDebugInfo() {
		return tableDebug;
	}

	public Logger getLogger() {
		return logger;
	}
}
