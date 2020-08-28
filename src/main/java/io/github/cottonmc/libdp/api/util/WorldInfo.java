package io.github.cottonmc.libdp.api.util;

import net.minecraft.world.World;

/**
 * A wrapped view of a world, accessible outside of obfuscation.
 */
public class WorldInfo {
	private final World world;

	public WorldInfo(World world) {
		this.world = world;
	}

	/**
	 * @return Whether it's currently daytime or not, purely by time.
	 */
	public boolean isDay() {
		return world.getTimeOfDay() >= 0 && world.getTimeOfDay() <= 12000;
	}

	/**
	 * @return Whether it's daytime and not too cloudy to let light through.
	 */
	public boolean isSunnyDay() {
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

	/**
	 * @return The dimension of the world.
	 */
	public String getDimension() {
		return world.getServer().getRegistryManager().getDimensionTypes().getId(world.getDimension()).toString();
	}
}
