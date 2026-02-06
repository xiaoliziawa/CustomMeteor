package com.lirxowo.custommeteor.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

public final class MeteorPaletteConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
            .resolve("custommeteor")
            .resolve("meteorite_palette.json");

    private static volatile Data cached;

    private MeteorPaletteConfig() {
    }

    public static Data get() {
        Data data = cached;
        if (data != null) {
            return data;
        }
        synchronized (MeteorPaletteConfig.class) {
            if (cached == null) {
                cached = loadOrCreate();
            }
            return cached;
        }
    }

    private static Data loadOrCreate() {
        if (Files.exists(FILE)) {
            try (Reader reader = Files.newBufferedReader(FILE)) {
                Data data = GSON.fromJson(reader, Data.class);
                return data != null ? data : defaultData();
            } catch (IOException e) {
                LOGGER.warn("Failed to read meteorite palette config, using defaults.", e);
                return defaultData();
            }
        }

        Data data = defaultData();
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to write default meteorite palette config.", e);
        }
        return data;
    }

    private static Data defaultData() {
        return new Data(
                List.of("ae2:sky_stone_block"),
                List.of(
                        "ae2:quartz_block",
                        "ae2:damaged_budding_quartz",
                        "ae2:chipped_budding_quartz",
                        "ae2:flawed_budding_quartz",
                        "ae2:flawless_budding_quartz"),
                List.of(
                        "ae2:small_quartz_bud",
                        "ae2:medium_quartz_bud",
                        "ae2:large_quartz_bud"));
    }

    public record Data(List<String> shell, List<String> core, List<String> buds) {
    }
}
