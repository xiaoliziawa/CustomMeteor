package com.lirxowo.custommeteor.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import appeng.worldgen.meteorite.CraterType;
import appeng.worldgen.meteorite.fallout.FalloutMode;
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
                return readData(reader);
            } catch (Exception e) {
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

    private static Data readData(Reader reader) {
        JsonElement root = JsonParser.parseReader(reader);
        if (root == null || !root.isJsonObject()) {
            LOGGER.warn("Meteorite palette config root must be a JSON object.");
            return defaultData();
        }

        JsonObject json = root.getAsJsonObject();
        JsonElement coreElement = json.get("core");

        List<Entry> shell = parseEntries(json.get("shell"), "shell");
        List<Entry> core = parseEntries(coreElement, "core");
        List<Entry> coreNoBud = parseEntries(json.get("coreNoBud"), "coreNoBud");
        List<Entry> buds = parseEntries(json.get("buds"), "buds");

        if (coreNoBud.isEmpty() && isLegacyCoreArray(coreElement) && !core.isEmpty()) {
            coreNoBud.add(core.remove(0));
        }

        float budChance = parseBudChance(json.get("budChance"));
        CraterType craterType = parseCraterType(json.get("craterType"));
        FalloutMode falloutMode = parseFalloutMode(json.get("falloutMode"));
        Boolean pureCrater = parseBoolean(json.get("pureCrater"), "pureCrater");
        Boolean craterLake = parseBoolean(json.get("craterLake"), "craterLake");

        return new Data(
                List.copyOf(shell),
                List.copyOf(core),
                List.copyOf(coreNoBud),
                List.copyOf(buds),
                budChance,
                craterType,
                falloutMode,
                pureCrater,
                craterLake);
    }

    private static List<Entry> parseEntries(JsonElement element, String section) {
        List<Entry> entries = new ArrayList<>();

        if (element == null || element.isJsonNull()) {
            return entries;
        }

        if (!element.isJsonArray()) {
            LOGGER.warn("Meteorite palette section '{}' must be an array.", section);
            return entries;
        }

        JsonArray array = element.getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            Entry entry = parseEntry(array.get(i), section, i);
            if (entry != null) {
                entries.add(entry);
            }
        }

        return entries;
    }

    private static Entry parseEntry(JsonElement element, String section, int index) {
        if (element == null || element.isJsonNull()) {
            return null;
        }

        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String id = element.getAsString().trim();
            return id.isEmpty() ? null : new Entry(id, 1);
        }

        if (!element.isJsonObject()) {
            LOGGER.warn("Meteorite palette {}[{}] must be a string id or object.", section, index);
            return null;
        }

        JsonObject object = element.getAsJsonObject();
        String id = readString(object, "id");
        if (id == null || id.isBlank()) {
            id = readString(object, "block");
        }
        if (id == null || id.isBlank()) {
            id = readString(object, "blockId");
        }
        if (id == null || id.isBlank()) {
            LOGGER.warn("Meteorite palette {}[{}] is missing a valid block id.", section, index);
            return null;
        }

        int weight = 1;
        if (object.has("weight") && !object.get("weight").isJsonNull()) {
            try {
                weight = object.get("weight").getAsInt();
            } catch (Exception ex) {
                LOGGER.warn("Meteorite palette {}[{}] has invalid weight.", section, index);
                return null;
            }
        }
        if (weight <= 0) {
            LOGGER.warn("Meteorite palette {}[{}] has invalid weight {}.", section, index, weight);
            return null;
        }

        return new Entry(id.trim(), weight);
    }

    private static float parseBudChance(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return 0.7f;
        }

        try {
            double chance = element.getAsDouble();
            if (Double.isNaN(chance) || Double.isInfinite(chance)) {
                return 0.7f;
            }
            return (float) Math.max(0.0, Math.min(1.0, chance));
        } catch (Exception ex) {
            LOGGER.warn("Meteorite palette 'budChance' must be a number.");
            return 0.7f;
        }
    }

    private static CraterType parseCraterType(JsonElement element) {
        return parseEnum(element, "craterType", CraterType.class);
    }

    private static FalloutMode parseFalloutMode(JsonElement element) {
        return parseEnum(element, "falloutMode", FalloutMode.class);
    }

    private static Boolean parseBoolean(JsonElement element, String key) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            LOGGER.warn("Meteorite palette '{}' must be a boolean.", key);
            return null;
        }
        return element.getAsBoolean();
    }

    private static <E extends Enum<E>> E parseEnum(JsonElement element, String key, Class<E> enumClass) {
        if (element == null || element.isJsonNull()) {
            return null;
        }

        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            LOGGER.warn("Meteorite palette '{}' must be a string.", key);
            return null;
        }

        String raw = element.getAsString().trim();
        if (raw.isEmpty()) {
            return null;
        }

        try {
            return Enum.valueOf(enumClass, raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Unknown {} value '{}' in meteorite palette config.", key, raw);
            return null;
        }
    }

    private static boolean isLegacyCoreArray(JsonElement coreElement) {
        if (coreElement == null || !coreElement.isJsonArray()) {
            return false;
        }

        JsonArray array = coreElement.getAsJsonArray();
        if (array.isEmpty()) {
            return false;
        }

        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                return false;
            }
        }

        return true;
    }

    private static String readString(JsonObject object, String key) {
        if (!object.has(key)) {
            return null;
        }

        JsonElement element = object.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            return null;
        }

        return element.getAsString();
    }

    private static Data defaultData() {
        return new Data(
                List.of(new Entry("ae2:sky_stone_block", 1)),
                List.of(
                        new Entry("ae2:damaged_budding_quartz", 1),
                        new Entry("ae2:chipped_budding_quartz", 1),
                        new Entry("ae2:flawed_budding_quartz", 1),
                        new Entry("ae2:flawless_budding_quartz", 1)),
                List.of(new Entry("ae2:quartz_block", 1)),
                List.of(
                        new Entry("ae2:small_quartz_bud", 1),
                        new Entry("ae2:medium_quartz_bud", 1),
                        new Entry("ae2:large_quartz_bud", 1)),
                0.7f,
                null,
                null,
                null,
                null);
    }

    public record Entry(String id, int weight) {
    }

    public record Data(List<Entry> shell, List<Entry> core, List<Entry> coreNoBud, List<Entry> buds,
            float budChance, CraterType craterType, FalloutMode falloutMode, Boolean pureCrater, Boolean craterLake) {
    }
}
