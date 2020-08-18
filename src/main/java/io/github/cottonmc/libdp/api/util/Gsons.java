package io.github.cottonmc.libdp.api.util;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import net.minecraft.loot.*;

public class Gsons {
    public static final JsonParser PARSER = new JsonParser();

    public static final Gson LOOT_TABLE = LootGsons.getTableGsonBuilder().create();
}
