package com.lirxowo.custommeteor.kubejs;

import com.mojang.logging.LogUtils;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.slf4j.Logger;

import com.lirxowo.custommeteor.worldgen.KubeJSMeteorPalette;
import com.lirxowo.custommeteor.worldgen.WeightedBlockList;
import com.lirxowo.custommeteor.worldgen.WeightedPalette;

public class AE2MeteorEventJS extends EventJS {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final WeightedBlockList shell = new WeightedBlockList();
    private final WeightedBlockList core = new WeightedBlockList();
    private final WeightedBlockList buds = new WeightedBlockList();
    private float budChance = 0.7f;

    public AE2MeteorEventJS() {
        WeightedPalette defaults = KubeJSMeteorPalette.defaultPalette();
        copyDefaults(defaults);
    }

    public void clearShell() {
        shell.clear();
    }

    public void clearCore() {
        core.clear();
    }

    public void clearBuds() {
        buds.clear();
    }

    public void clearAll() {
        shell.clear();
        core.clear();
        buds.clear();
    }

    public void shell(Block block) {
        shell(block, 1);
    }

    public void shell(Block block, int weight) {
        addBlock(shell, block, weight, true, "shell");
    }

    public void core(Block block) {
        core(block, 1);
    }

    public void core(Block block, int weight) {
        addBlock(core, block, weight, true, "core");
    }

    public void coreNoBud(Block block) {
        coreNoBud(block, 1);
    }

    public void coreNoBud(Block block, int weight) {
        addBlock(core, block, weight, false, "core");
    }

    public void buds(Block block) {
        buds(block, 1);
    }

    public void buds(Block block, int weight) {
        addBlock(buds, block, weight, true, "buds");
    }

    public void budChance(double chance) {
        if (Double.isNaN(chance)) {
            return;
        }
        this.budChance = (float) Math.max(0.0, Math.min(1.0, chance));
    }

    public WeightedPalette build() {
        WeightedPalette input = new WeightedPalette(shell.copy(), core.copy(), buds.copy(), budChance);
        return KubeJSMeteorPalette.mergeWithDefaults(input);
    }

    private void copyDefaults(WeightedPalette defaults) {
        for (WeightedBlockList.Entry entry : defaults.shell().entries()) {
            shell.add(entry.state(), entry.weight(), entry.allowBud());
        }
        for (WeightedBlockList.Entry entry : defaults.core().entries()) {
            core.add(entry.state(), entry.weight(), entry.allowBud());
        }
        for (WeightedBlockList.Entry entry : defaults.buds().entries()) {
            buds.add(entry.state(), entry.weight(), entry.allowBud());
        }
        budChance = defaults.budChance();
    }

    private void addBlock(WeightedBlockList list, Object input, int weight, boolean allowBud, String section) {
        BlockState state = resolveBlockState(input);
        if (state == null) {
            LOGGER.warn("KubeJS meteor {} entry is invalid: {}", section, input);
            return;
        }
        if ("buds".equals(section)) {
            state = withFacingUp(state);
        }
        list.add(state, weight, allowBud);
    }

    private BlockState resolveBlockState(Object input) {
        if (input instanceof BlockState state) {
            return state;
        }
        if (input instanceof Block block) {
            return block.defaultBlockState();
        }
        if (input instanceof ResourceLocation id) {
            return blockStateFromId(id);
        }
        if (input != null) {
            ResourceLocation id = ResourceLocation.tryParse(input.toString());
            if (id != null) {
                return blockStateFromId(id);
            }
        }
        return null;
    }

    private BlockState blockStateFromId(ResourceLocation id) {
        if (!BuiltInRegistries.BLOCK.containsKey(id)) {
            return null;
        }
        Block block = BuiltInRegistries.BLOCK.get(id);
        BlockState state = block.defaultBlockState();
        return state.isAir() ? null : state;
    }

    private BlockState withFacingUp(BlockState state) {
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.setValue(BlockStateProperties.FACING, Direction.UP);
        }
        return state;
    }
}
