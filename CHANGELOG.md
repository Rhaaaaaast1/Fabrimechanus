# Tinkers' AI — Changelog / 更新日志

## 1.2.1 — 2026-10-31 (1.21.1 only)

### Fixes / 修复

**English**

- Added the missing root item models, fixing the missing item and block-item textures for all primitive items.
- Cleaned the loose-rock block model names so the placed block renders correctly.
- Plant fiber is now pulled by **empty-hand right-click**, not by a shard. The shard is only a crafting material.
- The no-empty-hand tree rule now triggers on left-click as well as break speed, with the 1-damage, small knockback, and `你疯了?为什么要空手打树?` action-bar message.
- Added vanilla wooden tag fallbacks so logs, planks, stairs, slabs, fences, gates, doors, trapdoors, signs, buttons, and pressure plates are covered even if the custom tag is overridden.
- Pebble world generation now uses count 5, `WORLD_SURFACE`, and no extra random rejection, matching the referenced No Tree Punching placement more closely.
- Updated JEI text to describe empty-hand grass pulling.

**中文**

- 补齐物品根模型，修复所有原始物品和方块物品的材质缺失。
- 清理石子方块模型名称，修复放置后的方块纹理。
- 拔草改为**空手右键**，不再使用石片；石片只作为合成材料。
- 禁止空手撸树现在同时监听左键和破坏速度，触发 1 点伤害、小击退和 `你疯了?为什么要空手打树?` 动作栏消息。
- 增加原版木质标签兜底，即使自定义标签被覆盖，也能覆盖原木、木板、楼梯、台阶、栅栏、栅栏门、门、活板门、告示牌、按钮和压力板。
- 石子生成改为 count 5、`WORLD_SURFACE`，并去掉额外随机拒绝，更接近参考模组的放置规则。
- 更新 JEI 文本，说明空手拔草。

### File / 文件

| Item | Value |
|---|---|
| Jar | `fabrimechanus-1.2.1.jar` |
| Size | 125,791 bytes |
| SHA-256 | `AB3270CB19CA82C0D5EB7999165FA03684AFB16360ED46644BBD4D6C7F40AAD6` |
| Loader | NeoForge 21.1.x |
| Optional | Tinkers' Construct 3.13.x, Mantle |

---

## 1.2.0 — 2026-10-31 (1.21.1 only)

### Primitive survival / 原始生存

**English**

- Added `small_pebble` and `small_flint` as waterloggable surface blocks.
- World generation places them only on valid stone/gravel/sand supports, never floating, never on grass or leaves.
- Right-click picks up a rock; left-click drops it; removing the support drops it as well.
- Added `stone_shard` and `flint_shard`; sneak-right-click a rock on configured knapping blocks to convert it, with stone particles and the item-break sound.
- Added optional JEI information pages for the rocks, shards, fiber, cord, and axes.
- Added `plant_fiber` and `plant_cord`; empty-hand right-click pulls grass for 1–2 fiber, and 3 fiber craft 1 cord.
- Added `stone_axe` and `flint_axe` with durability 10 and wooden-axe-like speed.
- Vanilla 4-plank recipes are removed; 1 log now crafts 2 planks, and a charged axe on stripped wood produces 2 planks.
- Wooden blocks require an axe. Empty-hand attempts deal 1 damage, small knockback, and show `你疯了?为什么要空手打树?`.

**中文**

- 新增可含水的表面方块 `small_pebble` 和 `small_flint`。
- 只生成在合法的石头/砂砾/沙子支撑上，不会悬空，也不会生成在草或树叶上。
- 右键拾取，左键掉落；支撑方块被破坏时也会掉落。
- 新增 `stone_shard` 和 `flint_shard`；潜行右键把石子按在可敲击方块上转换，带石头粒子和物品损坏音效。
- 为石子、碎片、纤维、植物绳和斧子添加可选的 JEI 信息页。
- 新增 `plant_fiber` 和 `plant_cord`；空手右键拔草获得 1–2 个纤维，3 个纤维合成 1 个植物绳。
- 新增耐久 10、速度接近木斧的 `stone_axe` 和 `flint_axe`。
- 移除原版 4 木板配方；1 个原木合成 2 个木板，斧子对去皮木头蓄力右键也获得 2 个木板。
- 木质方块必须使用斧类工具。空手尝试造成 1 点伤害、小击退，并显示 `你疯了?为什么要空手打树?`。

### Config / 配置

**English**

- Mob gear config is now TOML under `config/fabrimechanus/mob_gear/`.
- Layout: `common.toml`, `minecraft/*.toml`, and an empty `compat/`.
- First launch writes 15 vanilla mob files; existing files are never overwritten.
- `/reload` reloads the TOML rules.
- The old JSON config format is no longer read.

**中文**

- 怪物装备配置改为 `config/fabrimechanus/mob_gear/` 下的 TOML。
- 结构：`common.toml`、`minecraft/*.toml` 和空的 `compat/`。
- 首次启动写入 15 个原版怪物文件；已有文件不会被覆盖。
- `/reload` 重新读取 TOML 规则。
- 不再读取旧 JSON 配置。

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
| Size | 124,183 bytes |
| SHA-256 | `928D0E11D97C378A9C38D55EF40F55E3ABDDC29F6B1BD3B41B9009BB0D164BE6` |
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
