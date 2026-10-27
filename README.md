# Fabrimechanus

把 **Tinkers' Construct（匠魂）** 接入原版怪物装备系统。

- 怪物会使用匠魂弓进行远程攻击（原版 AI 不认识匠魂发射器）。
- 怪物生成时携带匠魂工具/防具，材料按等级概率随机。
- 剥离指定的原版工具/防具刷新，但不影响捡取，也不改动匠魂自带数据。
- 匠魂是 **可选** 依赖：未安装时模组正常加载，集成代码完全跳过。

本仓库是 **Minecraft 1.21.1 + NeoForge 21.1.264** 版本（依赖非官方移植匠魂 3.13.3）。
1.20.1 + Forge 版本在独立仓库：
[Rhaaaaaast1/TinkersAdditions](https://github.com/Rhaaaaaast1/TinkersAdditions)。

> KubeJS 工具层已经拆分到独立模组 **AKubejsPlugin**，本模组不再包含 KubeJS 代码。

## 构建

```bash
./gradlew build
```

Gradle wrapper 使用腾讯镜像：

```text
https://mirrors.cloud.tencent.com/gradle/gradle-8.8-bin.zip
```

构建前需要把匠魂移植版与 Mantle 的 jar 放进 `libs/`（jar 不进 git）：

- `libs/TinkersConstruct-1.21.1-3.13.3.jar`
- `libs/Mantle-1.21.1-1.13.1.jar`

两者分别来自：

- <https://github.com/zhuchuovo/TinkersConstruct-1.21.1>
- <https://github.com/zhuchuovo/Mantle-1.21.1>

`build.gradle` 使用 `compileOnly files(...)`，因此产物不会打包匠魂；运行时由玩家自行安装。

## 实现结构

`dev.rhaaaaaast1.fabrimechanus.tinkers` 包内的类只在检测到 `tconstruct` 时被加载
（`ModList.get().isLoaded("tconstruct")`），避免未安装匠魂时出现 `NoClassDefFoundError`。

| 类 | 职责 |
| --- | --- |
| `TinkersIntegration` | 入口：检测匠魂、注册监听器、清理材料缓存 |
| `RandomTierMaterials` | 按等级概率为每个部件独立选材 |
| `ToolFactory` | 用选中的材料组装匠魂工具/防具 |
| `VanillaGear` | 判断装备是否属于需要剥离的原版物品 |
| `MobGearRule` | 数据包规则：怪物、主手/副手权重、护甲概率与套装 |
| `MobGearRegistry` | 规则查找表，按 `priority` 取第一条匹配 |
| `MobGearReloadListener` | 读取 `data/<namespace>/fabrimechanus/mob_gear/**/*.json` |
| `MobGearSpawnHandler` | 生成时剥离原版装备、发放匠魂装备、挂载远程 AI |
| `MobAmmoHandler` | 给怪物提供箭矢（匠魂弹药链路） |
| `TinkersRangedAttackGoal` | 识别匠魂弓的远程攻击 AI |
| `TinkersCrossbowAi` | 判断怪物是否手持匠魂弓并发射已上膛弩箭 |
| `TinkersSpearAi` | 让手持匠魂矛的怪物像三叉戟一样蓄力投掷 |

## 配置

材料等级权重在 `config/fabrimechanus-common.toml` 的
`materialTierWeights` 下调整；权重是相对值，不要求和为 100。

## Gradle 警告

`gradle.properties` 已关闭实验性/孵化中的 configuration cache 警告：

```properties
org.gradle.configuration-cache=false
org.gradle.warning.mode=none
```
