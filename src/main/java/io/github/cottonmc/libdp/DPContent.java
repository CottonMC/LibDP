package io.github.cottonmc.libdp;

import io.github.cottonmc.libdp.api.DriverInitializer;
import io.github.cottonmc.libdp.api.driver.DriverManager;
import io.github.cottonmc.libdp.api.driver.recipe.RecipeDriver;
import io.github.cottonmc.libdp.api.driver.util.Nbt;
import io.github.cottonmc.libdp.api.driver.util.DriverUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.Identifier;

public class DPContent implements DriverInitializer {

	@Override
	public void init(DriverManager manager) {
		manager.addDriver("libdp.recipe.RecipeDriver", RecipeDriver.INSTANCE);
		manager.addAssistant("libdp.util.DriverUtils", DriverUtils.INSTANCE);
		manager.addAssistant("libdp.util.Nbt", Nbt.INSTANCE);
		manager.addStackFactory(new Identifier("minecraft", "potion"), (id) -> {
			Potion potion = Potion.byId(id.toString());
			if (potion == Potions.EMPTY) return ItemStack.EMPTY;
			return PotionUtil.setPotion(new ItemStack(Items.POTION), potion);
		});
	}

}
