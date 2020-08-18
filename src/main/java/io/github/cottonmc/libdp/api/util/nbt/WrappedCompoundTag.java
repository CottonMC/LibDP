package io.github.cottonmc.libdp.api.util.nbt;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.*;

import java.util.List;
import java.util.UUID;

/**
 * A wrapper for the CompoundTag class, since that's obfuscated.
 */
public class WrappedCompoundTag {
	private CompoundTag underlying;

	public WrappedCompoundTag(CompoundTag underlying) {
		this.underlying = underlying;
	}

	/**
	 * @return A new, empty compound tag.
	 */
	public static WrappedCompoundTag create() {
		return new WrappedCompoundTag(new CompoundTag());
	}

	/**
	 * @return The underlying compound tag. Only call from Java.
	 */
	public CompoundTag getUnderlying() {
		return underlying;
	}

	/**
	 * @return an array of all the keys in the tag.
	 */
	public String[] getKeys() {
		return underlying.getKeys().toArray(new String[0]);
	}

	/**
	 * @param key The key to check.
	 * @return The type of the key.
	 */
	public String getType(String key) {
		return NbtUtils.getTypeName(underlying.getType(key));
	}

	/**
	 * @param key The key to check for.
	 * @return Whether the tag has this key.
	 */
	public boolean hasTag(String key) {
		return underlying.contains(key);
	}

	/**
	 * @param key The key to check for.
	 * @param type The type to check for. Must match the values in {@link WrappedCompoundTag#getType(String)} exactly.
	 * @return Whether the tag has this key, and the key has this type.
	 */
	public boolean hasTag(String key, String type) {
		return underlying.contains(key) && getType(key).equals(type);
	}

	/**
	 * @param key The key to check for.
	 * @param type The type to check for, as its raw int. You can use {@link net.fabricmc.fabric.api.util.NbtType} for this.
	 * @return Whether the tag has this key, and the key has this type.
	 */
	public boolean hasTag(String key, int type) {
		return underlying.contains(key, type);
	}

	/**
	 * @param key The tag to get.
	 * @return The string form of this tag.
	 */
	public Object getTag(String key) {
		return NbtUtils.getObjectFor(underlying.get(key));
	}

	/**
	 * Get the byte in the tag.
	 * @param key The key to get from..
	 * @return The value of the byte tag with this key, or 0.
	 */
	public byte getByte(String key) {
		return underlying.getByte(key);
	}

	/**
	 * Insert a byte into the tag.
	 * @param key The key to insert at.
	 * @param value The byte to insert.
	 */
	public void putByte(String key, byte value) {
		underlying.putByte(key, value);
	}

	/**
	 * Get a boolean in the tag. Saved as a byte.
	 * @param key The key to get from.
	 * @return The value of the boolean tag with this key, or false.
	 */
	public boolean getBoolean(String key) {
		return underlying.getBoolean(key);
	}

	/**
	 * Insert a boolean into the tag. Saved as a byte.
	 * @param key The key to insert at.
	 * @param value The boolean to insert.
	 */
	public void putBoolean(String key, boolean value) {
		underlying.putBoolean(key, value);
	}

	/**
	 * Get a short in the tag.
	 * @param key The key to get from.
	 * @return The value of the boolean tag with this key, or 0.
	 */
	public short getShort(String key) {
		return underlying.getShort(key);
	}

	/**
	 * Insert a short into the tag.
	 * @param key The key to insert at.
	 * @param value The short to insert.
	 */
	public void putShort(String key, short value) {
		underlying.putShort(key, value);
	}

	/**
	 * Get an int in the tag.
	 * @param key The key to get from.
	 * @return The value of the int tag with this key, or 0.
	 */
	public int getInt(String key) {
		return underlying.getInt(key);
	}

	/**
	 * Insert an int into the tag.
	 * @param key The key to insert at.
	 * @param value The int to insert.
	 */
	public void putInt(String key, int value) {
		underlying.putInt(key, value);
	}

	/**
	 * Get a long in the tag.
	 * @param key The key to get from.
	 * @return The value of the long tag with this key, or 0.
	 */
	public long getLong(String key) {
		return underlying.getLong(key);
	}

	/**
	 * Insert a long into the tag.
	 * @param key The key to insert at.
	 * @param value The long to insert.
	 */
	public void putLong(String key, long value) {
		underlying.putLong(key, value);
	}

	// TODO: Remove "New" suffix from UUID methods

	/**
	 * Reads a {@link UUID} from its NBT representation in this {@code CompoundTag}.
	 * @param key The key to get from.
	 * @return The value of the UUID tags with this key, or "00000000-0000-0000-0000-000000000000".
	 */
	public UUID getUuidNew(String key) {
		return underlying.getUuid(key);
	}

	/**
	 * Returns {@code true} if this {@code CompoundTag} contains a valid UUID representation associated with the given key.
	 * A valid UUID is represented by an int array of length 4.
	 * @param key The key to check at.
	 * @return Whether the tag has a UUID with this key.
	 */
	public boolean containsUuidNew(String key) {
		return underlying.containsUuid(key);
	}

	/**
	 * Insert a UUID into the tag. Saved as an integer array.
	 * @param key The key to insert at.
	 * @param value The long to insert.
	 */
	public void putUuidNew(String key, UUID value) {
		underlying.putUuid(key, value);
	}

	/**
	 * Get a float in the tag.
	 * @param key The key to get from.
	 * @return The value of the float tag with this key, or 0.
	 */
	public float getFloat(String key) {
		return underlying.getFloat(key);
	}

