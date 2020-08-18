package io.github.cottonmc.libdp.api.util.nbt;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NbtUtils {
	/**
	 * @param type The int type of the tag you want to get the name of.
	 * @return The string name of the tag type, in all lower case.
	 */
	public static String getTypeName(int type) {
		switch (type) {
			case 0:
				return "end";
			case 1:
				return "byte";
			case 2:
				return "short";
			case 3:
				return "int";
			case 4:
				return "long";
			case 5:
				return "float";
			case 6:
				return "double";
			case 7:
				return "byte array";
			case 8:
				return "string";
			case 9:
				return "list";
			case 10:
				return "compound";
			case 11:
				return "int array";
			case 12:
				return "long array";
			case 99:
				return "any number";
			default:
				return "unknown";
		}
	}

	/**
	 * @param type The string form of the tag type you want to get the magic number of. Not case sensitive.
	 * @return The int number of the tag type.
	 */
	public static int getTypeNumber(String type) {
		String lower = type.toLowerCase();
		switch(lower) {
			case "end":
				return 0;
			case "byte":
				return 1;
			case "short":
				return 2;
			case "int":
				return 3;
			case "long":
				return 4;
			case "float":
				return 5;
			case "double":
				return 6;
			case "byte array":
				return 7;
			case "string":
				return 8;
			case "list":
				return 9;
			case "compound":
				return 10;
			case "int array":
				return 11;
			case "long array":
				return 12;
			case "any number":
			case "number":
				return 99;
			default:
				return -1;
		}
	}

	/**
	 * @param value The object to get a tagified form of.
	 * @return The tagified form of the object.
	 */
	public static Tag getTagFor(Object value) {
		if (value == null) return null;
		if (value instanceof JsonPrimitive) {
			JsonPrimitive prim = (JsonPrimitive)value;
			if (prim.isNumber()) {
				return DoubleTag.of(prim.getAsDouble());
			} else if (prim.isBoolean()) {
				return ByteTag.of(prim.getAsBoolean());
			} else {
				return StringTag.of(prim.getAsString());
			}
		}
		if (value instanceof Byte) {
			return ByteTag.of((byte) value);
		} else if (value instanceof Boolean) {
			return ByteTag.of((byte)((boolean)value ? 1 : 0));
		} else if (value instanceof Short) {
			return ShortTag.of((short)value);
		} else if (value instanceof Integer) {
			return IntTag.of((int)value);
		} else if (value instanceof Long) {
			return LongTag.of((long)value);
		} else if (value instanceof Float) {
			return FloatTag.of((float)value);
		} else if (value instanceof Double) {
			return DoubleTag.of((double) value);
		} else if (value instanceof Byte[]) {
			return new ByteArrayTag(Arrays.asList((Byte[])value));
		} else if (value instanceof String) {
			return StringTag.of((String)value);
		} else if (value instanceof WrappedListTag) {
			return ((WrappedListTag)value).getUnderlying();
		} else if (value instanceof WrappedCompoundTag) {
			return ((WrappedCompoundTag) value).getUnderlying();
		} else if (value instanceof Integer[]) {
			return new IntArrayTag(Arrays.asList((Integer[])value));
		} else if (value instanceof Long[]) {
			return new LongArrayTag(Arrays.asList((Long[])value));
		} else return StringTag.of(value.toString());
	}

	/**
	 * @param tag The tag to get the object form of.
	 * @return The object form of the tag.
	 */
	public static Object getObjectFor(Tag tag) {
		if (tag == null) return null;
		if (tag instanceof ByteTag) return ((ByteTag)tag).getByte();
		else if (tag instanceof ShortTag) return ((ShortTag)tag).getShort();
		else if (tag instanceof IntTag) {
			return ((IntTag)tag).getInt();
		} else if (tag instanceof LongTag) {
			return ((LongTag)tag).getLong();
		} else if (tag instanceof FloatTag) {
			return ((FloatTag)tag).getFloat();
		} else if (tag instanceof DoubleTag) {
			return ((DoubleTag)tag).getDouble();
		} else if (tag instanceof ByteArrayTag) {
			return ((ByteArrayTag)tag).getByteArray();
		} else if (tag instanceof StringTag) {
			return tag.asString();
		} else if (tag instanceof ListTag) {
			return new WrappedListTag((ListTag)tag);
		} else if (tag instanceof CompoundTag) {
			return new WrappedCompoundTag((CompoundTag)tag);
		} else if (tag instanceof IntArrayTag) {
			IntTag[] tags = ((IntArrayTag)tag).toArray(new IntTag[0]);
			IntList ret = new IntArrayList();
			for (IntTag intTag : tags) {
				ret.add(intTag.getInt());
			}
			return ret.toArray(new int[0]);
		} else if (tag instanceof LongArrayTag) {
			LongTag[] tags = ((LongArrayTag)tag).toArray(new LongTag[0]);
			LongList ret = new LongArrayList();
			for (LongTag longTag : tags) {
				ret.add(longTag.getLong());
			}
			return ret.toArray(new long[0]);
		} else return tag.asString();
	}

	public static CompoundTag fromJson(JsonObject json) {
		CompoundTag ret = new CompoundTag();
		for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
			String key = entry.getKey();
			JsonElement elem = entry.getValue();
			if (elem instanceof JsonObject) {
				ret.put(key, fromJson((JsonObject)elem));
			} else if (elem instanceof JsonPrimitive) {
				JsonPrimitive prim = (JsonPrimitive) elem;
				ret.put(key, getTagFor(prim));
			} else if (elem instanceof JsonArray) {
				ret.put(key, parseJsonList((JsonArray)elem).getUnderlying());
			}
		}
		return ret;
	}

	public static WrappedListTag parseJsonList(JsonArray array) {
		List<Object> ret = new ArrayList<>();
		for (JsonElement entry : array) {
			if (entry instanceof JsonObject) {
				ret.add(new WrappedCompoundTag(fromJson((JsonObject)entry)));
			} else if (entry instanceof JsonPrimitive) {
				JsonPrimitive prim = (JsonPrimitive)entry;
				ret.add(getTagFor(prim));
			} else if (entry instanceof JsonArray) {
				ret.add(parseJsonList(array));
			}
		}
		return WrappedListTag.create(ret);
	}

	public static JsonObject toJson(CompoundTag tag) {
		JsonObject ret = new JsonObject();
		for (String key : tag.getKeys()) {
			Tag elem = tag.get(key);
			if (elem instanceof CompoundTag) {
				ret.add(key, toJson((CompoundTag)elem));
			} else if (elem instanceof ListTag) {
				ret.add(key, parseTagList((ListTag)elem));
			} else if (elem instanceof AbstractNumberTag) {
				ret.add(key, new JsonPrimitive(((AbstractNumberTag)elem).getNumber()));
			}
		}
		return ret;
	}

	public static JsonArray parseTagList(ListTag list) {
		JsonArray ret = new JsonArray();
		for (Tag tag : list) {
			if (tag instanceof CompoundTag) {
				ret.add(toJson((CompoundTag)tag));
			} else if (tag instanceof ListTag) {
				ret.add(parseTagList((ListTag)tag));
			} else if (tag instanceof AbstractNumberTag) {
				ret.add(new JsonPrimitive(((AbstractNumberTag)tag).getNumber()));
			}
		}
		return ret;
	}
}
