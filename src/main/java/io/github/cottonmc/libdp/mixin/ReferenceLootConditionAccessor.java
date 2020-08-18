package io.github.cottonmc.libdp.mixin;

import net.minecraft.loot.condition.ReferenceLootCondition;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ReferenceLootCondition.class)
public interface ReferenceLootConditionAccessor {
    @Invoker("<init>")
    static ReferenceLootCondition callConstructor(Identifier id) {
        throw new AssertionError();
    }
}
