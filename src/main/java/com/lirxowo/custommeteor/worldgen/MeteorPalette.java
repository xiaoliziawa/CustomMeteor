package com.lirxowo.custommeteor.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.logging.LogUtils;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import appeng.core.definitions.AEBlocks;
import com.lirxowo.custommeteor.config.MeteorPaletteConfig;
import org.slf4j.Logger;

public final class MeteorPalette {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final TagKey<Block> SHELL_TAG = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("custommeteor", "meteorite_shell"));
    public static final TagKey<Block> CORE_TAG = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("custommeteor", "meteorite_core"));
    public static final TagKey<Block> BUDS_TAG = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("custommeteor", "meteorite_buds"));

    private MeteorPalette() {
    }

    public static Palette resolve(RandomSource random) {
        MeteorPaletteConfig.Data config = MeteorPaletteConfig.get();

        List<BlockState> shellOptions = statesFromConfig(config.shell());
        if (shellOptions.isEmpty()) {
            shellOptions = tagStates(SHELL_TAG);
        }
        if (shellOptions.isEmpty()) {
            shellOptions = List.of(AEBlocks.SKY_STONE_BLOCK.block().defaultBlockState());
        }

        List<BlockState> coreBlocks = statesFromConfig(config.core());
        if (coreBlocks.isEmpty()) {
            coreBlocks = tagStates(CORE_TAG);
        }
        if (coreBlocks.isEmpty()) {
            coreBlocks = List.of(
                    AEBlocks.QUARTZ_BLOCK.block().defaultBlockState(),
                    AEBlocks.DAMAGED_BUDDING_QUARTZ.block().defaultBlockState(),
                    AEBlocks.CHIPPED_BUDDING_QUARTZ.block().defaultBlockState(),
                    AEBlocks.FLAWED_BUDDING_QUARTZ.block().defaultBlockState(),
                    AEBlocks.FLAWLESS_BUDDING_QUARTZ.block().defaultBlockState());
        }

        List<BlockState> budBlocks = statesFromConfig(config.buds()).stream()
                .map(MeteorPalette::withFacingUp)
                .collect(Collectors.toList());
        if (budBlocks.isEmpty()) {
            budBlocks = tagStates(BUDS_TAG).stream()
                    .map(MeteorPalette::withFacingUp)
                    .collect(Collectors.toList());
        }
        if (budBlocks.isEmpty()) {
            budBlocks = List.of(
                    withFacingUp(AEBlocks.SMALL_QUARTZ_BUD.block().defaultBlockState()),
                    withFacingUp(AEBlocks.MEDIUM_QUARTZ_BUD.block().defaultBlockState()),
                    withFacingUp(AEBlocks.LARGE_QUARTZ_BUD.block().defaultBlockState()));
        }

        BlockState shell = shellOptions.get(random.nextInt(shellOptions.size()));
        return new Palette(shell, coreBlocks, budBlocks);
    }

    private static List<BlockState> statesFromConfig(List<String> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<BlockState> states = new ArrayList<>();
        for (String entry : entries) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(entry);
            if (id == null) {
                LOGGER.warn("Invalid block id in meteorite palette config: {}", entry);
                continue;
            }
            if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                LOGGER.warn("Unknown block id in meteorite palette config: {}", entry);
                continue;
            }
            Block block = BuiltInRegistries.BLOCK.get(id);
            BlockState state = block.defaultBlockState();
            if (!state.isAir()) {
                states.add(state);
            }
        }
        return states;
    }

    private static List<BlockState> tagStates(TagKey<Block> tag) {
        return BuiltInRegistries.BLOCK.getTag(tag)
                .map(set -> set.stream()
                        .map(holder -> holder.value().defaultBlockState())
                        .filter(state -> !state.isAir())
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    private static BlockState withFacingUp(BlockState state) {
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.setValue(BlockStateProperties.FACING, Direction.UP);
        }
        return state;
    }

    public record Palette(BlockState shell, List<BlockState> core, List<BlockState> buds) {
    }
}
