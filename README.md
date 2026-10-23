# Fabrimechanus

模组工程，第一个功能：把 **Tinkers' Construct（匠魂）** 接入原版怪物体系。

- 怪物会用匠魂弓/弩进行远程攻击（原版 AI 不识别匠魂发射器）。
- 怪物生成时携带匠魂工具与防具，材料按等级概率随机。
- 剥离指定的原版工具/防具刷新，但不影响拾取，也不改动任何匠魂自带数据。

本仓库是 **Minecraft 1.21.1 + NeoForge 21.1.264** 版本（依赖非官方移植版匠魂 3.13.3），已编译、已开服验证。功能一致的 **Minecraft 1.20.1 + Forge 47.4.28** 版本在另一个仓库：[Rhaaaaaast1/TinkersAdditions](https://github.com/Rhaaaaaast1/TinkersAdditions)（依赖官方匠魂 3.12.1.237）。

匠魂是**可选**依赖：未安装时模组照常加载，集成代码完全跳过。

本模组**不注册任何物品、方块与创造模式标签页**，只做怪物装备与 AI 的行为注入。

## 1.21.1 / NeoForge 构建

```bash
./gradlew build
```

构建前需要把匠魂移植版与 Mantle 的 jar 放进 `libs/`（该 1.21.1 移植版没有发布到任何 Maven 仓库，jar 不进 git）：

- `libs/TinkersConstruct-1.21.1-3.13.3.jar`
- `libs/Mantle-1.21.1-1.13.1.jar`

两者分别来自 <https://github.com/zhuchuovo/TinkersConstruct-1.21.1> 和 <https://github.com/zhuchuovo/Mantle-1.21.1>（在两个仓库里各执行一次 `./gradlew build`，产物在 `build/libs/`）。`build.gradle` 用 `compileOnly files(...)` 引用，因此产出的 jar 不会打包匠魂，运行时必须由玩家自行安装。

## 1.20.1 / Forge 版本

1.20.1 版本（mod id `tinkersaddition`，工程名 Tinkers' Addition）在独立仓库 [Rhaaaaaast1/TinkersAdditions](https://github.com/Rhaaaaaast1/TinkersAdditions) 中维护，依赖官方匠魂 3.12.1.237 与 Mantle 1.11.124，构建见那边的 README。两边功能一致，只有事件/配置 API 的包名以及弩 AI 的实现方式不同（见下节）。

## 实现结构

`dev.rhaaaaaast1.fabrimechanus.tinkers` 包内的类只会在检测到 `tconstruct` 时被加载（`ModList.get().isLoaded("tconstruct")`），避免未安装匠魂时出现 `NoClassDefFoundError`。1.20.1 版本类名与职责相同，只有事件/配置 API 的包名不同。

| 类 | 职责 |
| --- | --- |
| `TinkersIntegration` | 入口：检测匠魂、注册监听器、清理材料缓存 |
| `RandomTierMaterials` | 按等级概率为每个部件独立选材 |
| `ToolFactory` | 用选中的材料组装出匠魂工具/防具的 `ItemStack` |
| `VanillaGear` | 判断一件装备是否属于需要剥离的原版物品（数据来自 `#fabrimechanus:stripped_monster_gear` 标签） |
| `MobGearRule` | 一条数据包规则：哪些怪物、主手/副手权重、护甲概率与套装 |
| `MobGearRegistry` | 规则查找表，按 `priority` 从高到低取第一条匹配 |
| `MobGearReloadListener` | 读取 `data/<命名空间>/fabrimechanus/mob_gear/{minecraft,compat}/**/*.json`，每次数据包重载时重建规则 |
| `MobGearSpawnHandler` | 生成时剥离原版装备、发放匠魂装备、挂载远程 AI |
| `MobAmmoHandler` | 给怪物提供箭矢（匠魂弹药链路） |
| `TinkersRangedAttackGoal` | 识别匠魂弓（以及没有原版弩 Goal 的怪物）的远程攻击 AI |
| `TinkersCrossbowAi` | 判断怪物是否手持匠魂弩、并发射已上膛的弩矢 |
| `mixin/RangedCrossbowAttackGoalMixin` | 让原版共享的 `RangedCrossbowAttackGoal` 直接驱动匠魂弩（掠夺者等 Goal 类生物） |
| `mixin/CrossbowAttackBehaviorMixin` | 同样的补丁，作用于猪灵等 Brain 类生物的 `CrossbowAttack` 行为 |
| `mixin/PiglinCrossbowMixin` | 让 `Mob#canFireProjectileWeapon` 接受匠魂弩，猪灵才会在远程距离举弩而不是走进近战距离 |
| `mixin/PillagerPoseMixin` / `mixin/PiglinPoseMixin` | 拿匠魂弩时保持原版持弩手臂姿势 |
### 弩：直接修好原版的两套弩 AI

原版的弩 AI 有两套，匠魂弩在两套里都会因为“只认 `CrossbowItem`”而失效，所以各用 Mixin 修一处：

**Goal 类生物（掠夺者、模组生物）——`RangedCrossbowAttackGoal`**

- `isHoldingCrossbow()` 只认 `CrossbowItem`，注入后也认 `ModifiableCrossbowItem`；
- 蓄力时原版用 `ProjectileUtil.getWeaponHoldingHand(mob, item -> item instanceof CrossbowItem)` 找手，这里把谓词放宽到匠魂弩；
- 真正开火时原版调用 `CrossbowItem.performShooting`，改为调用匠魂的 `ModifiableCrossbowItem.fireCrossbow`，发射 Goal 已经上好膛的那一发。

**Brain 类生物（猪灵）——`CrossbowAttack` 行为**

猪灵身上没有 `RangedCrossbowAttackGoal`，它靠 Brain 活动里的 `CrossbowAttack` 行为干活，所以上面那处 Mixin 管不到它，需要在 `CrossbowAttack` 里打同样的补丁：

- `checkExtraStartConditions` 里的 `owner.isHolding(is -> is.getItem() instanceof CrossbowItem)` 放宽到匠魂弩，否则这个行为永远不会启动；
- 蓄力找手的 `ProjectileUtil.getWeaponHoldingHand(...)` 谓词放宽；
- `READY_TO_ATTACK` 阶段原本调用 `CrossbowAttackMob#performCrossbowAttack`（内部又是 `CrossbowItem.performShooting`），改为调用 `fireCrossbow`；
- 起始条件里的 `BehaviorUtils.isWithinAttackRange` 会先问 `Mob#canFireProjectileWeapon`，而猪灵的实现只认 `Items.CROSSBOW`。不修这一处，行为只有在贴身距离判定通过，于是猪灵会先跑进近战距离、只做出“上弦”的动作却不放箭——这就是玩家看到的“假上弦”。

**手臂姿势**

`Pillager#getArmPose`、`Piglin#getArmPose` 同样用 `isHolding(is -> is.getItem() instanceof CrossbowItem)` 决定是否显示举弩姿势：掠夺者会因为判定失败退回近战 `ATTACKING` 姿势（看起来像抡胳膊），猪灵则退回 `DEFAULT`。把这条判定放宽后，搬运匠魂弩的掠夺者/猪灵就恢复原版的持弩、蓄力姿势。

于是“举弩 → 上弦 → 放箭”整套走位与节奏都是原版的，匠魂弩只是换了个弹道出口。1.20.1 版本没有用 Mixin（ForgeGradle 的 refmap 对 `ProjectileUtil` 重载映射不全，产物会在正式环境注入失败），那边由自己的 `TinkersRangedAttackGoal` 完成同样的工作。

## 材料等级概率

每件工具/防具的**每个部件**独立随机，同一件工具可以混用不同等级的材料。等级权重可以在配置文件中自由调整（见下节），默认值：

| 等级 | 默认权重 | 实际概率 |
| --- | --- | --- |
| 0 | 25 | 25% |
| 1 | 25 | 25% |
| 2 | 30 | 30% |
| 3 | 17 | 17% |
| 4 | 3 | 3% |

即零级 + 一级合计 50%，二级 30%，三级 17%，四级 3%。权重是**相对值**，不要求和为 100（写 `25/25/30/17/3` 或 `0.25/0.25/0.30/0.17/0.03` 效果相同），设为 0 表示该等级不再出现；五个全为 0 时回退到默认值。

候选材料只取 `getVisibleMaterials()`，且必须对该部件的属性类型有实际数值；某个等级没有可用材料时，向相邻等级借用。

## 配置

配置文件是 `config/fabrimechanus-common.toml`（整合包/联机服务器改服务器目录下的这份，单人游戏在 `.minecraft/config/`，开发环境在 `run/config/`）。因为注册为 `COMMON` 类型配置，它不在存档的 `serverconfig` 里，改完重启游戏即生效；游戏内也可以从「模组 → Fabrimechanus → Config」直接编辑。

```toml
[materialTierWeights]
	tier0 = 25.0
	tier1 = 25.0
	tier2 = 30.0
	tier3 = 17.0
	tier4 = 3.0
```

**可翻译的部分**：配置项在游戏内 Config 界面显示的名称来自语言文件 `assets/fabrimechanus/lang/<语言>.json` 中的 `fabrimechanus.configuration.material_tier_weights.tierN`（已带 `en_us` 与 `zh_cn`），整合包可以像普通资源一样用资源包覆盖它；而写进 toml 文件的 `#` 注释（`comment(...)`）是 FML 自动生成的内容，**不会**被翻译，永远保持英文。修改语言文件后需要重载资源包（F3+T）才能看到新名称。

## 怪物 → 装备

武器尽量对齐原版：原版持弓的给长弓、持斧的给手斧/板斧，以此类推。唯一的例外是僵尸家族——它们在原版空手，若不发武器就只剩匠魂自带战利品表塞的煎锅，所以僵尸、尸壳、僵尸村民、溺尸也会掷一次匠魂武器；溺尸若带有三叉戟或钓鱼竿则保留原物（武器只填空手）。

| 怪物 | 主手 | 副手 | 护甲 |
| --- | --- | --- | --- |
| 骷髅 / 流浪者 / 沼骸 | 长弓 | — | 板甲 10% |
| 凋灵骷髅 | 剑 / 劈刀（1:1） | — | 板甲 15% |
| 僵尸 / 尸壳 | 剑 / 劈刀 / 匕首 / 手斧（5:2:2:1） | — | 旅行者 8% |
| 僵尸村民 | 剑 / 劈刀 / 匕首（4:3:2） | — | 板甲 10% |
| 溺尸 | 剑 / 匕首 / 标枪（3:3:2），保留三叉戟 / 钓鱼竿 | — | 旅行者 8% |
| 掠夺者 | 弩 | — | — |
| 卫道士 | 手斧 / 板斧（5:4） | — | — |
| 幻术师 | 长弓 | — | — |
| 恼鬼 | 匕首 / 剑（1:1） | — | — |
| 猪灵 | 弩 / 剑（1:1） | — | 板甲 12% |
| 僵尸猪灵 | 剑 | — | 板甲 8% |
| 猪灵蛮兵 | 手斧 / 板斧（5:3） | — | 板甲 7% |

唤魔者、女巫、劫掠兽没有装备方案（原版也不持武器，且模型没有护甲层），只会应用下节的“剥离原版装备”。

方案中**不包含任何远古工具**（战斗牌、斩首刀、战镐、熔炼锅、牛头斧），这些只由匠魂自己的怪物装备表提供。

## 数据包：适配别的模组

上表不是写死在代码里的，而是 `data/fabrimechanus/fabrimechanus/mob_gear/minecraft/` 下的 12 个文件。目录按**怪物来自哪个模组**分两层：

```
data/<命名空间>/fabrimechanus/mob_gear/
├── minecraft/        原版怪物（本模组的 12 个文件都在这里）
└── compat/           其它模组的怪物，按模组再分一层，例如 compat/iceandfire/dragon.json
```

本模组目前没有联动模组，所以 `compat/` 是空的。任何整合包或附属模组只要把同样结构的 JSON 放进**自己的**命名空间（`data/<你的命名空间>/fabrimechanus/mob_gear/compat/<模组id>/xxx.json`），就能给自己的怪物配装备，或覆盖本模组的规则，不需要写 Java；原版怪物想覆盖就放到 `minecraft/` 下并调高 `priority`。两个子目录只是约定，放错位置也仍然会加载，但日志会警告一句。

```json
{
  "priority": 10,
  "mobs": [ "yourmod:your_zombie", "#minecraft:skeletons" ],
  "mainhand": {
    "chance": 1.0,
    "items": [
      { "item": "tconstruct:sword", "weight": 5 },
      { "item": "tconstruct:cleaver", "weight": 2 }
    ]
  },
  "offhand": { "chance": 0.0, "items": [] },
  "armor": { "chance": 0.25, "set": "plate" }
}
```

| 字段 | 说明 |
| --- | --- |
| `priority` | 整数，默认 0。规则按从高到低取**第一条**匹配，所以用 `priority: 10` 加一条只针对某个怪物的规则即可覆盖本模组的整条规则 |
| `mobs` | 实体 id 或实体类型标签，`#` 开头表示标签；一条规则可以写多个，任意一个匹配即生效 |
| `mainhand` / `offhand` | 槽位规则。`chance` 是“这个槽位要不要发”的概率（默认 1.0，副手一般写 0.2 之类）；`items` 里每个条目是 `item` + 可选 `weight`（默认 1），按权重挑一件 |
| `armor` | `chance` 是整套护甲的概率；套装可以用 `set` 写一个（`plate` / `travelers` / `none`），也可以用 `sets` 写多个带权重的套装，见下 |

`armor` 的两种写法等价，第二种可以在多个套装之间按权重随机，但**一只怪只会用其中一套**（不会出现板甲头盔配旅行者胸甲）：

```json
"armor": { "chance": 0.3, "set": "plate" }
```

```json
"armor": {
  "chance": 0.3,
  "sets": [
    { "set": "plate", "weight": 3 },
    { "set": "travelers", "weight": 1 }
  ]
}
```

`sets` 里 `weight` 缺省为 1；同一套的四个部位仍然各自有 15% 的概率缺席（原版的“零散装备”感），所以一整套并不总是四件齐全。

几个要点：

- **只填空槽**：主手、副手、每个护甲槽只在为空时才填，所以匠魂自己 `tinkering/mob_equipment` 发的东西不会被顶掉，玩家丢给怪物的东西也不会被清。
- **远古工具排除**：本模组自带的 12 个文件里没有远古工具；如果你想要，自己在规则里写 `tconstruct:battlesign` 之类的 id 即可，代码不拦。
- **物品 id 写错**只会让这一条被跳过并在日志里留一行 `Unknown item ...`，不会让整个重载失败。
- **材料仍然是随机的**：规则只决定“发什么工具”，每件工具的每个部件依旧按 `config/fabrimechanus-common.toml` 里的等级权重独立 roll 材料。
- **改完执行 `/reload`** 即可生效，日志会出现 `Loaded N mob gear rules from datapacks (minecraft: X, compat: Y, other: Z)`，`other` 不为 0 说明有文件放错了目录；启动时也会打印这一行，方便确认规则有没有被读到。
- **想改剥离清单**（哪些原版装备不许再刷）：改 `data/fabrimechanus/tags/item/stripped_monster_gear.json` 这个物品标签，不需要动用代码。

## 兼容性约定

- **不改动匠魂的任何战利品表**：既不写 `data/tconstruct/**`，也不挂全局战利品修改器。匠魂自己的 `tinkering/mob_equipment` 表先执行，本模组只处理它剩下的槽位。
- **不覆盖匠魂已发放的装备**：某个槽位若已有物品（来自匠魂表），保持原样。
- **剥离原版**：木/石/铁/金/钻石/下界合金的剑、镐、斧、锹、锄；皮革/锁链/铁/金/钻石/下界合金的头盔、胸甲、护腿、靴子；盾牌、弓、弩、剪刀、打火石。三叉戟、重锤、海龟壳、旗帜等特殊物品保留。
- **拾取行为不变**：只在生成时处理一次，玩家丢出的物品仍可被怪物捡起；已经处理过的怪物从存档读回时不会重新随机（`fabrimechanus:tinkers_gear` 标记），但仍会重新挂上远程 AI（原版不保存 AI goal）。
- **远程 AI**：`TinkersRangedAttackGoal` 复刻原版 `RangedBowAttackGoal` 的移动与侧移逻辑，但按 `ModifiableLauncherItem` 判定；弓按工具的 `DRAW_SPEED` 蓄力后释放，弩则先上膛再调用 `ModifiableCrossbowItem#fireCrossbow` 开火。箭矢由 `LivingGetProjectileEvent` 提供，只对非玩家发射器生效。
