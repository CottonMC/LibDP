package io.github.cottonmc.libdp.api.util.loot;

import io.github.cottonmc.libdp.api.util.MutableStack;
import io.github.cottonmc.libdp.api.util.WorldInfo;
import io.github.cottonmc.libdp.api.util.WrappedPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WrappedLootContext {
	private static final Map<String, LootContextParameter<?>> PARAMETERS = new HashMap<>();

	private final LootContext context;

	public WrappedLootContext(LootContext context) {
		this.context = context;
	}

	public Random getRandom() {
		return context.getRandom();
	}

	public float getLuck() {
		return context.getLuck();
	}

	public WorldInfo getWorld() {
		return new WorldInfo(context.getWorld());
	}

	public boolean hasParameter(String parameter) {
		return context.hasParameter(PARAMETERS.get(parameter));
	}

	public Object getParameter(String parameter) {
		Object ret = context.get(PARAMETERS.get(parameter));
		if (ret instanceof PlayerEntity) {
			return new WrappedPlayer((PlayerEntity) ret);
		} else if (ret instanceof Vec3d) {
			return new double[]{ ((Vec3d) ret).x, ((Vec3d) ret).y, ((Vec3d) ret).z, };
		} else if (ret instanceof ItemStack) {
			return new MutableStack((ItemStack) ret);
		}
		return ret;
	}

	static {
//		PARAMETERS.put("this_entity", LootContextParameters.THIS_ENTITY); //TODO: entity wrapper
		PARAMETERS.put("last_damage_player", LootContextParameters.LAST_DAMAGE_PLAYER);
//		PARAMETERS.put("damage_source", LootContextParameters.DAMAGE_SOURCE); //TODO: damage source wrapper
//		PARAMETERS.put("killer_entity", LootContextParameters.KILLER_ENTITY); //TODO: entity wrapper
//		PARAMETERS.put("direct_killer_entity", LootContextParameters.DIRECT_KILLER_ENTITY); //TODO: entity wrapper
		PARAMETERS.put("origin", LootContextParameters.ORIGIN);
//		PARAMETERS.put("block_state", LootContextParameters.BLOCK_STATE); //TODO: block state wrapper
//		PARAMETERS.put("block_entity", LootContextParameters.BLOCK_ENTITY); //TODO: block entity wrapper?
		PARAMETERS.put("tool", LootContextParameters.TOOL);
		PARAMETERS.put("explosion_radius", LootContextParameters.EXPLOSION_RADIUS);
	}
}
