package com.lirxowo.custommeteor;


import com.lirxowo.custommeteor.config.CustomMeteorConfig;
import com.lirxowo.custommeteor.config.MeteorPaletteConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(CustomMeteor.MODID)
public class CustomMeteor {

    public static final String MODID = "custommeteor";

    public CustomMeteor(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, CustomMeteorConfig.SPEC);
        MeteorPaletteConfig.get();
    }
}
