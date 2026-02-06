package com.lirxowo.custommeteor.worldgen;

import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.block.state.BlockState;

import appeng.worldgen.meteorite.MeteoriteBlockPutter;

public final class KubeJSMeteoritePlacer {
    private KubeJSMeteoritePlacer() {
    }

    public static boolean place(LevelAccessor level, RandomSource random, BoundingBox boundingBox,
            MeteoriteBlockPutter putter, int x, int y, int z, double squaredMeteoriteSize, WeightedPalette palette) {
        if (palette == null) {
            return false;
        }

        WeightedBlockList shell = palette.shell();
        WeightedBlockList core = palette.core();
        WeightedBlockList buds = palette.buds();

        if (shell.isEmpty() || core.isEmpty()) {
            return false;
        }

        int meteorXLength = clampX(boundingBox, x - 8);
        int meteorXHeight = clampX(boundingBox, x + 8);
        int meteorZLength = clampZ(boundingBox, z - 8);
        int meteorZHeight = clampZ(boundingBox, z + 8);

        MutableBlockPos pos = new MutableBlockPos();
        for (int i = meteorXLength; i <= meteorXHeight; i++) {
            pos.setX(i);
            for (int j = y - 8; j < y + 8; j++) {
                pos.setY(j);
                for (int k = meteorZLength; k <= meteorZHeight; k++) {
                    pos.setZ(k);
                    int dx = i - x;
                    int dy = j - y;
                    int dz = k - z;

                    if (dx * dx * 0.7 + dy * dy * (j > y ? 1.4 : 0.8) + dz * dz * 0.7 < squaredMeteoriteSize) {
                        if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1 && Math.abs(dz) <= 1) {
                            if (dy == -1) {
                                WeightedBlockList.Entry coreEntry = core.getRandomEntry(random);
                                if (coreEntry != null) {
                                    putter.put(level, pos, coreEntry.state());
                                    if (coreEntry.allowBud()
                                            && !buds.isEmpty()
                                            && (dx != 0 || dz != 0)
                                            && random.nextFloat() <= palette.budChance()) {
                                        BlockState budState = buds.getRandomState(random);
                                        if (budState != null) {
                                            putter.put(level, pos.offset(0, 1, 0), budState);
                                        }
                                    }
                                }
                            }
                        } else {
                            BlockState shellState = shell.getRandomState(random);
                            if (shellState != null) {
                                putter.put(level, pos, shellState);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private static int clampX(BoundingBox box, int value) {
        if (value < box.minX()) {
            return box.minX();
        }
        if (value > box.maxX()) {
            return box.maxX();
        }
        return value;
    }

    private static int clampZ(BoundingBox box, int value) {
        if (value < box.minZ()) {
            return box.minZ();
        }
        if (value > box.maxZ()) {
            return box.maxZ();
        }
        return value;
    }
}
