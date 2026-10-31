# Tinkers' AI — 1.21.1 / NeoForge

**Primitive survival, Tinkers' Construct mob gear, and ranged combat AI.**
**原始生存、匠魂怪物装备与远程战斗 AI。**

- GitHub: https://github.com/Rhaaaaaast1/Fabrimechanus
- License: MIT
- Version: **1.2.0**
- Minecraft: **1.21.1**
- Loader: **NeoForge 21.1.x**
- Optional: **Tinkers' Construct 3.13.x**, **Mantle**

> The 1.20.1 branch is separate. The primitive survival features below are **1.21.1 only** and are not synced to 1.20.1.
> 1.20.1 是独立分支。下面的原始生存功能**只存在于 1.21.1**，不会同步到 1.20.1。

---

## Overview / 概述

Tinkers' AI adds two layers to the game:

1. **Primitive survival (1.21.1 only)** — loose rocks, plant fiber, crude axes, plank gating, and a no-empty-hand tree rule.
2. **Tinkers' mobs** — newly spawned monsters receive Tinkers' gear, use Tinkers' ranged weapons, and vanilla chest gear is replaced with tiered Tinkers' equipment when Tinkers' Construct is installed.

Tinkers' AI 在游戏里加入两层内容：

1. **原始生存（仅 1.21.1）**：石子、植物纤维、简陋斧、木板限制、禁止空手撸树。
2. **匠魂怪物**：新生成怪物获得匠魂装备、使用匠魂远程武器；安装匠魂时，原版箱子里的装备会替换为对应等级匠魂装备。

---

## Primitive survival / 原始生存

### Loose rocks / 石子

- `fabrimechanus:small_pebble` and `fabrimechanus:small_flint` are placeable ground blocks.
- They generate only on the world surface, resting directly on stone-like or gravel/sand blocks. They never float and never generate on grass or leaves.
- Both blocks have a `waterlogged` state.
- **Right-click** picks the rock up.
- **Left-click** breaks it and drops the item.
- If the supporting block is removed, the rock drops its item and disappears.

- `fabrimechanus:small_pebble` 和 `fabrimechanus:small_flint` 是可放置的地面方块。
- 只生成在世界表面，且必须直接放在石质或砂砾类方块上；不会悬空，也不会生成在草或树叶上。
- 两个方块都有 `waterlogged`（含水）状态。
- **右键**拾取。
- **左键**破坏并掉落物品。
- 底部支撑方块被破坏时，石子会直接掉落并消失。

### Shards / 碎片

Sneak-right-click a pebble or flint pebble on a configured knapping block (stone, andesite, diorite, granite, deepslate, tuff, calcite by default) to turn it into:

- `fabrimechanus:stone_shard`
- `fabrimechanus:flint_shard`

The action plays stone break particles and the item-break sound. The knapping block list is the block tag `#fabrimechanus:pebble_knapping_blocks`.

潜行右键把石子/燧石石子按在可敲击方块上（默认石头、安山岩、闪长岩、花岗岩、深板岩、凝灰岩、方解石），会变成：

- `fabrimechanus:stone_shard`
- `fabrimechanus:flint_shard`

会播放石头破坏粒子和物品损坏音效。可敲击方块由方块标签 `#fabrimechanus:pebble_knapping_blocks` 控制。

### Plant fiber / 植物纤维

Hold **right-click** with a stone shard or flint shard on short grass, fern, tall grass, or large fern. After the charge completes, the plant is cut and drops 1–2 `fabrimechanus:plant_fiber`.

Three plant fiber craft one `fabrimechanus:plant_cord`.

用石片或燧石碎片对短草、蕨、高草、大型蕨**按住右键蓄力**；蓄力完成后割下植物，掉落 1–2 个 `fabrimechanus:plant_fiber`。

3 个植物纤维合成 1 个 `fabrimechanus:plant_cord`。

### Crude axes / 简陋斧

