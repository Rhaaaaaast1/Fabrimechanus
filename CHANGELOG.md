# Tinkers' AI — Changelog / 更新日志

## 1.1.1 — 2026-10-31

### 1.21.1 / NeoForge

**English**

- Mob gear rules moved from datapacks to `config/fabrimechanus/mob_gear/`.
- First launch generates 15 vanilla mob configs.
- `minecraft/` holds vanilla mobs; `compat/` holds modded mobs.
- Rules reload with `/reload`; existing files are never overwritten.
- The old `data/<namespace>/fabrimechanus/mob_gear/` is no longer read.
- No vanilla or Tinkers' loot table changes; chest loot is untouched.
- Vanilla equipment is removed only from newly spawned monsters' equipment slots,
  controlled by `#fabrimechanus:stripped_monster_gear`.
- Version: `1.1.0` → `1.1.1`.

**中文**

- 怪物装备规则从数据包迁移到 `config/fabrimechanus/mob_gear/`。
- 首次启动自动生成 15 个原版怪物配置。
- `minecraft/` 放原版怪物，`compat/` 放其他模组怪物。
- 配置文件支持 `/reload` 重载，已有文件不会被覆盖。
- 旧的 `data/<namespace>/fabrimechanus/mob_gear/` 不再读取。
- 不修改原版或匠魂战利品表；箱子战利品不受影响。
- 原版装备只从新生成怪物的装备槽移除，
  范围由 `#fabrimechanus:stripped_monster_gear` 控制。
- 版本：`1.1.0` → `1.1.1`。

**File / 文件**

| Item / 项目 | Value / 值 |
|---|---|
| Jar | `tinkers-ai-1.21.1-1.1.1.jar` |
| Size / 大小 | 75,590 bytes |
| SHA-256 | `96f2a9fbd5709d223563212e3ae41b14154de9ce84101b3b3e78d9ec7cf02597` |
| Loader / 加载器 | NeoForge 21.1.x |
| Optional / 可选依赖 | Tinkers' Construct 3.13.x, Mantle |

### 1.20.1 / Forge

**English**

- Mob gear rules moved from datapacks to `config/tinkersai/mob_gear/`.
- First launch generates 14 vanilla mob configs (1.20.1 has no bogged).
- `minecraft/` holds vanilla mobs; `compat/` holds modded mobs.
- Rules reload with `/reload`; existing files are never overwritten.
- The old `data/<namespace>/tinkersai/mob_gear/` is no longer read.
- No vanilla or Tinkers' loot table changes; chest loot is untouched.
- Vanilla equipment is removed only from newly spawned monsters' equipment slots,
  controlled by `#tinkersai:stripped_monster_gear`.
- Version: `1.1.0` → `1.1.1`.
- The 1.21.1 survival features (pebbles, plant fiber, crude axes, etc.) are **not**
  synced to 1.20.1.

**中文**

- 怪物装备规则从数据包迁移到 `config/tinkersai/mob_gear/`。
- 首次启动自动生成 14 个原版怪物配置（1.20.1 没有 bogged）。
- `minecraft/` 放原版怪物，`compat/` 放其他模组怪物。
- 配置文件支持 `/reload` 重载，已有文件不会被覆盖。
- 旧的 `data/<namespace>/tinkersai/mob_gear/` 不再读取。
- 不修改原版或匠魂战利品表；箱子战利品不受影响。
- 原版装备只从新生成怪物的装备槽移除，
  范围由 `#tinkersai:stripped_monster_gear` 控制。
- 版本：`1.1.0` → `1.1.1`。
- 1.21.1 的新生存功能（石子、植物纤维、简陋石斧等）**不会同步到 1.20.1**。

**File / 文件**

| Item / 项目 | Value / 值 |
|---|---|
| Jar | `tinkers-ai-1.20.1-1.1.1.jar` |
| Size / 大小 | 70,623 bytes |
| SHA-256 | `7c6e4a39739170c853ae806347ccce7a8f67c8caca5ccb66b77680ae29108573` |
| Loader / 加载器 | Forge 47.4.x |
| Optional / 可选依赖 | Tinkers' Construct 3.12.1.237, Mantle 1.11.124 |

### Migration / 迁移说明

**English:** Move old datapack rule files to the config folder. The JSON format is unchanged (`priority`, `mobs`, `mainhand`, `offhand`, `armor`).

**中文：** 把旧数据包规则文件移动到配置文件夹；JSON 格式不变（`priority`、`mobs`、`mainhand`、`offhand`、`armor`）。

```text
Old / 旧:
data/<namespace>/fabrimechanus/mob_gear/
data/<namespace>/tinkersai/mob_gear/

New / 新:
config/fabrimechanus/mob_gear/minecraft/
config/fabrimechanus/mob_gear/compat/
config/tinkersai/mob_gear/minecraft/
config/tinkersai/mob_gear/compat/
```

### Loot tables / 战利品表

**English:** No vanilla or Tinkers' chest loot is removed or replaced. Only newly spawned monsters' equipment slots are cleared.

**中文：** 没有删除或替换原版/匠魂箱子战利品，只清空新生成怪物的装备槽。

---

## 1.1.0 — Base release / 基础版本

**English**

- Newly spawned monsters receive Tinkers' Construct tools/armor in all six equipment slots.
- Each tool part rolls its material independently; tier weights are configurable.
- Ranged AI for Tinkers' bows, crossbows, and throwing weapons.
- Mobs have infinite arrows; thrown weapons are not consumed and lose no durability.
- Rules were datapack-driven in 1.1.0 (moved to config files in 1.1.1).
- The 1.21.1 KubeJS utility layer was split into **AKubejsPlugin**.

**中文**

- 新生成怪物在六个装备槽中获得匠魂工具/护甲。
- 每件工具的各部件材料独立随机；材料等级权重可配置。
- 匠魂弓、弩、投掷武器的怪物远程 AI。
- 怪物无限箭矢；投掷武器不消耗、不扣耐久。
- 1.1.0 的规则由数据包驱动（1.1.1 起改为配置文件）。
- 1.21.1 的 KubeJS 工具层已拆分到 **AKubejsPlugin**。

---

## 1.2.0 — Planned / 计划中 (1.21.1 only)

**English:** Not implemented yet. Planned: pebbles/flint pebbles, plant fiber/string, crude stone/flint axes, and preventing empty-hand tree punching. Not planned for 1.20.1.

**中文：** 尚未实现。计划内容：石子/燧石石子、植物纤维/植物绳、简陋石斧/燧石斧、禁止空手撸树。不同步到 1.20.1。

---

## Compatibility / 兼容性

**English:** Tinkers' Construct / Mantle are optional; without them the mod loads but does nothing. Only new mobs are affected. Install on client and server.

**中文：** 匠魂/Mantle 是可选依赖；未安装时模组正常加载但不启用功能。只影响新怪物。客户端与服务端都需要安装。
