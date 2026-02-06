package com.lirxowo.custommeteor.worldgen;

public final class WeightedPalette {
    private final WeightedBlockList shell;
    private final WeightedBlockList core;
    private final WeightedBlockList buds;
    private final float budChance;

    public WeightedPalette(WeightedBlockList shell, WeightedBlockList core, WeightedBlockList buds, float budChance) {
        this.shell = shell;
        this.core = core;
        this.buds = buds;
        this.budChance = budChance;
    }

    public WeightedBlockList shell() {
        return shell;
    }

    public WeightedBlockList core() {
        return core;
    }

    public WeightedBlockList buds() {
        return buds;
    }

    public float budChance() {
        return budChance;
    }
}
