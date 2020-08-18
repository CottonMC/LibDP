package io.github.cottonmc.libdp.api.driver;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.github.cottonmc.libdp.api.Diskette;

public class DriverManager {
	public static final DriverManager INSTANCE = new DriverManager();

	private DriverManager() {}

	private final List<Driver> drivers = new ArrayList<>();
	private final Map<String, Function<Diskette, Object>> assistants = new HashMap<>();
	private final Map<Driver, String> driverNames = new HashMap<>();
	private final Map<Identifier, StackFactory> factories = new HashMap<>();

	/**
	 * Add a new tweaker to store data in.
	 * @param name A name to pass to `libdp.require`. Names shared with addAssistant(Factory). Namespace with package notation, ex. `libdp.util.DriverUtils`
	 * @param driver An instanceof Driver to call whenever reloading.
	 */
	public void addDriver(String name, Driver driver) {
		drivers.add(driver);
		driverNames.put(driver, name);
		assistants.put(name, (id) -> {
			driver.prepareFor(id);
			return driver;
		});
	}

	/**
	 * Add a new assistant class for tweakers to access through `diskette.require`.
	 * DO NOT PASS TWEAKER INSTANCES HERE. They are automatically added in addDriver.
	 * @param name A name to pass to `diskette.require`. Names shared with addDriver and addAssistantFactory. Namespace with package notation, ex. `libdp.util.DriverUtils`
	 * @param assistant An object of a class to use in scripts.
	 */
	public void addAssistant(String name, Object assistant) {
		assistants.put(name, id -> assistant);
	}

	/**
	 * Add a factory for assistants which have methods affected by script ID.
	 * @param name A name to pass to `diskette.require`. Names shared with addDriver and addAssistant. Namespace with package notation, ex. `libdp.util.DriverUtils`
	 * @param factory A function that takes an identifier and returns an object of a class to use in scripts.
	 * For deprecation purposes, the final part of the package-notated name will be passed directly to the script on its own.
	 */
	public void addAssistantFactory(String name, Function<Diskette, Object> factory) {
		assistants.put(name, factory);
	}

	/**
	 * Add a factory for special item stacks.
	 * @param id The ID to register the factory at.
	 * @param factory The stack factory used.
	 */
	public void addStackFactory(Identifier id, StackFactory factory) {
		factories.put(id, factory);
	}

	public List<Driver> getDrivers() {
		return drivers;
	}

	public Object getAssistant(String name, Diskette scriptFrom) {
		return assistants.get(name).apply(scriptFrom);
	}

	public Map<Identifier, StackFactory> getStackFactories() {
		return factories;
	}

	public String getDriverName(Driver driver) {
		return driverNames.get(driver);
	}
}
