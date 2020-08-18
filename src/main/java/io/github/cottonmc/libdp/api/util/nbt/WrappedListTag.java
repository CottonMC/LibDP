package io.github.cottonmc.libdp.api.util.nbt;

import net.minecraft.nbt.*;

import java.util.Arrays;
import java.util.List;

/**
 * A wrapper for the ListTag class, since that's obfuscated.
 */
public class WrappedListTag {
	private ListTag underlying;

	public WrappedListTag(ListTag underlying) {
		this.underlying = underlying;
	}

	/**
	 * @return A new, empty list tag.
	 */
	public static WrappedListTag create() {
		return new WrappedListTag(new ListTag());
	}

	/**
	 * @param orig The list to add into the tag.
	 * @return a new list tag containing tagified forms of the passed list.
	 */
	public static WrappedListTag create(List orig) {
		ListTag ret = new ListTag();
		for (Object o : orig) {
			ret.addTag(ret.size(), NbtUtils.getTagFor(o));
		}
		return new WrappedListTag(ret);
	}

	/**
	 * @param orig The array to add into the tag.
	 * @return a new list tag containing tagified forms of the passed array.
	 */
	public static WrappedListTag create(Object[] orig) {
		ListTag ret = new ListTag();
		for (Object o : orig) {
			ret.addTag(ret.size(), NbtUtils.getTagFor(o));
		}
		return new WrappedListTag(ret);
	}

	/**
	 * @return The underlying list tag. Only call from java.
	 */
	public ListTag getUnderlying() {
		return underlying;
	}

	/**
	 * List tags can only accept one type of object tag.
	 * @return The type of tag the list will accept.
	 */
	public String getListType() {
		return NbtUtils.getTypeName(underlying.getElementType());
	}

	/**
	 * @return How many elements are in the list.
	 */
	public int getSize() {
		return underlying.size();
	}

	/**
	 * Get an object from the list.
	 * @param index The index of the item to get.
	 * @return The object form of the tag at that point.
	 */
	public Object get(int index) {
		return NbtUtils.getObjectFor(underlying.get(index));
	}

	/**
	 * Set an object in the list. Will override.
	 * @param index The index of the item to set.
	 * @param value The object form to add in
	 * @return Whether the object could be successfully added.
	 */
	public boolean set(int index, Object value) {
		switch(underlying.getElementType()) {
			case 0:
				return underlying.setTag(index, NbtUtils.getTagFor(value));
			case 1:
				if (value instanceof Byte) {
					return underlying.setTag(index, ByteTag.of((Byte)value));
				} else return false;
			case 2:
				if (value instanceof Short) return underlying.setTag(index, ShortTag.of((Short)value));
				 else return false;
			case 3:
				if (value instanceof Integer) return underlying.setTag(index, IntTag.of((Integer)value));
				else return false;
			case 4:
				if (value instanceof Long) return underlying.setTag(index, LongTag.of((Long)value));
				else return false;
			case 5:
				if (value instanceof Float) return underlying.setTag(index, FloatTag.of((Float)value));
				else return false;
			case 6:
				if (value instanceof Double) return underlying.setTag(index, DoubleTag.of((Double)value));
				else return false;
			case 7:
				if (value instanceof Byte[]) return underlying.setTag(index, new ByteArrayTag(Arrays.asList((Byte[])value)));
				else return false;
			case 8:
				if (value instanceof String) return underlying.setTag(index, StringTag.of((String)value));
				else return false;
			case 9:
				if (value instanceof WrappedListTag) return underlying.setTag(index, ((WrappedListTag)value).getUnderlying());
				else return false;
			case 10:
				if (value instanceof WrappedCompoundTag) return underlying.setTag(index, ((WrappedCompoundTag)value).getUnderlying());
				else return false;
			case 11:
				if (value instanceof Integer[]) return underlying.setTag(index, new IntArrayTag(Arrays.asList((Integer[])value)));
				else return false;
			case 12:
				if (value instanceof Long[]) return underlying.setTag(index, new LongArrayTag(Arrays.asList((Long[])value)));
				else return false;
		}
		return false;
	}

	/**
	 * Add an element to the end of the list.
	 * @param value The object to add.
	 * @return Whether the tagified object could be added.
	 */
	public boolean add(Object value) {
		return add(getSize(), value);
	}

	/**
	 * Add an element to the list, at a certain index. Will not override.
	 * @param index The index to add at.
	 * @param value The object to add.
	 * @return Whether the tagified object could be added.
	 */
	public boolean add(int index, Object value) {
		switch(underlying.getElementType()) {
			case 0:
				return underlying.addTag(index, NbtUtils.getTagFor(value));
			case 1:
				if (value instanceof Byte) {
					return underlying.addTag(index, ByteTag.of((Byte)value));
				} else return false;
			case 2:
				if (value instanceof Short) return underlying.addTag(index, ShortTag.of((Short)value));
				else return false;
			case 3:
				if (value instanceof Integer) return underlying.addTag(index, IntTag.of((Integer)value));
				else return false;
			case 4:
				if (value instanceof Long) return underlying.addTag(index, LongTag.of((Long)value));
				else return false;
			case 5:
				if (value instanceof Float) return underlying.addTag(index, FloatTag.of((Float)value));
				else return false;
			case 6:
				if (value instanceof Double) return underlying.addTag(index, DoubleTag.of((Double)value));
				else return false;
			case 7:
				if (value instanceof Byte[]) return underlying.addTag(index, new ByteArrayTag(Arrays.asList((Byte[])value)));
				else return false;
			case 8:
				if (value instanceof String) return underlying.addTag(index, StringTag.of((String)value));
				else return false;
			case 9:
				if (value instanceof WrappedListTag) return underlying.addTag(index, ((WrappedListTag)value).getUnderlying());
				else return false;
			case 10:
				if (value instanceof WrappedCompoundTag) return underlying.addTag(index, ((WrappedCompoundTag)value).getUnderlying());
				else return false;
			case 11:
				if (value instanceof Integer[]) return underlying.addTag(index, new IntArrayTag(Arrays.asList((Integer[])value)));
				else return false;
			case 12:
				if (value instanceof Long[]) return underlying.addTag(index, new LongArrayTag(Arrays.asList((Long[])value)));
				else return false;
		}
		return false;
	}

	/**
	 * Remove an element from the list. If this is the last element, the list's type will be reset.
	 * @param index The index of the element to remove.
	 * @return The object form of the tag removed.
	 */
	public Object remove(int index) {
		return NbtUtils.getObjectFor(underlying.remove(index));
	}

	/**
	 * Empty all entries from the list, and reset its type.
	 */
	public void clear() {
		underlying.clear();
	}

	/**
	 * @return Whether there are no elements in the list.
	 */
	public boolean isEmpty() {
		return underlying.isEmpty();
	}

	/**
	 * @return The string form of the underlying list tag.
	 */
	public String toString() {
		return underlying.asString();
	}

	/**
	 * @return The hash code of the underlying list tag.
	 */
	public int hashCode() {
		return underlying.hashCode();
	}
}
