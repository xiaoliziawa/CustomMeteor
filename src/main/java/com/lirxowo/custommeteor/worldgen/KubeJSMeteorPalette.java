package com.lirxowo.custommeteor.worldgen;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import appeng.core.definitions.AEBlocks;

public final class KubeJSMeteorPalette {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile WeightedPalette rawPalette;
    private static volatile WeightedPalette resolved;
    private static volatile boolean initialized;

    private KubeJSMeteorPalette() {
    }

    public static void set(WeightedPalette newPalette) {
        rawPalette = newPalette;
        resolved = null;
        initialized = true;
    }

    public static WeightedPalette get() {
        WeightedPalette r = resolved;
        if (r != null) {
            return r;
        }
        synchronized (KubeJSMeteorPalette.class) {
            if (resolved == null) {
                if (rawPalette != null) {
                    resolved = mergeWithDefaults(rawPalette);
                } else {
                    resolved = defaultPalette();
                }
            }
            return resolved;
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static WeightedPalette defaultPalette() {
        WeightedBlockList shell = new WeightedBlockList();
        shell.add(AEBlocks.SKY_STONE_BLOCK.block().defaultBlockState(), 1, true);

        WeightedBlockList core = new WeightedBlockList();
        core.add(AEBlocks.QUARTZ_BLOCK.block().defaultBlockState(), 1, false);
        core.add(AEBlocks.DAMAGED_BUDDING_QUARTZ.block().defaultBlockState(), 1, true);
        core.add(AEBlocks.CHIPPED_BUDDING_QUARTZ.block().defaultBlockState(), 1, true);
        core.add(AEBlocks.FLAWED_BUDDING_QUARTZ.block().defaultBlockState(), 1, true);
        core.add(AEBlocks.FLAWLESS_BUDDING_QUARTZ.block().defaultBlockState(), 1, true);

        WeightedBlockList buds = new WeightedBlockList();
        buds.add(withFacingUp(AEBlocks.SMALL_QUARTZ_BUD.block().defaultBlockState()), 1, true);
        buds.add(withFacingUp(AEBlocks.MEDIUM_QUARTZ_BUD.block().defaultBlockState()), 1, true);
        buds.add(withFacingUp(AEBlocks.LARGE_QUARTZ_BUD.block().defaultBlockState()), 1, true);

        return new WeightedPalette(shell, core, buds, 0.7f);
    }

    public static WeightedPalette mergeWithDefaults(WeightedPalette input) {
        WeightedPalette defaults = defaultPalette();
        WeightedBlockList shell = input.shell().isEmpty() ? defaults.shell() : input.shell();
        WeightedBlockList core = input.core().isEmpty() ? defaults.core() : input.core();
        WeightedBlockList buds = input.buds().isEmpty() ? defaults.buds() : input.buds();

        if (shell.isEmpty()) {
            LOGGER.warn("KubeJS shell list is empty; using defaults.");
            shell = defaults.shell();
        }
        if (core.isEmpty()) {
            LOGGER.warn("KubeJS core list is empty; using defaults.");
            core = defaults.core();
        }
        if (buds.isEmpty()) {
            LOGGER.warn("KubeJS buds list is empty; using defaults.");
            buds = defaults.buds();
        }

        return new WeightedPalette(shell.copy(), core.copy(), buds.copy(), input.budChance());
    }

    private static net.minecraft.world.level.block.state.BlockState withFacingUp(
            net.minecraft.world.level.block.state.BlockState state) {
        if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)) {
            return state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,
                    net.minecraft.core.Direction.UP);
        }
        return state;
    }
}
