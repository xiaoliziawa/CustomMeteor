package com.lirxowo.custommeteor.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface AE2MeteorEvents {
    EventGroup GROUP = EventGroup.of("AE2MeteorEvent");
    EventHandler CREATE = GROUP.startup("create", () -> AE2MeteorEventJS.class);
}
