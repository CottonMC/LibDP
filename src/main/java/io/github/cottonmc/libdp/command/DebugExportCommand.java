package io.github.cottonmc.libdp.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cottonmc.libdp.api.driver.Driver;
import io.github.cottonmc.libdp.api.driver.DriverManager;
import io.github.cottonmc.libdp.loader.DisketteLoader;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.io.File;
import java.io.FileOutputStream;

public class DebugExportCommand implements Command<ServerCommandSource> {
	@Override
	public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		try {
			File file = FabricLoader.getInstance().getGameDir().resolve("debug/libdp.json5").toFile();
			JsonObject json = new JsonObject();
			json.add("Loader", DisketteLoader.getDebugObject());
			for (Driver driver : DriverManager.INSTANCE.getDrivers()) {
				json.add(DriverManager.INSTANCE.getDriverName(driver), driver.getDebugInfo());
			}
			String result = json.getAsString();
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(file,false);
			out.write(result.getBytes());
			out.flush();
			out.close();
			context.getSource().sendFeedback(new LiteralText("Debug info exported!"), true);
			return 1;
		} catch (Exception e) {
			context.getSource().sendError(new LiteralText("Error exporting debug info: " + e.getMessage()));
			return 0;
		}
	}
}
