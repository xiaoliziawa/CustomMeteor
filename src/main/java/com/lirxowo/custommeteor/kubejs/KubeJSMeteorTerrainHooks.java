package com.lirxowo.custommeteor.kubejs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure.GenerationContext;
import net.minecraft.world.level.LevelAccessor;

import net.minecraftforge.fml.ModList;

import appeng.worldgen.meteorite.CraterType;
import appeng.worldgen.meteorite.fallout.FalloutMode;
import dev.latvian.mods.kubejs.script.ScriptType;

public final class KubeJSMeteorTerrainHooks {
    private KubeJSMeteorTerrainHooks() {
    }

    public static TerrainOverrides apply(GenerationContext context, BlockPos pos, CraterType craterType,
            FalloutMode falloutMode, boolean pureCrater, boolean craterLake) {
        if (!ModList.get().isLoaded("kubejs")) {
            return new TerrainOverrides(craterType, falloutMode, pureCrater, craterLake);
        }

        Holder<Biome> biome = context.chunkGenerator()
                .getBiomeSource()
                .getBiomesWithin(pos.getX(), context.chunkGenerator().getSeaLevel(), pos.getZ(), 0,
                        context.randomState().sampler())
                .stream()
                .findFirst()
                .orElse(null);

        AE2MeteorTerrainEventJS event = new AE2MeteorTerrainEventJS(pos, biome, craterType, falloutMode, pureCrater,
                craterLake);
        AE2MeteorEvents.TERRAIN.post(ScriptType.STARTUP, event);

        return finalizeOverrides(event);
    }

    public static TerrainOverrides apply(LevelAccessor level, BlockPos pos, CraterType craterType,
            FalloutMode falloutMode, boolean pureCrater, boolean craterLake) {
        if (!ModList.get().isLoaded("kubejs")) {
            return new TerrainOverrides(craterType, falloutMode, pureCrater, craterLake);
        }

        Holder<Biome> biome = level.getBiome(pos);
        AE2MeteorTerrainEventJS event = new AE2MeteorTerrainEventJS(pos, biome, craterType, falloutMode, pureCrater,
                craterLake);
        AE2MeteorEvents.TERRAIN.post(ScriptType.STARTUP, event);

        return finalizeOverrides(event);
    }

    private static TerrainOverrides finalizeOverrides(AE2MeteorTerrainEventJS event) {
        return new TerrainOverrides(event.craterTypeValue(), event.falloutModeValue(), event.isPureCrater(),
                event.isCraterLake());
    }

    public record TerrainOverrides(CraterType craterType, FalloutMode falloutMode, boolean pureCrater,
            boolean craterLake) {
    }
}
