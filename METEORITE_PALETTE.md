# custommeteor Meteorite Configuration

This mod supports three meteorite generation modes:

- `TEMPLATE`: use `custommeteor:ae2_meteorite` structure template if present.
- `PALETTE`: use JSON/tag palette to pick blocks.
- `KUBEJS`: use KubeJS startup scripts with weighted probabilities.

Set the mode here:

```
config/custommeteor-common.toml
[meteorite]
mode = "PALETTE"
```

## Palette JSON (PALETTE mode)

The JSON file is auto-created after the first game/server start:

- `config/custommeteor/meteorite_palette.json`

The JSON controls which blocks are used for different parts of the meteorite:

- `shell`: The outer shell of the meteorite (the skystone body).
- `core`: Blocks used inside the small center chamber (budding quartz / quartz blocks).
- `buds`: Bud blocks placed on top of core blocks (placed facing up; 70% chance in the center chamber).

If a list is empty or contains invalid IDs, the mod falls back to the block tags:

- `custommeteor:meteorite_shell`
- `custommeteor:meteorite_core`
- `custommeteor:meteorite_buds`

If those tags are also empty, it falls back to AE2 defaults.

### Example JSON

```json
{
  "shell": [
    "ae2:sky_stone_block"
  ],
  "core": [
    "ae2:quartz_block",
    "ae2:damaged_budding_quartz",
    "ae2:chipped_budding_quartz",
    "ae2:flawed_budding_quartz",
    "ae2:flawless_budding_quartz"
  ],
  "buds": [
    "ae2:small_quartz_bud",
    "ae2:medium_quartz_bud",
    "ae2:large_quartz_bud"
  ]
}
```

## KubeJS (KUBEJS mode)

When `mode = "KUBEJS"`, the JSON and tags are ignored. Instead, define the
meteorite palette in a startup script:

Path: `kubejs/startup_scripts/ae2_meteor.js`

```js
AE2MeteorEvent.create(event => {
  // weights for shell/core/buds
  event.shell('ae2:sky_stone_block', 8)
  event.shell('minecraft:deepslate', 2)

  event.coreNoBud('ae2:quartz_block', 4)
  event.core('ae2:flawless_budding_quartz', 1)

  event.buds('ae2:small_quartz_bud', 3)
  event.buds('ae2:medium_quartz_bud', 2)
  event.buds('ae2:large_quartz_bud', 1)

  event.budChance(0.7)
})
```

### Event API reference

All methods below accept `blockId` as a string (`"modid:block"`), a Block, or a BlockState.
`weight` is a relative probability (higher = more common).

- `event.shell(blockId, weight = 1)`
  - Adds a shell block candidate. Each shell position is rolled independently.
- `event.core(blockId, weight = 1)`
  - Adds a core block candidate and allows buds to spawn above it.
- `event.coreNoBud(blockId, weight = 1)`
  - Adds a core block candidate that will **not** spawn buds above it.
- `event.buds(blockId, weight = 1)`
  - Adds a bud block candidate. Buds are auto-oriented upward.
- `event.budChance(chance)`
  - Chance (0.0–1.0) to place a bud above a core block that allows buds.
- `event.clearShell()`, `event.clearCore()`, `event.clearBuds()`, `event.clearAll()`
  - Clears the corresponding list so you can fully replace defaults.

Notes:
- `coreNoBud` marks a core block as "no bud allowed".
- All lists are weighted; higher numbers = higher probability.
- Keep `ae2:sky_stone_block` in `shell` so AE2 compass can still track meteors.

## Notes

- Changes require a restart to take effect.
- `TEMPLATE` mode ignores both JSON and KubeJS.
