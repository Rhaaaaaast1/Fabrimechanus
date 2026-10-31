# Tinkers' AI — Changelog / 更新日志

## 1.2.0 — 2026-10-31 (1.21.1 only)

### Post-release fixes / 后续修复

**English**

- Added the missing root item models, fixing the item and block-item textures.
- Empty-hand plant pulling now charges for 20 ticks and uses a both-arms-forward pose; shards no longer cut grass.
- The no-empty-hand tree rule now also fires on left-click, with 1 damage, small knockback, and the action-bar message.
- Pebble generation now uses No Tree Punching-style surface supports (grass, dirt, gravel, sand, etc.), scans down up to 8 blocks for stone, and still only runs at the surface.
- Armor distribution now clears all four slots (including Tinkers' armor) and uses vanilla-style stop chances: first piece always, later pieces stop at 25% normal / 10% hard.
- Mob gear config is now JSON at `config/fabrimechanus/`; `materialTier` selects the random material tier per mob.
- Updated JEI text for empty-hand plant pulling.

**中文**

- 补齐物品根模型，修复物品和方块物品材质。
- 空手拔草现在蓄力 20 tick，并显示双手前伸姿势；石片不再用于割草。
- 禁止空手撸树同时监听左键，触发 1 点伤害、小击退和动作栏消息。
- 石子生成改用参考模组的地表支撑（草、泥土、砂砾、沙子等），向下扫描最多 8 格寻找石头，仍然只在地表生成。
- 护甲改为先清空四个槽（包括匠魂盔甲），再按原版风格分件：第一件必出，后续普通 25% / 困难 10% 停止。
- 怪物装备配置改为 `config/fabrimechanus/` 下的 JSON；每个怪物用 `materialTier` 指定随机材料等级。
- 更新 JEI 的空手拔草说明。

### Primitive survival / 原始生存

**English**

- Added `small_pebble` and `small_flint` as waterloggable surface blocks.
- World generation uses No Tree Punching-style surface supports (grass, dirt, gravel, sand, terracotta, stone, etc.) and scans down up to 8 blocks for stone; it only runs at the surface and never floats.
- Right-click picks up a rock; left-click drops it; removing the support drops it as well.
- Added `stone_shard` and `flint_shard`; sneak-right-click a rock on configured knapping blocks to convert it, with stone particles and the item-break sound.
- Added optional JEI information pages for the rocks, shards, fiber, cord, and axes.
- Added `plant_fiber` and `plant_cord`; empty-hand right-click pulls grass for 1–2 fiber, and 3 fiber craft 1 cord.
- Added `stone_axe` and `flint_axe` with durability 10 and wooden-axe-like speed.
- Vanilla 4-plank recipes are removed; 1 log now crafts 2 planks, and a charged axe on stripped wood produces 2 planks.
- Wooden blocks require an axe. Empty-hand attempts deal 1 damage, small knockback, and show `你疯了?为什么要空手打树?`.

**中文**

- 新增可含水的表面方块 `small_pebble` 和 `small_flint`。
- 生成使用参考模组的地表支撑（草、泥土、砂砾、沙子、陶瓦、石头等），向下最多扫描 8 格寻找石头；只在地表生成，不会悬空。
- 右键拾取，左键掉落；支撑方块被破坏时也会掉落。
- 新增 `stone_shard` 和 `flint_shard`；潜行右键把石子按在可敲击方块上转换，带石头粒子和物品损坏音效。
- 为石子、碎片、纤维、植物绳和斧子添加可选的 JEI 信息页。
- 新增 `plant_fiber` 和 `plant_cord`；空手右键拔草获得 1–2 个纤维，3 个纤维合成 1 个植物绳。
- 新增耐久 10、速度接近木斧的 `stone_axe` 和 `flint_axe`。
- 移除原版 4 木板配方；1 个原木合成 2 个木板，斧子对去皮木头蓄力右键也获得 2 个木板。
- 木质方块必须使用斧类工具。空手尝试造成 1 点伤害、小击退，并显示 `你疯了?为什么要空手打树?`。

### Config / 配置

**English**

- Mob gear config is JSON under `config/fabrimechanus/` (no `mob_gear` subfolder).
- Layout: `common.json`, `minecraft/*.json`, and an empty `compat/`.
- First launch writes 15 vanilla mob files; existing files are never overwritten.
- `/reload` reloads the JSON rules.
- Each mob JSON can set `materialTier` (0 = global weights, 1-4 = fixed tier); materials are randomized within that tier.
- The old `config/fabrimechanus/mob_gear/` folder and TOML files are no longer read.

**中文**

- 怪物装备配置改为 `config/fabrimechanus/` 下的 JSON（不再有 `mob_gear` 子文件夹）。
- 结构：`common.json`、`minecraft/*.json` 和空的 `compat/`。
- 首次启动写入 15 个原版怪物文件；已有文件不会被覆盖。
- `/reload` 重新读取 JSON 规则。
- 每个怪物 JSON 可以设置 `materialTier`（0 = 全局权重，1-4 = 固定等级）；材料在该等级内随机。
- 不再读取旧的 `config/fabrimechanus/mob_gear/` 文件夹和 TOML 文件。

### Chest loot and recipes / 箱子战利品与配方

**English**

- When Tinkers' Construct is installed, all vanilla chest loot tables (including the spawn bonus chest) replace vanilla tools/armor with Tinkers' equivalents.
- Tier mapping: wood/stone/leather = 1, iron/gold = 2, diamond = 3, netherite = 4.
- Each item uses one random material of the matching tier for all parts.
- Vanilla recipes for replaced gear, flint and steel, shears, fishing rod, bow, crossbow, and shield are removed.
- Disabled items show `已禁用` and cannot be used.
- Horse armor, turtle helmet, and wolf armor are untouched.
- Without Tinkers' Construct, loot and recipes are left alone.

**中文**

- 安装匠魂时，所有原版箱子战利品表（包括奖励箱）会把原版工具/护甲替换为匠魂对应物。
- 等级映射：木/石/皮革 = 1，铁/金 = 2，钻石 = 3，下界合金 = 4。
- 每件物品的所有部位使用同等级的一种随机材料。
- 移除被替换装备，以及打火石、剪刀、钓鱼竿、弓、弩、盾的原版配方。
- 被禁用物品显示 `已禁用`，且无法使用。
- 马铠、海龟壳、狼铠不受影响。
- 未安装匠魂时，战利品与配方保持原样。

### File / 文件

| Item | Value |
|---|---|
| Jar | `fabrimechanus-1.2.0.jar` |
| Size | 141,863 bytes |
| SHA-256 | `46E1E88982648BB9C816562658BD3FDC31F8D65E2670D882A533729D5B0C5856` |
| Loader | NeoForge 21.1.x |
| Optional | Tinkers' Construct 3.13.x, Mantle |

---

## 1.1.1 — 2026-10-31

### 1.21.1 / NeoForge

**English**

- Mob gear rules moved from datapacks to `config/fabrimechanus/mob_gear/`.
- First launch generated 15 vanilla mob configs.
- `minecraft/` held vanilla mobs; `compat/` held modded mobs.
- Rules reloaded with `/reload`; existing files were never overwritten.
- The old `data/<namespace>/fabrimechanus/mob_gear/` was no longer read.
- No vanilla or Tinkers' loot table changes; chest loot was untouched.
- Vanilla equipment was removed only from newly spawned monsters' equipment slots.

**中文**

- 怪物装备规则从数据包迁移到 `config/fabrimechanus/mob_gear/`。
- 首次启动生成 15 个原版怪物配置。
- `minecraft/` 放原版怪物，`compat/` 放其他模组怪物。
- 规则支持 `/reload` 重载，已有文件不会被覆盖。
- 不再读取旧的 `data/<namespace>/fabrimechanus/mob_gear/`。
- 不修改原版或匠魂战利品表；箱子战利品不受影响。
- 只从新生成怪物的装备槽移除原版装备。

### 1.20.1 / Forge

**English**

- Mob gear rules moved to `config/tinkersai/mob_gear/`; 14 vanilla mob files (1.20.1 has no bogged).
- The 1.21.1 primitive survival features are **not** included in 1.20.1.

**中文**

- 怪物装备规则迁移到 `config/tinkersai/mob_gear/`；14 个原版怪物文件（1.20.1 没有 bogged）。
- 1.21.1 的原始生存功能**不会**同步到 1.20.1。

---

## 1.1.0 — Base release / 基础版本

**English**

- Newly spawned monsters received Tinkers' Construct tools/armor in all six equipment slots.
- Each tool part rolled its material independently; tier weights were configurable.
- Ranged AI for Tinkers' bows, crossbows, and throwing weapons.
- Mobs had infinite arrows; thrown weapons were not consumed and lost no durability.

**中文**

- 新生成怪物在六个装备槽中获得匠魂工具/护甲。
- 每件工具的各部件材料独立随机；材料等级权重可配置。
- 匠魂弓、弩、投掷武器的怪物远程 AI。
- 怪物无限箭；投掷武器不消耗、不扣耐久。
