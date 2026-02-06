package com.lirxowo.custommeteor;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import com.lirxowo.custommeteor.config.CustomMeteorConfig;
import com.lirxowo.custommeteor.config.MeteorPaletteConfig;

@Mod(CustomMeteor.MODID)
public class CustomMeteor {

    public static final String MODID = "custommeteor";

    public CustomMeteor() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CustomMeteorConfig.SPEC);
        MeteorPaletteConfig.get();
    }
}
