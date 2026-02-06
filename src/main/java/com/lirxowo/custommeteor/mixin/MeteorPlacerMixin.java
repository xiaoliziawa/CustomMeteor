package com.lirxowo.custommeteor.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import appeng.worldgen.meteorite.MeteoriteBlockPutter;
import appeng.worldgen.meteorite.MeteoritePlacer;
import appeng.worldgen.meteorite.PlacedMeteoriteSettings;
import com.lirxowo.custommeteor.config.CustomMeteorConfig;
import com.lirxowo.custommeteor.worldgen.KubeJSMeteorPalette;
import com.lirxowo.custommeteor.worldgen.KubeJSMeteoritePlacer;
import com.lirxowo.custommeteor.worldgen.MeteorTemplatePlacer;
import com.lirxowo.custommeteor.worldgen.MeteorPalette;
import com.lirxowo.custommeteor.worldgen.WeightedBlockList;
import com.lirxowo.custommeteor.worldgen.WeightedPalette;

@Mixin(value = MeteoritePlacer.class, remap = false)
public class MeteorPlacerMixin {
    @Shadow
    @Final
    @SuppressWarnings("remap")
    private LevelAccessor level;

    @Shadow
    @Final
    @SuppressWarnings("remap")
    private RandomSource random;

    @Shadow
    @Final
    @SuppressWarnings("remap")
    private BlockPos pos;

    @Shadow
    @Final
    @SuppressWarnings("remap")
    private BoundingBox boundingBox;

    @Shadow
    @Final
    @SuppressWarnings("remap")
    private MeteoriteBlockPutter putter;

    @Shadow
    @Final
    @SuppressWarnings("remap")
    private int x;

    @Shadow
    @Final
    @SuppressWarnings("remap")
    private int y;

    @Shadow
    @Final
    @SuppressWarnings("remap")
    private int z;

    @Shadow
    @Final
    @SuppressWarnings("remap")
    private double squaredMeteoriteSize;

    @Shadow
    @Final
    @Mutable
    @SuppressWarnings("remap")
    private BlockState skyStone;

    @Shadow
    @Final
    @Mutable
    @SuppressWarnings("remap")
    private List<BlockState> quartzBlocks;

    @Shadow
    @Final
    @Mutable
    @SuppressWarnings("remap")
    private List<BlockState> quartzBuds;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void custommeteor$applyPalette(LevelAccessor level, PlacedMeteoriteSettings settings,
            BoundingBox boundingBox, RandomSource random, CallbackInfo ci) {
        CustomMeteorConfig.MeteoriteMode mode = CustomMeteorConfig.meteoriteMode();
        if (mode == CustomMeteorConfig.MeteoriteMode.PALETTE) {
            MeteorPalette.Palette palette = MeteorPalette.resolve(random);
            this.skyStone = palette.shell();
            this.quartzBlocks = palette.core();
            this.quartzBuds = palette.buds();
            return;
        }

        if (mode != CustomMeteorConfig.MeteoriteMode.KUBEJS) {
            return;
        }
        WeightedPalette palette = KubeJSMeteorPalette.get();
        WeightedBlockList.Entry shellEntry = palette.shell().getFirstEntry();
        if (shellEntry != null) {
            this.skyStone = shellEntry.state();
        }
    }

    @Inject(method = "placeMeteoriteSkyStone", at = @At("HEAD"), cancellable = true, remap = false)
    private void custommeteor$placeTemplate(CallbackInfo ci) {
        CustomMeteorConfig.MeteoriteMode mode = CustomMeteorConfig.meteoriteMode();
        if (mode == CustomMeteorConfig.MeteoriteMode.TEMPLATE
                && MeteorTemplatePlacer.placeIfPresent(level, pos, boundingBox, random)) {
            ci.cancel();
            return;
        }
        if (mode == CustomMeteorConfig.MeteoriteMode.KUBEJS) {
            WeightedPalette palette = KubeJSMeteorPalette.get();
            if (KubeJSMeteoritePlacer.place(level, random, boundingBox, putter, x, y, z, squaredMeteoriteSize,
                    palette)) {
                ci.cancel();
            }
        }
    }
}
