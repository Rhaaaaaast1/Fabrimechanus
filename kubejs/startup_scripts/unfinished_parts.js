// priority: 0
// 纯 KJS 未完成件注册
// 注意：这里的 PARTS 必须和 server_scripts/unfinished_parts.js 保持一致
//
// 外观：
// - 模型是一块扁平 tinted 面，tintindex = 0
// - 颜色 = 匠魂该材料颜色 + 橙红高温色
// - 没有 Material 时退化为纯橙红

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

    const JsonParser = Java.loadClass('com.google.gson.JsonParser')
    const DataComponents = Java.loadClass('net.minecraft.core.component.DataComponents')

    // 刚浇铸出来的高温橙红
    const HOT_R = 255
    const HOT_G = 64
    const HOT_B = 16
    const HOT_MIX = 0.65

    let MaterialVariantId = null
    let MaterialRenderInfoLoader = null
    const MATERIAL_COLOR_CACHE = {}

    function getMaterialColor(material) {
        if (!material) return null
        if (MATERIAL_COLOR_CACHE[material] !== undefined) {
            return MATERIAL_COLOR_CACHE[material]
        }

        let color = null
        try {
            if (!MaterialVariantId) {
                MaterialVariantId = Java.loadClass('slimeknights.tconstruct.library.materials.definition.MaterialVariantId')
            }
            if (!MaterialRenderInfoLoader) {
                MaterialRenderInfoLoader = Java.loadClass('slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader')
            }

            const variant = MaterialVariantId.tryParse(material)
            if (variant) {
                const info = MaterialRenderInfoLoader.INSTANCE
                    .getRenderInfo(variant)
                    .orElse(null)
                if (info) {
                    color = info.vertexColor()
                }
            }
        } catch (e) {
            // 服务端/类过滤/加载顺序问题：退化为纯橙红
            color = null
        }

        MATERIAL_COLOR_CACHE[material] = color
        return color
    }

    function tint(stack, tintIndex) {
        let material = null
        try {
            const custom = stack.getComponents().get(DataComponents.CUSTOM_DATA)
            if (custom && !custom.isEmpty()) {
                material = custom.copyTag().getString('Material')
            }
        } catch (e) {
            material = null
        }

        const argb = getMaterialColor(material)
        if (argb == null) {
            return Color.rgba(HOT_R, HOT_G, HOT_B, 255)
        }

        const a = (argb >>> 24) & 0xFF
        const r = (argb >>> 16) & 0xFF
        const g = (argb >>> 8) & 0xFF
        const b = argb & 0xFF

        // 保留材料色，但明显偏向刚浇铸的橙红
        return Color.rgba(
            Math.round(r * (1 - HOT_MIX) + HOT_R * HOT_MIX),
            Math.round(g * (1 - HOT_MIX) + HOT_G * HOT_MIX),
            Math.round(b * (1 - HOT_MIX) + HOT_B * HOT_MIX),
            a === 0 ? 255 : a
        )
    }

    // 扁平 tinted 模型；display 用原版 generated 的参数
    const TINTED_DISPLAY = JsonParser.parseString(`{
        "gui": { "rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [1, 1, 1] },
        "ground": { "rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5] },
        "fixed": { "rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [1, 1, 1] },
        "thirdperson_righthand": { "rotation": [0, 0, 0], "translation": [0, 3, 1], "scale": [0.55, 0.55, 0.55] },
        "firstperson_righthand": { "rotation": [0, -135, 25], "translation": [0, 4, 2], "scale": [0.4, 0.4, 0.4] }
    }`)

    StartupEvents.registry('item', event => {
        PARTS.forEach(partId => {
            const path = partId.split(':')[1]

            event.create(`unfinished_${path}`)
                // 用浅灰/白色底材，材料色和高温色更容易显出来
                .texture('minecraft:item/iron_ingot')
                .maxStackSize(1)
                .color(0, tint)
                .modelGenerator(model => {
                    // 一块 16x16、1px 厚的扁平模型；tintindex 0 交给 tint()
                    model.texture('layer0', 'minecraft:item/iron_ingot')
                    model.element(element => {
                        element.size(AABB.of(0, 0, 7.5, 16, 16, 8.5))
                        element.allFaces(face => {
                            face.tex('#layer0')
                            face.tintindex(0)
                            face.uv(0, 0, 16, 16)
                        })
                    })

                    // 去掉默认 generated parent，避免未 tint 的 layer0 叠在上面
                    model.custom(json => {
                        json.remove('parent')
                        json.add('display', TINTED_DISPLAY)
                    })
                })
        })

        console.info(`[unfinished] registered ${PARTS.length} tinted unfinished part items`)
    })
})()
