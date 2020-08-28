package io.github.cottonmc.libdp;

import java.nio.file.Files;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.cottonmc.libdp.api.DriverInitializer;
import io.github.cottonmc.libdp.api.driver.DriverManager;
import io.github.cottonmc.libdp.api.driver.recipe.CustomSpecialCraftingRecipe;
import io.github.cottonmc.libdp.command.DebugExportCommand;
import io.github.cottonmc.libdp.command.HeldItemCommand;
import io.github.cottonmc.libdp.loader.DisketteLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;

public class LibDP implements ModInitializer {
	public static final String MODID = "libdp";

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static RecipeSerializer<CustomSpecialCraftingRecipe> CUSTOM_SPECIAL_SERIALIZER = new CustomSpecialCraftingRecipe.Serializer();

	//if true, custom special recipes will not register their serializer and will lie about their identity during sync
	//WARNING: CURRENTLY UNTESTED
	public static boolean COMPATIBILITY_MODE = false;

	@Override
	public void onInitialize() {
		//TODO: real config
		COMPATIBILITY_MODE = Boolean.getBoolean("libdp.compat")
				|| Files.exists(FabricLoader.getInstance().getConfigDir().resolve("libdp_compat_mode.txt"));
		FabricLoader.getInstance().getEntrypoints(MODID, DriverInitializer.class).forEach(init ->
				init.init(DriverManager.INSTANCE));
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DisketteLoader());
		if (!COMPATIBILITY_MODE) {
			Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MODID,
					"custom_special_crafting"), CUSTOM_SPECIAL_SERIALIZER);
		}
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			
			//New nodes
			LiteralCommandNode<ServerCommandSource> libdpNode = CommandManager
					.literal(MODID)
					.build();

			LiteralCommandNode<ServerCommandSource> heldNode = CommandManager
					.literal("held")
					.executes(new HeldItemCommand())
					.build();

			LiteralCommandNode<ServerCommandSource> debugNode = CommandManager
					.literal("debug")
					.requires(source -> source.hasPermissionLevel(3))
					.build();

			LiteralCommandNode<ServerCommandSource> debugExportNode = CommandManager
					.literal("export")
					.executes(new DebugExportCommand())
					.build();

			//Stitch nodes together
			libdpNode.addChild(heldNode);
			debugNode.addChild(debugExportNode);
			libdpNode.addChild(debugNode);
			dispatcher.getRoot().addChild(libdpNode);
		});
	}
}
