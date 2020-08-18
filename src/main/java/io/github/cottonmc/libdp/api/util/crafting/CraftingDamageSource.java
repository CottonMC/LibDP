package io.github.cottonmc.libdp.api.util.crafting;

import net.minecraft.entity.damage.DamageSource;

public class CraftingDamageSource extends DamageSource {
	public static final CraftingDamageSource INSTANCE = new CraftingDamageSource();

	private CraftingDamageSource() {
		super("libdp.crafting");
		this.setBypassesArmor();
		this.setUnblockable();
	}
}
