package com.lirxowo.custommeteor.worldgen;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public final class MeteorTemplatePlacer {
    public static final ResourceLocation TEMPLATE_ID = ResourceLocation.fromNamespaceAndPath("custommeteor", "ae2_meteorite");

    private MeteorTemplatePlacer() {
    }

    public static boolean placeIfPresent(LevelAccessor level, BlockPos center, BoundingBox bounds, RandomSource random) {
        if (!(level instanceof ServerLevelAccessor serverLevelAccessor)) {
            return false;
        }

        StructureTemplateManager manager = serverLevelAccessor.getLevel().getStructureManager();
        Optional<StructureTemplate> templateOpt = manager.get(TEMPLATE_ID);
        if (templateOpt.isEmpty()) {
            return false;
        }

        StructureTemplate template = templateOpt.get();
        Vec3i size = template.getSize();
        if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) {
            return false;
        }

        BlockPos pivot = new BlockPos(size.getX() / 2, size.getY() / 2, size.getZ() / 2);
        BlockPos origin = center.subtract(pivot);

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(Rotation.NONE)
                .setRotationPivot(pivot)
                .setBoundingBox(bounds)
                .setIgnoreEntities(true)
                .setKeepLiquids(true)
                .setRandom(random);

        template.placeInWorld(serverLevelAccessor, origin, origin, settings, random, Block.UPDATE_ALL);
        return true;
    }
}
