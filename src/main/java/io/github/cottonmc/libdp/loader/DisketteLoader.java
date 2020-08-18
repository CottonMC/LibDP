package io.github.cottonmc.libdp.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.cottonmc.libdp.LibDP;
import io.github.cottonmc.libdp.api.Diskette;
import io.github.cottonmc.libdp.api.driver.Driver;
import io.github.cottonmc.libdp.api.driver.DriverManager;
import io.github.cottonmc.parchment.api.ScriptDataLoader;
import org.apache.logging.log4j.Logger;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class DisketteLoader extends ScriptDataLoader<Diskette> {
	public static Map<Identifier, Diskette> DISKETTES;

	public DisketteLoader() {
		super(Diskette::new, "diskettes");
	}

	@Override
	public CompletableFuture<Void> apply(Map<Identifier, Diskette> diskettes, ResourceManager manager,
										 Profiler profiler, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			DISKETTES = diskettes;
			for (Driver driver : DriverManager.INSTANCE.getDrivers()) {
				driver.prepareReload(manager);
			}
			int loaded = 0;
			for (Identifier id : diskettes.keySet()) {
				Diskette script = diskettes.get(id);
				if (!script.hasRun()) script.run();
				if (!script.hadError()) loaded++;
			}
			List<String> applied = new ArrayList<>();
			for (Driver driver : DriverManager.INSTANCE.getDrivers()) {
				driver.applyReload(manager, executor);
				applied.add(driver.getApplyMessage());
			}
			String confirm = formatApplied(applied);
			if (loaded > 0) getLogger().info("Applied {} {}, including {}", loaded, (loaded == 1? "diskette" :
					"diskettes"), confirm);
		});
	}

	private String formatApplied(List<String> messages) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < messages.size(); i++) {
			String message = messages.get(i);
			ret.append(message);
			if (i < messages.size() - 1) {
				if (messages.size() <= 2) ret.append(" ");
				else ret.append(", ");
			}
			if (i == messages.size() - 2) ret.append("and ");
		}
		return ret.toString();
	}

	public static JsonObject getDebugObject() {
		JsonObject ret = new JsonObject();
		JsonArray successful = new JsonArray();
		JsonArray errored = new JsonArray();
		for (Identifier id : DISKETTES.keySet()) {
			Diskette diskette = DISKETTES.get(id);
			if (diskette.hadError()) {
				errored.add(id.toString());
			} else {
				successful.add(id.toString());
			}
		}
		ret.add("successful", successful);
		ret.add("errored", errored);
		return ret;
	}

	@Override
	public Identifier getFabricId() {
		return new Identifier(LibDP.MODID, "diskette_loader");
	}

	@Override
	public Logger getLogger() {
		return LibDP.LOGGER;
	}
}
