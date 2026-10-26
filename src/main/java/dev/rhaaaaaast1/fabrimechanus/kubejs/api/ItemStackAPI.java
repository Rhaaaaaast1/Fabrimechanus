package dev.rhaaaaaast1.fabrimechanus.kubejs.api;

import dev.latvian.mods.kubejs.plugin.builtin.wrapper.NBTWrapper;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.ScriptableObject;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * KubeJS binding for editing the {@code minecraft:custom_data} component of an
 * ItemStack without loading Java classes from scripts.
 */
public final class ItemStackAPI {
    private ItemStackAPI() {
    }

    public static Object get(Context cx, ItemStack stack) {
        CompoundTag tag = tag(stack);
        Object value = NBTWrapper.fromTag(tag);
        return toJs(cx, value);
    }

    public static ItemStack set(Context cx, ItemStack stack, Object data) {
        CompoundTag tag = NBTWrapper.wrapCompound(cx, data);
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        return stack;
    }

    public static ItemStack merge(Context cx, ItemStack stack, Object data) {
        CompoundTag existing = tag(stack);
        existing.merge(NBTWrapper.wrapCompound(cx, data));
        return set(cx, stack, existing);
    }

    public static ItemStack remove(Context cx, ItemStack stack, Object keys) {
        CompoundTag existing = tag(stack);
        for (String key : keys(keys)) {
            existing.remove(key);
        }
        return set(cx, stack, existing);
    }

    public static boolean has(ItemStack stack, String key) {
        return tag(stack).contains(key);
    }

    public static ItemStack copyKeys(ItemStack from, ItemStack to, Object keys) {
        CompoundTag source = tag(from);
        CompoundTag target = tag(to);

        for (String key : keys(keys)) {
            Tag value = source.get(key);
            if (value != null) {
                target.put(key, value.copy());
            }
        }

        return setRaw(to, target);
    }

    /**
     * Copies every non-default data component from {@code from} onto {@code to}.
     * This includes custom_data, enchantments, damage, name, etc.
     *
     * <p>Use this only when the two items are component-compatible. For plain
     * custom NBT, prefer {@link #copyKeys(ItemStack, ItemStack, Object)}.</p>
     */
    public static ItemStack copyComponents(ItemStack from, ItemStack to) {
        if (from == null || from.isEmpty() || to == null || to.isEmpty()) {
            return to;
        }
        to.applyComponents(from.getComponentsPatch());
        return to;
    }

    /**
     * Copies only the minecraft:custom_data component. A source without
     * custom_data clears the target's custom_data.
     */
    public static ItemStack copyCustomData(ItemStack from, ItemStack to) {
        if (to == null || to.isEmpty()) {
            return to;
        }
        CustomData data = from == null || from.isEmpty() ? null : from.get(DataComponents.CUSTOM_DATA);
        if (data == null || data.isEmpty()) {
            to.remove(DataComponents.CUSTOM_DATA);
        } else {
            to.set(DataComponents.CUSTOM_DATA, CustomData.of(data.copyTag()));
        }
        return to;
    }

    public static ItemStack of(Context cx, String itemId, Object data) {
        return of(cx, itemId, data, 1);
    }

    public static ItemStack of(Context cx, String itemId, Object data, int count) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return ItemStack.EMPTY;
        }

        Item item = BuiltInRegistries.ITEM.get(id);
        ItemStack stack = new ItemStack(item, Math.max(1, count));
        return set(cx, stack, data);
    }

    public static boolean matches(Context cx, ItemStack stack, Object data) {
        CompoundTag existing = tag(stack);
        CompoundTag expected = NBTWrapper.wrapCompound(cx, data);
        for (String key : expected.getAllKeys()) {
            Tag value = existing.get(key);
            if (value == null || !value.equals(expected.get(key))) {
                return false;
            }
        }
        return true;
    }

    public static String snbt(ItemStack stack) {
        return tag(stack).toString();
    }

    private static ItemStack setRaw(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        return stack;
    }

    private static CompoundTag tag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    private static List<String> keys(Object keys) {
        List<String> result = new ArrayList<>();
        if (keys == null) {
            return result;
        }
        if (keys instanceof String string) {
            result.add(string);
        } else if (keys instanceof Iterable<?> iterable) {
            for (Object value : iterable) {
                if (value != null) {
                    result.add(String.valueOf(value));
                }
            }
        } else if (keys instanceof Object[] array) {
            for (Object value : array) {
                if (value != null) {
                    result.add(String.valueOf(value));
                }
            }
        } else {
            result.add(String.valueOf(keys));
        }
        return result;
    }

    private static Object toJs(Context cx, Object value) {
        Scriptable scope = ((KubeJSContext) cx).topLevelScope;

        if (value instanceof Map<?, ?> map) {
            Scriptable object = cx.newObject(scope);
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                ScriptableObject.putProperty(object, String.valueOf(entry.getKey()), toJs(cx, entry.getValue()), cx);
            }
            return object;
        }

        if (value instanceof List<?> list) {
            Object[] array = new Object[list.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = toJs(cx, list.get(i));
            }
            return cx.newArray(scope, array);
        }

        return value;
    }
}
