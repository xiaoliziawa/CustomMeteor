# custommeteor 陨石配置说明

本模组支持三种陨石生成模式：

- `TEMPLATE`：如果存在结构模板 `custommeteor:ae2_meteorite` 就使用模板生成。
- `PALETTE`：使用 JSON/Tag 调色板选择陨石方块。
- `KUBEJS`：通过 KubeJS startup 脚本用“权重概率”控制生成。

在配置中切换模式：

```
config/custommeteor-common.toml
[meteorite]
mode = "PALETTE"
```

## Palette JSON（PALETTE 模式）

第一次启动游戏/服务器后会自动生成：

- `config/custommeteor/meteorite_palette.json`

JSON 字段含义：

- `shell`：陨石外壳（天陨石主体）。
- `core`：中心小空腔里的方块（赛特斯石英/母岩）。
- `buds`：在核心方块上方生成的芽（自动朝上；中心空腔 70% 概率）。

如果列表为空或有无效方块 ID，会回退到以下标签：

- `custommeteor:meteorite_shell`
- `custommeteor:meteorite_core`
- `custommeteor:meteorite_buds`

如果标签也为空，则回退到 AE2 默认方块。

### JSON 示例

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

## KubeJS（KUBEJS 模式）

当 `mode = "KUBEJS"` 时，会忽略 JSON 与标签，改由 KubeJS 脚本控制。

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

下面方法的 `blockId` 可以是字符串（`"modid:block"`）、方块对象或 BlockState。
`weight` 是相对权重（数值越大出现概率越高）。

- `event.shell(blockId, weight = 1)`
  - 添加外壳方块候选，每个外壳位置都会独立随机。
- `event.core(blockId, weight = 1)`
  - 添加核心方块候选，允许在其上方生成芽。
- `event.coreNoBud(blockId, weight = 1)`
  - 添加核心方块候选，但不会在其上方生成芽。
- `event.buds(blockId, weight = 1)`
  - 添加芽方块候选，芽会自动朝上。
- `event.budChance(chance)`
  - 设置生成芽的概率（0.0–1.0）。只对允许长芽的核心方块生效。
- `event.clearShell()`, `event.clearCore()`, `event.clearBuds()`, `event.clearAll()`
  - 清空对应列表，用于完全替换默认内容。

说明：
- `coreNoBud` 表示该核心方块不会生成芽。
- 权重数值越大，出现概率越高。
- 请保留 `ae2:sky_stone_block` 在 shell 中，保证 AE2 罗盘仍能定位陨石。

## 注意事项

- 修改后需要重启才生效。
- `TEMPLATE` 模式会忽略 JSON 和 KubeJS。
