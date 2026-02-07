# custommeteor Meteorite Configuration

This mod supports three meteorite generation modes:

- `TEMPLATE`: use `custommeteor:ae2_meteorite` structure template if present.
- `PALETTE`: use JSON/tag palette with weighted entries.
- `KUBEJS`: use KubeJS startup scripts with weighted probabilities.

Set the mode here:

```toml
config/custommeteor-common.toml
[meteorite]
mode = "PALETTE"
```

## Palette JSON (PALETTE mode)

The JSON file is auto-created after first startup:

- `config/custommeteor/meteorite_palette.json`

PALETTE mode now uses the same placement logic as KubeJS mode:

- weighted `shell`
- weighted `core` (allows buds)
- weighted `coreNoBud` (never grows buds)
- weighted `buds`
- configurable `budChance`
- optional `craterType` / `falloutMode` overrides

### JSON format

Use object entries with explicit weight:

```json
{ "id": "ae2:sky_stone_block", "weight": 8 }
```

If `weight` is omitted, it defaults to `1`.

Full example:

```json
{
  "shell": [
    { "id": "ae2:sky_stone_block", "weight": 8 },
    { "id": "minecraft:deepslate", "weight": 2 }
  ],
  "core": [
    { "id": "ae2:flawless_budding_quartz", "weight": 1 },
    { "id": "ae2:flawed_budding_quartz", "weight": 2 }
  ],
  "coreNoBud": [
    { "id": "ae2:quartz_block", "weight": 4 }
  ],
  "buds": [
    { "id": "ae2:small_quartz_bud", "weight": 3 },
    { "id": "ae2:medium_quartz_bud", "weight": 2 },
    { "id": "ae2:large_quartz_bud", "weight": 1 }
  ],
  "budChance": 0.7,
  "craterType": "NORMAL",
  "falloutMode": "SAND",
  "pureCrater": true,
  "craterLake": false
}
```

### Fallback rules

If a section is empty or has invalid IDs, the mod falls back to tags:

- `custommeteor:meteorite_shell`
- `custommeteor:meteorite_core`
- `custommeteor:meteorite_buds`

If tags are also empty, it falls back to AE2 defaults.

Legacy compatibility:

- Old `core` string-only arrays are still supported.
- If `coreNoBud` is missing and legacy `core` is used, the first `core` entry is treated as `coreNoBud`.

### Optional terrain fields in JSON

You can define terrain behavior directly in `meteorite_palette.json`:

- `"craterType": "NORMAL"`
- `"falloutMode": "SAND"`
- `"pureCrater": true`
- `"craterLake": false`

These use the same enum names as the KubeJS terrain event.

`craterType` values:

- `NONE`, `NORMAL`, `LAVA`, `OBSIDIAN`, `WATER`, `SNOW`, `ICE`

`falloutMode` values:

- `NONE`, `DEFAULT`, `SAND`, `TERRACOTTA`, `ICE_SNOW`

`pureCrater` / `craterLake` values:

- `true` or `false`
- Use `null` or omit the field to keep AE2 defaults

## KubeJS (KUBEJS mode)

When `mode = "KUBEJS"`, JSON and tags are ignored.

Path: `kubejs/startup_scripts/ae2_meteor.js`

```js
AE2MeteorEvent.create(event => {
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

`weight` is a relative probability (higher = more common).

- `event.shell(blockId, weight = 1)`
- `event.core(blockId, weight = 1)`
- `event.coreNoBud(blockId, weight = 1)`
- `event.buds(blockId, weight = 1)`
- `event.budChance(chance)`
- `event.clearShell()`, `event.clearCore()`, `event.clearBuds()`, `event.clearAll()`

## Terrain/crater overrides (biome-aware)

```js
AE2MeteorEvent.terrain(event => {
  if (event.isBiome('minecraft:desert') || event.isBiome('#forge:is_sandy')) {
    event.falloutMode('SAND')
    event.craterType('NORMAL')
  }
})
```

Terrain event API:

- `event.biomeId()`
- `event.isBiome(idOrTag)`
- `event.craterType(value)`
- `event.falloutMode(value)`
- `event.pureCrater(boolean)`
- `event.craterLake(boolean)`

## Notes

- Keep `ae2:sky_stone_block` in shell entries so AE2 compass can still track meteors.
- Changes require a restart to take effect.
- `TEMPLATE` mode ignores JSON and KubeJS.
