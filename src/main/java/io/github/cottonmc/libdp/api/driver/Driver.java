package io.github.cottonmc.libdp.api.driver;

import net.minecraft.resource.ResourceManager;

import java.util.concurrent.Executor;

import com.google.gson.JsonObject;
import io.github.cottonmc.libdp.api.Diskette;

public interface Driver {
	/**
	 * Called whenever the /reload command is run, before scripts are run.
	 * Use this time to empty out any lists or maps you need to.
	 * @param manager The ResourceManager reloading tweakers.
	 */
	void prepareReload(ResourceManager manager);

	/**
	 * Called whenever the /reload command is run, after scripts are run.
	 * Use this time to apply whatever you need to.
	 * @param manager The ResourceManager applying tweakers. Should be the same one called in prepareReload.
	 * @param executor The Executor applying tweakers.
	 */
	void applyReload(ResourceManager manager, Executor executor);

	/**
	 * Called after all scripts have been run, to log what tweakers have been applied.
	 * @return The number of applied tweaks and the description of what type of tweak it is, ex. "12 recipes"
	 */
	String getApplyMessage();

	/**
	 * Prepare anything needed based on the script, like namespaces or other information. Called before each script is run.
	 * @param bridge The bridge provided for this script, including info like the namespace, script engine, and script text.
	 */
	default void prepareFor(Diskette bridge) {}

	/**
	 * @return A JsonObject containing information useful for debugging. Called when `/libcd debug export` is run.
	 */
	JsonObject getDebugInfo();
}
