// priority: 0
// 匠魂浇筑 -> 机械动力半成品 -> 机械动力处理 -> 真匠魂部件
//
// 为什么这里用 MATERIALS 表：
//   KubeJS 的 event.findRecipes() 对 TConstruct 的自定义 recipe 只返回
//   synthetic KubeRecipe，recipe.json / originalJson 都没有字段；
//   ServerEvents.recipes 阶段 ServerLifecycleHooks.getCurrentServer() 也是 null。
//   所以纯 KJS 无法自动读取 tconstruct:material_fluid。
//   这里改为显式材料表；浇筑侧用 blockEntitySlotChanged 把真实产物替换成半成品。
//
// 依赖：
//   - Fabrimechanus 的 ItemStackAPI / BlockEntityAPI
//   - KubeJS Create

(function () {
    const PARTS = [
        'tconstruct:tool_binding',
        'tconstruct:tool_handle',
        'tconstruct:tough_binding',
        'tconstruct:tough_handle',
        'tconstruct:pick_head',
        'tconstruct:hammer_head',
        'tconstruct:small_axe_head',
        'tconstruct:broad_axe_head',
        'tconstruct:small_blade',
        'tconstruct:broad_blade',
        'tconstruct:large_plate',
        'tconstruct:bow_limb',
        'tconstruct:bow_grip',
        'tconstruct:repair_kit'
    ]

    // 只列需要参与流程的材料。
    // 想加材料：material 必须和匠魂一致；fluid 用具体流体 id，或用 fluidTag。
    const MATERIALS = [
        { material: 'tconstruct:iron', fluid: 'tconstruct:molten_iron', amount: 90 },
        { material: 'tconstruct:copper', fluid: 'tconstruct:molten_copper', amount: 90 },
        { material: 'tconstruct:gold', fluid: 'tconstruct:molten_gold', amount: 90 },
        { material: 'tconstruct:cobalt', fluid: 'tconstruct:molten_cobalt', amount: 90 },
        { material: 'tconstruct:steel', fluid: 'tconstruct:molten_steel', amount: 90 }
    ]

    const PRESSES = 1

    const UNFINISHED_BY_PART = {}
    PARTS.forEach(part => {
        UNFINISHED_BY_PART[part] = `kubejs:unfinished_${part.split(':')[1]}`
    })

    function partPath(part) {
        return part.split(':')[1]
    }

    function materialPath(material) {
        return material.split(':')[1].replace(/[^a-z0-9_\/]/g, '_')
    }

    function createFluid(entry) {
        // 当前配置使用具体流体 id；Fluid.of 返回 FluidStack，
        // KJS Create 的 filling 输入可直接接受。
        return Fluid.of(entry.fluid, entry.amount || 90)
    }

    function addCreateSequence(event, part, entry) {
        const data = {
            'minecraft:custom_data': {
                Material: entry.material
            }
        }

        // DataComponentIngredient，strict=false；允许 Create 追加进度组件
        const input = Ingredient.withData([UNFINISHED_BY_PART[part]], data)

        const transStack = ItemStackAPI.of(UNFINISHED_BY_PART[part], data, 1)
        const transOutput = CreateItem.of(transStack)

        const resultStack = ItemStackAPI.of(part, data, 1)
        const resultOutput = CreateItem.of(resultStack)

        const filling = event.recipes.create.filling(
            transOutput,
            [
                transStack,
                createFluid(entry)
            ]
        )

        const pressing = event.recipes.create.pressing(
            transOutput,
            transStack
        )

        const sequence = [filling]
        for (let i = 0; i < PRESSES; i++) {
            sequence.push(pressing)
        }

        // transitional_item 必须逐材料带 Material；
        // Create advance() 会重置成这个固定物品。
        event.recipes.create.sequenced_assembly(
            [resultOutput],
            input,
            sequence
        )
            .transitionalItem(transOutput)
            .loops(1)
    }

    ServerEvents.recipes(event => {
        console.info(`[unfinished] materials: ${MATERIALS.length}`)

        let sequenceCount = 0
        PARTS.forEach(part => {
            MATERIALS.forEach(entry => {
                addCreateSequence(event, part, entry)
                sequenceCount++
            })
        })

        console.info(`[unfinished] create sequences: ${sequenceCount}`)
    })

    // 浇筑台产出真实部件后，立刻换成未完成件。
    // TConstruct CastingBlockEntity: INPUT = 0, OUTPUT = 1。
    BlockEvents.blockEntitySlotChanged(event => {
        const blockId = String(event.block.id)
        if (blockId !== 'tconstruct:seared_table' && blockId !== 'tconstruct:scorched_table') return
        if (event.slot !== 1) return

        const stack = event.newStack
        if (stack.isEmpty()) return

        const realPart = String(stack.id)
        const unfinished = UNFINISHED_BY_PART[realPart]
        if (!unfinished) return

        const data = ItemStackAPI.get(stack)
        if (!data.Material) return

        let replacement = ItemStackAPI.of(unfinished, data, stack.count)
        ItemStackAPI.copyCustomData(stack, replacement)
        event.setStack(replacement)

        console.info(`[unfinished] casting table: ${realPart} -> ${unfinished}`)
    })
})()
