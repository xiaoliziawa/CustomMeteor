package com.lirxowo.custommeteor.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.ScriptType;

import com.lirxowo.custommeteor.worldgen.KubeJSMeteorPalette;
import com.lirxowo.custommeteor.worldgen.WeightedPalette;

public class AE2MeteorKubeJSPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        AE2MeteorEvents.GROUP.register();
    }

    @Override
    public void initStartup() {
        AE2MeteorEventJS event = new AE2MeteorEventJS();
        AE2MeteorEvents.CREATE.post(ScriptType.STARTUP, event);
        WeightedPalette palette = event.build();
        KubeJSMeteorPalette.set(palette);
    }
}
