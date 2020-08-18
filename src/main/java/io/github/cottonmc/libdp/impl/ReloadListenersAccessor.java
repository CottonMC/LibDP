package io.github.cottonmc.libdp.impl;

import net.minecraft.resource.ResourceReloadListener;

import java.util.List;

public interface ReloadListenersAccessor {
	List<ResourceReloadListener> libdp$getListeners();
}