- `fabrimechanus:stone_axe`
- `fabrimechanus:flint_axe`
- Durability: **10**
- Mining speed is close to a wooden axe.
- Charged right-click on **stripped logs/wood** saws the block into **2 planks**.
- Normal logs can still be stripped by right-clicking with the axe first.
- Both axes are in `#minecraft:axes`.

- `fabrimechanus:stone_axe`
- `fabrimechanus:flint_axe`
- 耐久：**10**
- 挖掘速度接近木斧。
- 对**去皮原木/木头**蓄力右键，会把方块锯成 **2 个木板**。
- 普通原木仍可先用斧右键去皮。
- 两把斧都加入 `#minecraft:axes`。

### Planks / 木板

- The vanilla 4-plank recipes are removed.
- A crafting table recipe converts **1 log → 2 planks**.
- A charged axe on stripped wood also gives **2 planks**.
- Planks are therefore gated behind an axe or the reduced-yield crafting recipe.

- 移除原版 4 木板配方。
- 工作台配方改为 **1 个原木 → 2 个木板**。
- 用斧对去皮木头蓄力右键同样获得 **2 个木板**。
- 因此木板必须通过斧子或低产出的工作台配方获得。

### No empty-hand tree punching / 禁止空手撸树

Wooden blocks (logs, wood, planks, stairs, slabs, fences, gates, doors, trapdoors, signs, buttons, pressure plates) require an axe.

- Empty-hand attempt: **1 damage (half a heart)**, a small knockback, and the action-bar message `你疯了?为什么要空手打树?`.
- A non-axe item cannot break these blocks either; the block simply resists.
- The block list is `#fabrimechanus:requires_axe`.

木质方块（原木、木头、木板、楼梯、台阶、栅栏、栅栏门、门、活板门、告示牌、按钮、压力板）必须使用斧类工具。

- 空手尝试：受到 **1 点伤害（半颗心）**、小击退，并显示动作栏消息 `你疯了?为什么要空手打树?`。
- 非斧类物品也无法破坏这些方块，方块会直接抵抗。
- 方块列表为 `#fabrimechanus:requires_axe`。

---

## Tinkers' mob gear / 匠魂怪物装备

When Tinkers' Construct is installed:

- Newly spawned monsters receive Tinkers' tools, weapons, and armor from config rules.
- Vanilla gear is removed only from the six equipment slots, controlled by `#fabrimechanus:stripped_monster_gear`.
- Tinkers' own mob equipment is preserved; only empty slots are filled.
- Bows, crossbows, and throwing weapons get custom ranged AI.
- Mob launchers receive infinite arrows; thrown weapons are not consumed.

安装匠魂时：

- 新生成怪物按配置获得匠魂工具、武器和护甲。
- 只从六个装备槽移除 `#fabrimechanus:stripped_monster_gear` 里的原版装备。
- 匠魂自带的怪物装备会保留，只填充空槽。
- 弓、弩、投掷武器获得自定义远程 AI。
- 怪物发射器获得无限箭；投掷武器不消耗。

### Chest loot replacement / 箱子战利品替换

When Tinkers' Construct is installed, all vanilla chest loot tables (including the spawn bonus chest) have their vanilla tools and armor replaced with Tinkers' equivalents:

| Vanilla / 原版 | Tinkers' / 匠魂 |
|---|---|
| sword / 剑 | `tconstruct:sword` |
| pickaxe / 镐 | `tconstruct:pickaxe` |
| axe / 斧 | `tconstruct:hand_axe` |
| shovel / 锹 | `tconstruct:mattock` |
| hoe / 锄 | `tconstruct:kama` |
| bow / 弓 | `tconstruct:longbow` |
| crossbow / 弩 | `tconstruct:crossbow` |
| shield / 盾 | `tconstruct:plate_shield` |
| leather armor / 皮革护甲 | `tconstruct:travelers_*` |
| metal armor / 金属护甲 | `tconstruct:plate_*` |

