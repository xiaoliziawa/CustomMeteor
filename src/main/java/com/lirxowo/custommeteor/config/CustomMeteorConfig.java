package com.lirxowo.custommeteor.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class CustomMeteorConfig {
    public static final ForgeConfigSpec SPEC;
    public static final Common COMMON;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        SPEC = builder.build();
    }

    private CustomMeteorConfig() {
    }

    public static MeteoriteMode meteoriteMode() {
        return COMMON.meteoriteMode.get();
    }

    public enum MeteoriteMode {
        TEMPLATE,
        PALETTE,
        KUBEJS
    }

    public static final class Common {
        public final ForgeConfigSpec.EnumValue<MeteoriteMode> meteoriteMode;

        private Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Custom meteorite settings").push("meteorite");
            meteoriteMode = builder
                    .comment("TEMPLATE: use structure template custommeteor:ae2_meteorite if present.",
                            "PALETTE: use block tags to customize meteorite composition.",
                            "KUBEJS: read meteorite palette from KubeJS startup scripts (AE2MeteorEvent.create).")
                    .defineEnum("mode", MeteoriteMode.TEMPLATE);
            builder.pop();
        }
    }
}
