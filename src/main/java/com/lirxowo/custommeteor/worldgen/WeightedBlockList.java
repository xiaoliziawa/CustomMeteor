package com.lirxowo.custommeteor.worldgen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public final class WeightedBlockList {
    public static final class Entry {
        private final BlockState state;
        private final int weight;
        private final boolean allowBud;

        public Entry(BlockState state, int weight, boolean allowBud) {
            this.state = state;
            this.weight = weight;
            this.allowBud = allowBud;
        }

        public BlockState state() {
            return state;
        }

        public int weight() {
            return weight;
        }

        public boolean allowBud() {
            return allowBud;
        }
    }

    private final List<Entry> entries = new ArrayList<>();
    private int totalWeight;

    public void clear() {
        entries.clear();
        totalWeight = 0;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public void add(BlockState state, int weight, boolean allowBud) {
        if (state == null || state.isAir() || weight <= 0) {
            return;
        }
        entries.add(new Entry(state, weight, allowBud));
        totalWeight += weight;
    }

    public Entry getRandomEntry(RandomSource random) {
        if (entries.isEmpty()) {
            return null;
        }
        int roll = random.nextInt(totalWeight);
        for (Entry entry : entries) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry;
            }
        }
        return entries.get(entries.size() - 1);
    }

    public BlockState getRandomState(RandomSource random) {
        Entry entry = getRandomEntry(random);
        return entry == null ? null : entry.state();
    }

    public Entry getFirstEntry() {
        return entries.isEmpty() ? null : entries.get(0);
    }

    public WeightedBlockList copy() {
        WeightedBlockList copy = new WeightedBlockList();
        for (Entry entry : entries) {
            copy.add(entry.state(), entry.weight(), entry.allowBud());
        }
        return copy;
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }
}
