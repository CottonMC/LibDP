package io.github.cottonmc.libdp.api;

import io.github.cottonmc.libdp.LibDP;
import io.github.cottonmc.libdp.api.driver.Driver;
import io.github.cottonmc.libdp.api.driver.DriverManager;
import io.github.cottonmc.libdp.loader.DisketteLoader;
import io.github.cottonmc.parchment.api.InvocableScript;
import io.github.cottonmc.parchment.api.Script;
import org.apache.logging.log4j.LogManager;

import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

/**
 * A bridge for specific LibCD hooks between Java and JSR-223 languages. 
 * An instance is provided to every script as `diskette`.
 * Contains information for other extension systems, such as the script engine, the text of the script, 
 * and the script's ID.
 */
public class Diskette implements Script, InvocableScript {
	
	private final ScriptEngine engine;
	private final Identifier id;
	private final String contents;
	private boolean hasRun = false;
	private boolean hadError = false;
	private final List<Driver> requiredDrivers = new ArrayList<>();

	public Diskette(ScriptEngine engine, Identifier id, String contents) {
		this.engine = engine;
		this.id = id;
		this.contents = contents;
	}

	/**
	 * Import an assistant object for this diskette to use.
	 * @param assistant The name of the assistant to get.
	 * @return The assistant object with the given name, prepared for this script.
	 */
	public Object require(String assistant) {
		Object ret = DriverManager.INSTANCE.getAssistant(assistant, this);
		//if it's a driver, note that so we can re-prep if `importDiskette` gets run
		if (ret instanceof Driver) {
			requiredDrivers.add((Driver)ret);
		}
		return ret;
	}

	/**
	 * Import another script to get variables from or invoke a function from.
	 * Will evaluate the imported script if it hasn't been eval'd yet.
	 * @param scriptId The ID of the script to import.
	 * @return The script bridge of the imported script, or null if the script doesn't exist.
	 */
	@Nullable
	public Diskette importDiskette(String scriptId) {
		Identifier id = new Identifier(scriptId);
		if (!DisketteLoader.DISKETTES.containsKey(id)) {
			LibDP.LOGGER.error("Diskette {} could not find other diskette {}", this.id.toString(), id.toString());
			return null;
		}
		Diskette bridge = DisketteLoader.DISKETTES.get(id);
		if (!bridge.hasRun()) bridge.run();
		//now that the other script has run, re-prep any tweakers we've required before, since the other script mighta messed them up
		for (Driver driver : requiredDrivers) {
			driver.prepareFor(this);
		}
		return bridge;
	}

	@Override
	public ScriptEngine getEngine() {
		return engine;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public String getContents() {
		return contents;
	}

	@Deprecated
	public String getScriptText() {
		return getContents();
	}

	@Override
	@Nullable
	public Object getVar(String varName) {
		if (!hasRun()) run();
		if (hadError()) {
			LibDP.LOGGER.error("Cannot get variable from errored diskette {}, returning null",
					id.toString());
			return null;
		}
		return engine.getBindings(ScriptContext.ENGINE_SCOPE).get(varName);
	}

	@Override
	@Nullable
	public Object invokeFunction(String funcName, Object...args) {
		if (!hasRun()) run();
		if (hadError()) {
			LibDP.LOGGER.error("Cannot invoke function from errored diskette {}, returning null",
					id.toString());
			return null;
		}
		if (engine instanceof Invocable) {
			Invocable invocable = (Invocable)engine;
			try {
				return invocable.invokeFunction(funcName, args);
			} catch (Exception e) {
				LibDP.LOGGER.error("Error invoking function {} from diskette {}: {}", funcName,
						id.toString(), e.getMessage());
				return null;
			}
		} else {
			LibDP.LOGGER.error("Cannot invoke functions from diskette {}: engine is not invocable",
					id.toString());
			return null;
		}
	}

	@Override
	public void run() {
		if (hasRun()) return;
		ScriptContext ctx = engine.getContext();
		ctx.setAttribute("diskette", this, ScriptContext.ENGINE_SCOPE);
		ctx.setAttribute("log", LogManager.getLogger(id.toString()), ScriptContext.ENGINE_SCOPE);
		try {
			engine.eval(contents);
		} catch (ScriptException e) {
			hadError = true;
			LibDP.LOGGER.error("Error playing diskette {}: {}", id.toString(), e.getMessage());
		}
		hasRun = true;
	}

	public boolean hasRun() {
		return hasRun;
	}

	@Override
	public boolean hadError() {
		return hadError;
	}
}
