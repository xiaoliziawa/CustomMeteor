package com.lirxowo.custommeteor.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure.GenerationContext;
import net.minecraft.world.level.levelgen.structure.Structure.GenerationStub;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import appeng.worldgen.meteorite.CraterType;
import appeng.worldgen.meteorite.MeteoriteStructure;
import appeng.worldgen.meteorite.fallout.FalloutMode;
import com.lirxowo.custommeteor.config.CustomMeteorConfig;
import com.lirxowo.custommeteor.config.MeteorPaletteConfig;
import com.lirxowo.custommeteor.kubejs.KubeJSMeteorTerrainHooks;
import com.lirxowo.custommeteor.mixin.accessor.MeteoriteStructurePieceInvoker;
import net.minecraftforge.fml.ModList;

@Mixin(value = MeteoriteStructure.class, remap = false)
public class MeteoriteStructureMixin {
    @Inject(method = "findGenerationPoint", at = @At("HEAD"), cancellable = true, remap = false)
    private void custommeteor$disableVanillaMeteorite(GenerationContext context,
            CallbackInfoReturnable<Optional<GenerationStub>> cir) {
        if (CustomMeteorConfig.disableVanillaMeteorite()) {
            cir.setReturnValue(Optional.empty());
        }
    }

    @Redirect(method = "generatePieces", remap = false, at = @At(value = "NEW",
            target = "Lappeng/worldgen/meteorite/MeteoriteStructurePiece;"))
    private static appeng.worldgen.meteorite.MeteoriteStructurePiece custommeteor$kubejsTerrain(BlockPos pos,
            float meteoriteRadius, CraterType craterType, FalloutMode falloutMode, boolean pureCrater,
            boolean craterLake, StructurePiecesBuilder piecesBuilder, GenerationContext context) {
        CustomMeteorConfig.MeteoriteMode mode = CustomMeteorConfig.meteoriteMode();
        if (mode == CustomMeteorConfig.MeteoriteMode.KUBEJS && ModList.get().isLoaded("kubejs")) {
            var overrides = KubeJSMeteorTerrainHooks.apply(context, pos, craterType, falloutMode, pureCrater,
                    craterLake);
            craterType = overrides.craterType();
            falloutMode = overrides.falloutMode();
            pureCrater = overrides.pureCrater();
            craterLake = overrides.craterLake();
        } else if (mode == CustomMeteorConfig.MeteoriteMode.PALETTE) {
            var config = MeteorPaletteConfig.get();
            if (config.craterType() != null) {
                craterType = config.craterType();
            }
            if (config.falloutMode() != null) {
                falloutMode = config.falloutMode();
            }
            if (config.pureCrater() != null) {
                pureCrater = config.pureCrater();
            }
            if (config.craterLake() != null) {
                craterLake = config.craterLake();
            }
        }
        return MeteoriteStructurePieceInvoker.custommeteor$init(pos, meteoriteRadius, craterType, falloutMode,
                pureCrater, craterLake);
    }
}
