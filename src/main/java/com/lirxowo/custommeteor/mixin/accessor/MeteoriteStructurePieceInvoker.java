package com.lirxowo.custommeteor.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;

import appeng.worldgen.meteorite.CraterType;
import appeng.worldgen.meteorite.MeteoriteStructurePiece;
import appeng.worldgen.meteorite.fallout.FalloutMode;

@Mixin(MeteoriteStructurePiece.class)
public interface MeteoriteStructurePieceInvoker {
    @Invoker("<init>")
    static MeteoriteStructurePiece custommeteor$init(BlockPos pos, float meteoriteRadius, CraterType craterType,
            FalloutMode falloutMode, boolean pureCrater, boolean craterLake) {
        throw new AssertionError("Mixin failed to apply.");
    }
}
