package io.github.cottonmc.libdp.mixin;

import java.util.List;

import io.github.cottonmc.libdp.impl.ReloadListenersAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceReloadListener;

@Mixin(ReloadableResourceManagerImpl.class)
public abstract class MixinResourceManagerImpl implements ReloadableResourceManager, ReloadListenersAccessor {

	@Shadow @Final private List<ResourceReloadListener> listeners;

	@Override
	public List<ResourceReloadListener> libdp$getListeners() {
		return listeners;
	}


}