- Tier mapping: wood/stone/leather = 1, iron/gold = 2, diamond = 3, netherite = 4.
- A random material of the same tier is used.
- Every part of one item uses the **same material**.
- `flint_and_steel`, `shears`, and `fishing_rod` have no Tinkers' equivalent and are removed from chest loot.
- Vanilla recipes for all replaced tools/armor, plus flint and steel, shears, fishing rod, bow, crossbow, and shield, are removed.
- Disabled items show `已禁用` in their tooltip and cannot be used.
- Horse armor, turtle helmet, and wolf armor are **not** touched.

安装匠魂时，所有原版箱子战利品表（包括奖励箱）里的原版工具和护甲会替换为匠魂对应物：

- 等级映射：木/石/皮革 = 1，铁/金 = 2，钻石 = 3，下界合金 = 4。
- 随机选择同等级材料。
- 同一件物品的所有部位使用**同一种材料**。
- 打火石、剪刀、钓鱼竿没有匠魂对应物，会从箱子战利品中移除。
- 所有被替换工具/护甲，以及打火石、剪刀、钓鱼竿、弓、弩、盾的原版配方都会移除。
- 被禁用物品在 tooltip 中显示 `已禁用`，且无法使用。
- 马铠、海龟壳、狼铠**不受影响**。

If Tinkers' Construct is absent, chest loot and recipes are left untouched.

如果未安装匠魂，箱子战利品和配方保持原样。

---

## Config / 配置

### Mob gear / 怪物装备

1.21.1 path:

```text
config/fabrimechanus/mob_gear/
├── common.toml
├── minecraft/
│   ├── skeleton.toml
│   ├── zombie.toml
│   └── ... 15 files
└── compat/          (empty by default)
```

On first launch the mod creates `common.toml`, 15 vanilla mob TOML files, and an empty `compat/` folder. Existing files are never overwritten. Use `/reload` after editing.

首次启动会创建 `common.toml`、15 个原版怪物 TOML 文件和空的 `compat/` 文件夹。已有文件不会被覆盖，修改后使用 `/reload`。

`common.toml`:

```toml
# Fabrimechanus mob gear
enabled = true
log_unknown_items = true
```

One mob file:

```toml
priority = 0
mobs = ["minecraft:skeleton", "#minecraft:skeletons"]

[mainhand]
chance = 1.0

[[mainhand.items]]
item = "tconstruct:longbow"
weight = 85

[[mainhand.items]]
item = "tconstruct:sword"
weight = 2

[offhand]
chance = 0.1

[[offhand.items]]
item = "tconstruct:plate_shield"
weight = 1

[armor]
chance = 0.1

[[armor.sets]]
set = "travelers"
weight = 3

[[armor.sets]]
set = "plate"
weight = 1
```

`mainhand`, `offhand`, and `armor` are optional. `set` is `plate`, `travelers`, or `none`.

`mainhand`、`offhand`、`armor` 都是可选的。`set` 可选 `plate`、`travelers` 或 `none`。

### Material tier weights / 材料等级权重

The existing Forge config still controls the tier weights used by mob gear:

原版 Forge 配置仍控制怪物装备的材料等级权重：

```text
config/fabrimechanus-common.toml
```

```toml
[materialTierWeights]
tier0 = 25.0
tier1 = 25.0
tier2 = 30.0
tier3 = 17.0
tier4 = 3.0
```

---

## Tags / 标签

| Tag | Purpose |
|---|---|
| `#fabrimechanus:stripped_monster_gear` | Vanilla gear removed from mob equipment slots |
| `#fabrimechanus:pebble_support` | Blocks a stone pebble can rest on |
| `#fabrimechanus:flint_support` | Blocks a flint pebble can rest on |
| `#fabrimechanus:pebble_knapping_blocks` | Blocks usable for shard knapping |
| `#fabrimechanus:plant_fiber_sources` | Plants cut for fiber |
| `#fabrimechanus:requires_axe` | Wooden blocks that require an axe |

---

## Requirements / 环境

| Component | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.x |
| Java | 21 |
| Tinkers' Construct (optional) | 3.13.x |
| Mantle (optional) | matching Tinkers' |

Install on both client and server.

---

## Build / 构建

```bash
./gradlew build
```

Output:

```text
build/libs/fabrimechanus-1.2.0.jar
```

---

## License / 许可

MIT
