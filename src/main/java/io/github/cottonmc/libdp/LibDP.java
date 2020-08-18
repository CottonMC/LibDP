package io.github.cottonmc.libdp;

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
import net.minecraft.recipe.SpecialRecipeSerializer;
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

	public static RecipeSerializer<CustomSpecialCraftingRecipe> CUSTOM_SPECIAL_SERIALIZER;

	@Override
	public void onInitialize() {
		FabricLoader.getInstance().getEntrypoints(MODID, DriverInitializer.class).forEach(init ->
				init.init(DriverManager.INSTANCE));
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DisketteLoader());
		CUSTOM_SPECIAL_SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MODID,
				"custom_special_crafting"), new SpecialRecipeSerializer<>(CustomSpecialCraftingRecipe::new));
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
