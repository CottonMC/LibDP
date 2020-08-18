package io.github.cottonmc.libdp.api;

import io.github.cottonmc.libdp.api.driver.DriverManager;

/**
 * Initializer for loading new drivers.
 */
public interface DriverInitializer {

	/**
	 * Register drivers and assistant scripts.
	 * @param manager The driver manager to register in.
	 */
	default void init(DriverManager manager) {}
}
