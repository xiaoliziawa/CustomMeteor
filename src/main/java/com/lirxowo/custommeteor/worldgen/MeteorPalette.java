package com.lirxowo.custommeteor.worldgen;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.logging.LogUtils;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

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

    public static WeightedPalette resolve() {
        MeteorPaletteConfig.Data config = MeteorPaletteConfig.get();
        WeightedPalette defaults = KubeJSMeteorPalette.defaultPalette();

        WeightedBlockList shell = buildFromConfig(config.shell(), true, false, "shell");
        if (shell.isEmpty()) {
            shell = buildFromTag(SHELL_TAG, true, false);
        }
        if (shell.isEmpty()) {
            shell = defaults.shell().copy();
        }

        WeightedBlockList core = new WeightedBlockList();
        addConfigEntries(core, config.coreNoBud(), false, false, "coreNoBud");
        addConfigEntries(core, config.core(), true, false, "core");

        if (core.isEmpty()) {
            addCoreTagFallback(core);
        }
        if (core.isEmpty()) {
            core = defaults.core().copy();
        }

        WeightedBlockList buds = buildFromConfig(config.buds(), true, true, "buds");
        if (buds.isEmpty()) {
            buds = buildFromTag(BUDS_TAG, true, true);
        }
        if (buds.isEmpty()) {
            buds = defaults.buds().copy();
        }

        return new WeightedPalette(shell.copy(), core.copy(), buds.copy(), config.budChance());
    }

    private static WeightedBlockList buildFromConfig(List<MeteorPaletteConfig.Entry> entries, boolean allowBud,
            boolean forceFacingUp, String section) {
        WeightedBlockList list = new WeightedBlockList();
        addConfigEntries(list, entries, allowBud, forceFacingUp, section);
        return list;
    }

    private static void addConfigEntries(WeightedBlockList list, List<MeteorPaletteConfig.Entry> entries,
            boolean allowBud, boolean forceFacingUp, String section) {
        if (entries == null || entries.isEmpty()) {
            return;
        }

        for (MeteorPaletteConfig.Entry entry : entries) {
            if (entry == null || entry.id() == null || entry.id().isBlank()) {
                continue;
            }

            ResourceLocation id = ResourceLocation.tryParse(entry.id().trim());
            if (id == null) {
                LOGGER.warn("Invalid block id in meteorite palette {} section: {}", section, entry.id());
                continue;
            }

            if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                LOGGER.warn("Unknown block id in meteorite palette {} section: {}", section, entry.id());
                continue;
            }

            BlockState state = BuiltInRegistries.BLOCK.get(id).defaultBlockState();
            if (state.isAir()) {
                continue;
            }

            if (forceFacingUp) {
                state = withFacingUp(state);
            }

            list.add(state, entry.weight(), allowBud);
        }
    }

    private static WeightedBlockList buildFromTag(TagKey<Block> tag, boolean allowBud, boolean forceFacingUp) {
        WeightedBlockList list = new WeightedBlockList();
        for (BlockState state : tagStates(tag)) {
            BlockState toAdd = forceFacingUp ? withFacingUp(state) : state;
            list.add(toAdd, 1, allowBud);
        }
        return list;
    }

    private static void addCoreTagFallback(WeightedBlockList list) {
        List<BlockState> states = tagStates(CORE_TAG);
        boolean first = true;
        for (BlockState state : states) {
            list.add(state, 1, !first);
            first = false;
        }
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
}
