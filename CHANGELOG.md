# Tinkers' AI — Changelog

## 1.1.1 — 2026-10-31

### 1.21.1 / NeoForge

- 怪物装备规则从数据包迁移到配置文件：
  - `config/fabrimechanus/mob_gear/`
  - 首次启动自动生成 15 个原版怪物配置
  - `minecraft/` 放原版怪物，`compat/` 放其他模组怪物
- 配置文件支持 `/reload` 重载；已有文件不会被覆盖。
- 旧的 `data/<namespace>/fabrimechanus/mob_gear/` 不再读取。
- 明确不修改原版/匠魂战利品表；箱子战利品不受影响。
- 原版装备仍然只从新生成怪物的装备槽移除，范围由 `#fabrimechanus:stripped_monster_gear` 控制。
- 版本号：`1.1.0` → `1.1.1`。

**文件**

| 项目 | 值 |
|---|---|
| Jar | `tinkers-ai-1.21.1-1.1.1.jar` |
| 大小 | 75,590 字节 |
| SHA-256 | `96f2a9fbd5709d223563212e3ae41b14154de9ce84101b3b3e78d9ec7cf02597` |
| 加载器 | NeoForge 21.1.x |
| 可选依赖 | Tinkers' Construct 3.13.x、Mantle |

### 1.20.1 / Forge

- 同样把怪物装备规则迁移到：
  - `config/tinkersai/mob_gear/`
  - 首次启动自动生成 14 个原版怪物配置（1.20.1 没有 bogged）
  - `minecraft/` 放原版怪物，`compat/` 放其他模组怪物
- 配置文件支持 `/reload` 重载；已有文件不会被覆盖。
- 旧的 `data/<namespace>/tinkersai/mob_gear/` 不再读取。
- 不修改原版/匠魂战利品表；箱子战利品不受影响。
- 原版装备仍然只从新生成怪物的装备槽移除，范围由 `#tinkersai:stripped_monster_gear` 控制。
- 版本号：`1.1.0` → `1.1.1`。
- 1.21.1 的新生存功能（石子、植物纤维、简陋石斧等）**不会同步到 1.20.1**。

**文件**

| 项目 | 值 |
|---|---|
| Jar | `tinkers-ai-1.20.1-1.1.1.jar` |
| 大小 | 70,623 字节 |
| SHA-256 | `7c6e4a39739170c853ae806347ccce7a8f67c8caca5ccb66b77680ae29108573` |
| 加载器 | Forge 47.4.x |
| 可选依赖 | Tinkers' Construct 3.12.1.237、Mantle 1.11.124 |

### 数据包迁移说明

旧版规则放在：

```text
data/<namespace>/fabrimechanus/mob_gear/
data/<namespace>/tinkersai/mob_gear/
```

现在请把 JSON 文件移动到：

```text
config/fabrimechanus/mob_gear/minecraft/
config/fabrimechanus/mob_gear/compat/
config/tinkersai/mob_gear/minecraft/
config/tinkersai/mob_gear/compat/
```

文件格式不变，仍然支持 `priority`、`mobs`、`mainhand`、`offhand`、`armor`。

### 关于箱子战利品

本版本**没有**删除或替换箱子战利品中的原版工具/护甲，也没有新增匠魂战利品替换。

- 原版箱子、地牢、要塞、沉船、下界要塞战利品：未修改
- 匠魂战利品表：未修改
- 只移除新生成怪物装备槽中的原版工具/护甲

如果看到箱子战利品变化，请检查其他数据包、旧实验文件或整合包配置。

---

## 1.1.0 — 基础版本

- 怪物生成时获得匠魂工具/护甲，主手、副手、头盔、胸甲、护腿、靴子。
- 每件工具的各部件材料独立随机，材料等级权重可配置。
- 匠魂弓、弩、投掷武器的怪物远程 AI。
- 怪物无限箭矢，投掷武器不消耗、不扣耐久。
- 规则由数据包驱动（1.1.1 起改为配置文件）。
- 1.21.1 的 KubeJS 工具层已拆分到独立模组 **AKubejsPlugin**。

---

## 1.2.0 — 计划中（仅 1.21.1）

以下功能尚未实现，仅作为后续计划：

- 石子 / 燧石石子的注册与生成
- 植物纤维、植物绳的注册与获取
- 简陋的石斧 / 简陋的燧石斧
- 禁止玩家空手撸树
- 不计划同步到 1.20.1

---

## 兼容性

- Tinkers' Construct / Mantle 是可选依赖；未安装时模组正常加载但不启用相关功能。
- 只影响新生成的怪物，已经存在的怪物不会被追溯修改。
- 客户端与服务端都需要安装。
- 这是非官方附属模组，与匠魂官方团队无关。
