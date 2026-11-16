package dev.rhaaaaaast1.fabrimechanus.primitive;

import com.mojang.logging.LogUtils;
import dev.rhaaaaaast1.fabrimechanus.mixin.ItemEntityAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

/** Periodically removes loaded ItemEntity instances in every dimension. */
public final class ItemCleanup {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ItemCleanup() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ItemCleanup::onServerTick);
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        if (!ItemCleanupConfig.enabled()) {
            return;
        }
        MinecraftServer server = event.getServer();
        long tick = server.getTickCount();
        if (tick <= 0) {
            return;
        }

        int interval = ItemCleanupConfig.intervalTicks();
        int warn = ItemCleanupConfig.warnSeconds();
        long elapsed = tick % interval;

        if (warn > 0 && elapsed % 20 == 0) {
            int remain = (int) ((interval - elapsed + 19) / 20);
            if (remain <= warn) {
                notifyAll(server, ChatFormatting.YELLOW + "掉落物将在 " + remain + " 秒后清理");
            }
        }

        if (elapsed != 0) {
            return;
        }

        int cleared = 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (ItemEntity item : level.getEntities(EntityTypeTest.forClass(ItemEntity.class), Entity::isAlive)) {
                if (ItemCleanupConfig.preserveNoPickup()
                        && ((ItemEntityAccessor) item).fabrimechanus$pickupDelay() == Short.MAX_VALUE) {
                    continue;
                }
                if (ItemCleanupConfig.preserveNoDespawn() && item.getAge() == Short.MIN_VALUE) {
                    continue;
                }
                cleared += item.getItem().getCount();
                item.discard();
            }
        }

        if (cleared > 0) {
            LOGGER.info("[清理掉落物] 已清除 {} 个物品", cleared);
        }
        if (ItemCleanupConfig.showClearMessage()) {
            notifyAll(server, ChatFormatting.GREEN + "掉落物已清理（" + cleared + " 个）");
        }
    }

    private static void notifyAll(MinecraftServer server, String text) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (ItemCleanupConfig.actionBar()) {
                player.displayClientMessage(Component.literal(text), true);
            } else {
                player.sendSystemMessage(Component.literal(text));
            }
        }
    }
}
