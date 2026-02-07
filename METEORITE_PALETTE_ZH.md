# custommeteor 陨石配置说明

本模组支持三种陨石生成模式：

- `TEMPLATE`：如果存在结构模板 `custommeteor:ae2_meteorite`，则使用模板生成。
- `PALETTE`：使用 JSON/Tag 调色板选择陨石方块。
- `KUBEJS`：通过 KubeJS startup 脚本按权重生成。

在配置中切换模式：

```
config/custommeteor-common.toml
[meteorite]
mode = "PALETTE"
```

## Palette JSON（PALETTE 模式）

第一次启动游戏/服务端会自动生成：

- `config/custommeteor/meteorite_palette.json`

JSON 字段含义：

- `shell`：陨石外壳（天陨石主体，支持权重）。
- `core`：允许长芽的核心方块（支持权重）。
- `coreNoBud`：不会长芽的核心方块（支持权重）。
- `buds`：芽方块（自动朝上，支持权重）。
- `budChance`：核心方块上方生成芽的概率（0.0-1.0）。
- `craterType`：可选，覆盖陨石坑类型（与 KubeJS 地形事件同枚举）。
- `falloutMode`：可选，覆盖坠落地形模式（与 KubeJS 地形事件同枚举）。
- `pureCrater`：可选，`true` 保留坑内填充不被坍塌覆盖；`false` 禁用该行为。
- `craterLake`：可选，`true` 强制生成水坑；`false` 禁用水坑。

权重写法：

- 推荐对象格式：`{ "id": "modid:block", "weight": 8 }`
- 兼容旧写法：`"modid:block"`（默认 `weight = 1`）

如果列表为空或包含无效 ID，会回退到以下标签：

- `custommeteor:meteorite_shell`
- `custommeteor:meteorite_core`
- `custommeteor:meteorite_buds`

如果标签也为空，则回退到 AE2 默认值。

### JSON 示例

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

## KubeJS（KUBEJS 模式）

当 `mode = "KUBEJS"` 时，忽略 JSON 和标签，改用 KubeJS 脚本控制。
脚本路径：`kubejs/startup_scripts/ae2_meteor.js`

```js
AE2MeteorEvent.create(event => {
  // shell/core/buds 都是权重概率
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

### Event API 说明

`blockId` 使用字符串（`"modid:block"`）。
`weight` 是相对权重（数值越大，出现概率越高）。

- `event.shell(blockId, weight = 1)`
  - 添加外壳候选方块，外壳位置独立抽取。
- `event.core(blockId, weight = 1)`
  - 添加核心候选方块，并允许在其上方生成芽。
- `event.coreNoBud(blockId, weight = 1)`
  - 添加核心候选方块，但不生成芽。
- `event.buds(blockId, weight = 1)`
  - 添加芽候选方块（自动朝上）。
- `event.budChance(chance)`
  - 设置生成芽的概率（0.0-1.0），仅对允许长芽的核心生效。
- `event.clearShell()`, `event.clearCore()`, `event.clearBuds()`, `event.clearAll()`
  - 清空对应列表，用于完全替换默认内容。

说明：
- `coreNoBud` 表示该核心方块不会长芽。
- 权重越大，出现概率越高。
- 请保留 `ae2:sky_stone_block` 在 `shell` 中，确保 AE2 罗盘仍能定位陨石。

### 地形/陨石坑覆盖（可按生物群系判断）

第二个事件用于修改陨石坑和坠落改造：

```js
AE2MeteorEvent.terrain(event => {
  // 生物群系 ID 或 Tag 判断
  if (event.isBiome('minecraft:desert') || event.isBiome('#forge:is_sandy')) {
    event.falloutMode('SAND') // 沙地坠落有玻璃
    event.craterType('NORMAL')
  }

  // 模组生物群系示例
  if (event.isBiome('modid:volcanic_wastes')) {
    event.craterType('LAVA')
    event.falloutMode('DEFAULT')
  }

  // 可选开关
  // event.pureCrater(true)
  // event.craterLake(false)
})
```

地形事件 API：

- `event.biomeId()` -> 当前生物群系 ID 字符串。
- `event.isBiome(idOrTag)` -> 按 ID 或 Tag 判断（如 `"modid:biome"` 或 `"#forge:is_sandy"`）。
- `event.craterType(value)` -> 控制**陨石坑内部**，与外壳无关。
- `event.falloutMode(value)` -> 控制**陨石坑周围地形改造**。
- `event.pureCrater(boolean)` -> 是否保留坑内填充，不被坍塌覆盖。
- `event.craterLake(boolean)` -> 是否强制生成水坑。

CraterType 取值：

- `NONE`：不挖坑。
- `NORMAL`：普通坑（空气挖空）。
- `LAVA`：岩浆坑。
- `OBSIDIAN`：黑曜石坑。
- `WATER`：水坑。
- `SNOW`：雪坑。
- `ICE`：冰坑。

FalloutMode 取值：

- `DEFAULT`：标准碎石/泥土坠落。
- `SAND`：沙地坠落（可能出现玻璃）。
- `TERRACOTTA`：红土地形坠落（陶瓦为主）。
- `ICE_SNOW`：冰雪地形坠落。
- `NONE`：不做坠落改造（建议配合 `craterType("NONE")`）。

## 注意事项

- 修改后需重启才能生效。
- `TEMPLATE` 模式会忽略 JSON 和 KubeJS。
