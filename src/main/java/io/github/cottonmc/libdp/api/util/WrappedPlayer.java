package io.github.cottonmc.libdp.api.util;

import io.github.cottonmc.libdp.api.util.crafting.CraftingDamageSource;
import net.minecraft.entity.player.PlayerEntity;

/**
 * A wrapper on player entities so they can be manipulated outside of obfuscation
 */
public class WrappedPlayer {
	private final PlayerEntity player;

	public WrappedPlayer(PlayerEntity player) {
		this.player = player;
	}

	/**
	 * @return Whether the player is real or not.
	 */
	public boolean exists() {
		return true;
	}

	/**
	 * @return The current health of the player.
	 */
	public float getHealth() {
		return player.getHealth();
	}

	/**
	 * @return The current max health of the player.
	 */
	public float getMaxHealth() {
		return player.getMaxHealth();
	}

	/**
	 * @return The player's absorption.
	 */
	public float getAbsorption() {
		return player.getAbsorptionAmount();
	}

	/**
	 * @return The player's armor level.
	 */
	public int getArmor() {
		return player.getArmor();
	}

	/**
	 * @return The current food level of the player.
	 */
	public int getFood() {
		return player.getHungerManager().getFoodLevel();
	}

	/**
	 * @return The current saturation level of the player.
	 */
	public float getSaturation() {
		return player.getHungerManager().getSaturationLevel();
	}

	/**
	 * @return The current food level plus saturation level of the player.
	 */
	public float getTotalHunger() {
		return player.getHungerManager().getFoodLevel() + player.getHungerManager().getSaturationLevel();
	}

	/**
	 * @return The current experience level of the player.
	 */
	public int getLevel() {
		return player.experienceLevel;
	}

	/**
	 * @return Whether the player is currently wet for Riptide purposes - in water or rain.
	 */
	public boolean isWet() {
		return player.isTouchingWaterOrRain();
	}

	/**
	 * @return The player's luck.
	 */
	public float getLuck() {
		return player.getLuck();
	}

	/**
	 * @return The player's score.
	 */
	public int getScore() {
		return player.getScore();
	}

	/**
	 * @return Whether the player is in creative mode.
	 */
	public boolean isCreative() {
		return player.isCreative();
	}

	/**
	 * @return Whether the player is both in creative mode and an op (if they can use command blocks).
	 */
	public boolean isCreativeOp() {
		return player.isCreativeLevelTwoOp();
	}

	/**
	 * Damage the player.
	 * @param amount The amount of hearts to take.
	 */
	public boolean damage(float amount) {
		if (!player.world.isClient) return player.damage(CraftingDamageSource.INSTANCE, amount);
		return false;
	}

	/**
	 * Take food levels or saturation from the player.
	 * @param amount The number of levels to take.
	 */
	public void takeFood(int amount) {
		if (!player.world.isClient) player.getHungerManager().addExhaustion(amount * 10);
	}

	/**
	 * Take experience levels from the player.
	 * @param amount The amount of levels to take.
	 */
	public void takeLevels(int amount) {
		if (!player.world.isClient) player.experienceLevel -= amount;
	}
}
