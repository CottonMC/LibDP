package io.github.cottonmc.libdp.api.util;

//TODO: zeros or default values?
public class DummyPlayer extends WrappedPlayer {
	public static final DummyPlayer INSTANCE = new DummyPlayer();

	private DummyPlayer() {
		super(null);
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public float getHealth() {
		return 0;
	}

	@Override
	public float getMaxHealth() {
		return 0;
	}

	@Override
	public float getAbsorption() {
		return 0;
	}

	@Override
	public int getArmor() {
		return 0;
	}

	@Override
	public int getFood() {
		return 0;
	}

	public float getSaturation() {
		return 0;
	}

	@Override
	public float getTotalHunger() {
		return 0;
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	public boolean isWet() {
		return false;
	}

	@Override
	public float getLuck() {
		return 0;
	}

	@Override
	public int getScore() {
		return 0;
	}

	@Override
	public boolean isCreative() {
		return false;
	}

	@Override
	public boolean isCreativeOp() {
		return false;
	}

	@Override
	public boolean damage(float amount) {
		return false;
	}

	@Override
	public void takeFood(int amount) { }

	@Override
	public void takeLevels(int amount) { }
}
