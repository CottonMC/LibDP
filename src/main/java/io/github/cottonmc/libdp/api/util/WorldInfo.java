package io.github.cottonmc.libdp.api.util;

import net.minecraft.world.World;

/**
 * A wrapped view of a world, accessible outside of obfuscation.
 */
public class WorldInfo {
	private World world;

	public WorldInfo(World world) {
		this.world = world;
	}

	/**
	 * @return Whether it's currently daytime or not.
	 */
	public boolean isDay() {
		return world.isDay();
	}

	/**
	 * @return The current time of day - from 0 to 23000.
	 */
	public long getTime() {
		return world.getTimeOfDay();
	}

	/**
	 * @return Whether the world itself is currently raining.
	 */
	public boolean isRaining() {
		return world.isRaining();
	}

	/**
	 * @return Whether the world itself is currently thundering.
	 */
	public boolean isThundering() {
		return world.isThundering();
	}

	/**
	 * @return The int value of the global difficulty of the world.
	 */
	public int getDifficulty() {
		return world.getLevelProperties().getDifficulty().getId();
	}
}