	/**
	 * Insert a float into the tag.
	 * @param key The key to insert at.
	 * @param value The float to insert.
	 */
	public void putFloat(String key, float value) {
		underlying.putFloat(key, value);
	}

	/**
	 * Get a float in the tag.
	 * @param key The key to get from.
	 * @return The value of the long tag with this key, or 0.
	 */
	public double getDouble(String key) {
		return underlying.getDouble(key);
	}

	/**
	 * Insert a double into the tag.
	 * @param key The key to insert at.
	 * @param value The float to insert.
	 */
	public void putDouble(String key, double value) {
		underlying.putDouble(key, value);
	}

	/**
	 * Get an array of bytes in the tag.
	 * @param key The key to get from.
	 * @return The value of the byte array tag with this key, or [].
	 */
	public byte[] getByteArray(String key) {
		return underlying.getByteArray(key);
	}

	/**
	 * Insert an array of bytes into the tag.
	 * @param key The key to insert at.
	 * @param value The array of bytes to insert.
	 */
	public void putByteArray(String key, byte[] value) {
		underlying.putByteArray(key, value);
	}

	/**
	 * Get a string in the tag.
	 * @param key The key to get from.
	 * @return The value of the string tag with this key, or "".
	 */
	public String getString(String key) {
		return underlying.getString(key);
	}

	/**
	 * Insert a string into the tag.
	 * @param key The key to insert at.
	 * @param value The string to insert.
	 */
	public void putString(String key, String value) {
		underlying.putString(key, value);
	}

	/**
	 * Check if the tag has a list of the given type.
	 * @param key The key to check at.
	 * @param type The name of the NBT type to check for.
	 * @return Whether a list at that tag and of that type exists.
	 */
	public boolean hasList(String key, String type) {
		return underlying.contains(key, NbtType.LIST) && ((ListTag)underlying.get(key)).getElementType() == NbtUtils.getTypeNumber(type);
	}

	/**
	 * @param key The key to check.
	 * @return The type of the list at the given key, or "" if it's not a list.
	 */
	public String getListType(String key) {
		Tag tag = underlying.get(key);
		if (tag instanceof ListTag) {
			return NbtUtils.getTypeName(((ListTag)tag).getElementType());
		} else return "";
	}

	/**
	 * Get a list in the tag, wrapped for ease of use in scripts.
	 * @param key The key to get from.
	 * @param type The type of the list to get.
	 * @return The wrapped form of the list tag at the given key and of the given type, or an empty wrapped list.
	 */
	public WrappedListTag getList(String key, String type) {
		return new WrappedListTag(underlying.getList(key, NbtUtils.getTypeNumber(type)));
	}

	/**
	 * Insert a list into the tag. Must be a wrapped list tag.
	 * To create a new empty list tag, call {@link WrappedListTag#create()}.
	 * To create a list tag from a pre-existing list or array,
	 * call {@link WrappedListTag#create(List)} or {@link WrappedListTag#create(Object[])}, respectively.
	 * @param key The key to insert at.
	 * @param value The wrapped list to insert.
	 */
	public void putList(String key, WrappedListTag value) {
		underlying.put(key, value.getUnderlying());
	}

	/**
	 * Get a compound in the tag, wrapped for ease of use in scripts.
	 * @param key The key to get from.
	 * @return The wrapped form of the compound tag at this key, or an empty wrapped compound.
	 */
	public WrappedCompoundTag getCompound(String key) {
		return new WrappedCompoundTag(underlying.getCompound(key));
	}

	/**
	 * Insert a compound into the tag. Must be a wrapped compound tag.
	 * To create a new empty compound tag, call {@link WrappedCompoundTag#create()}.
	 * @param key The key to insert at.
	 * @param value The wrapped compound to insert.
	 */
	public void putCompound(String key, WrappedCompoundTag value) {
		underlying.put(key, value.getUnderlying());
	}

	/**
	 * Get an int array in the tag.
	 * @param key The key to get from.
	 * @return The value of the int array tag with this key, or [].
	 */
	public int[] getIntArray(String key) {
		return underlying.getIntArray(key);
	}

	/**
	 * Insert an array of longs into the tag.
	 * @param key The key to insert at.
	 * @param value The array of ints to insert.
	 */
	public void putIntArray(String key, int[] value) {
		underlying.putIntArray(key, value);
	}

	/**
	 * Get an array of longs in the tag.
	 * @param key The key to get from.
	 * @return The value of the long array tag with this key, or [].
	 */
	public long[] getLongArray(String key) {
		return underlying.getLongArray(key);
	}

	/**
	 * Insert an array of longs into the tag.
	 * @param key The key to insert at.
	 * @param value the array of longs to insert.
	 */
	public void putLongArray(String key, long[] value) {
		underlying.putLongArray(key, value);
	}

	/**
	 * Remove a tag from the compound.
	 * @param key The key of the tag to remove.
	 */
	public void remove(String key) {
		underlying.remove(key);
	}

	/**
	 * Remove all keys from this tag.
	 */
	public void clear() {
		for (String key : getKeys()) {
			remove(key);
		}
	}

	/**
	 * @return Whether there are no tags in the compound.
	 */
	public boolean isEmpty() {
		return underlying.isEmpty();
	}

	/**
	 * @return The string form of the underlying compound tag.
	 */
	public String toString() {
		return underlying.asString();
	}

	/**
	 * @return The hash code of the underlying compound tag.
	 */
	public int hashCode() {
		return underlying.hashCode();
	}
}
