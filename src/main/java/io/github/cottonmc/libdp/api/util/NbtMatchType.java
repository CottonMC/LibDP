package io.github.cottonmc.libdp.api.util;

import net.minecraft.util.StringIdentifiable;

/**
 * The matching mode for nbt checking in an ingredient.
 */
public enum NbtMatchType implements StringIdentifiable {
	/**
	 * Nbt is not checked; any nbt will return true.
	 */
	NONE("none"),
	/**
	 * Nbt is fuzzily checked; the input stack may have extra tags,
	 * but it must have all of the tags that the ingredient stack has,
	 * with the same values.
	 */
	FUZZY("fuzzy"),
	/**
	 * Nbt is strictly checked; only identical nbt will return true.
	 */
	EXACT("exact");

	String name;

	NbtMatchType(String name) {
		this.name = name;
	}

	@Override
	public String asString() {
		return name;
	}

	public static NbtMatchType forName(String name) {
		for (NbtMatchType value : NbtMatchType.values()) {
			if (name.equals(value.asString())) {
				return value;
			}
		}
		return NbtMatchType.NONE;
	}
}
