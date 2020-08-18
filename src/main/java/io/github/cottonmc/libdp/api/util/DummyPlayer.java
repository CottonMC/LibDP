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

	public float getHealth() {
		return 0;
	}

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

	public int getLevel() {
		return 0;
	}

	public boolean isWet() {
		return false;
	}

	public boolean isCreative() {
		return false;
	}

	public boolean damage(float amount) {
		return false;
	}

	public boolean damage(int amount) {
		return false;
	}

	public void takeFood(int amount) {
	}

	public void takeLevels(int amount) {
	}
}
