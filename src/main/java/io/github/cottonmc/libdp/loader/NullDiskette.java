package io.github.cottonmc.libdp.loader;

import javax.annotation.Nullable;

import io.github.cottonmc.libdp.LibDP;
import io.github.cottonmc.libdp.api.Diskette;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

//TODO: bad idea?
public class NullDiskette extends Diskette {
	public static final NullDiskette INSTANCE = new NullDiskette();

	private NullDiskette() {
		super(new NashornScriptEngineFactory().getScriptEngine(), new Identifier(LibDP.MODID, "null.txt"), "");
	}

	@Override
	public boolean hasRun() {
		return true;
	}

	//TODO: bad idea?
	@Nullable
	@Override
	public Object invokeFunction(String funcName, Object... args) {
		if (funcName.equals("matches") || funcName.equals("test")) return false;
		if (funcName.equals("preview") || funcName.equals("craft") || funcName.equals("apply") || funcName.equals("generateLoot")) return ItemStack.EMPTY;
		return null;
	}
}
