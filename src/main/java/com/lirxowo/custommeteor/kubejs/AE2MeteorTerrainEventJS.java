package com.lirxowo.custommeteor.kubejs;

import java.util.Locale;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import appeng.worldgen.meteorite.CraterType;
import appeng.worldgen.meteorite.fallout.FalloutMode;

public class AE2MeteorTerrainEventJS implements KubeEvent {
    private final BlockPos pos;
    private final Holder<Biome> biome;
    private CraterType craterType;
    private FalloutMode falloutMode;
    private boolean pureCrater;
    private boolean craterLake;

    public AE2MeteorTerrainEventJS(BlockPos pos, Holder<Biome> biome, CraterType craterType, FalloutMode falloutMode,
            boolean pureCrater, boolean craterLake) {
        this.pos = pos;
        this.biome = biome;
        this.craterType = craterType;
        this.falloutMode = falloutMode;
        this.pureCrater = pureCrater;
        this.craterLake = craterLake;
    }

    public BlockPos getPos() {
        return pos;
    }

    public String biomeId() {
        if (biome == null) {
            return "unknown";
        }
        return biome.unwrapKey()
                .map(ResourceKey::location)
                .map(ResourceLocation::toString)
                .orElse("unknown");
    }

    public String getBiomeId() {
        return biomeId();
    }

    public boolean isBiome(String idOrTag) {
        if (biome == null || idOrTag == null) {
            return false;
        }
        String raw = idOrTag.trim();
        if (raw.isEmpty()) {
            return false;
        }
        if (raw.startsWith("#")) {
            ResourceLocation tagId = ResourceLocation.tryParse(raw.substring(1));
            if (tagId == null) {
                return false;
            }
            TagKey<Biome> tag = TagKey.create(Registries.BIOME, tagId);
            return biome.is(tag);
        }
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            return false;
        }
        return biome.is(ResourceKey.create(Registries.BIOME, id));
    }

    public String getCraterType() {
        return craterType.name();
    }

    public String getFalloutMode() {
        return falloutMode.name();
    }

    public boolean isPureCrater() {
        return pureCrater;
    }

    public boolean isCraterLake() {
        return craterLake;
    }

    public void craterType(String value) {
        setCraterType(parseCraterType(value));
    }

    public void falloutMode(String value) {
        setFalloutMode(parseFalloutMode(value));
    }

    public void pureCrater(boolean value) {
        pureCrater = value;
    }

    public void craterLake(boolean value) {
        craterLake = value;
    }

    public CraterType craterTypeValue() {
        return craterType;
    }

    public FalloutMode falloutModeValue() {
        return falloutMode;
    }

    private void setCraterType(CraterType value) {
        if (value != null) {
            craterType = value;
        }
    }

    private void setFalloutMode(FalloutMode value) {
        if (value != null) {
            falloutMode = value;
        }
    }

    private static CraterType parseCraterType(String value) {
        if (value == null) {
            return null;
        }
        try {
            return CraterType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static FalloutMode parseFalloutMode(String value) {
        if (value == null) {
            return null;
        }
        try {
            return FalloutMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

}
