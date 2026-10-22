package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModList;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingGetProjectileEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppedEvent;

/**
 * Entry point for everything Tinkers' Construct related.
 *
 * <p>Tinkers' Construct is an optional dependency, so nothing here is loaded unless the mod is present. All event
 * handlers only register from {@link #register()}, which is called from the mod constructor behind {@link #isLoaded()}.</p>
 */
public final class TinkersIntegration {
    public static final String TINKERS_MOD_ID = "tconstruct";

    private static boolean registered;

    private TinkersIntegration() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(TINKERS_MOD_ID);
    }

    public static void register() {
        if (registered || !isLoaded()) {
            return;
        }
        registered = true;
        // runs at join time, after both vanilla and Tinkers' equipment rolls, so their loot is never replaced
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, false, EntityJoinLevelEvent.class, MobGearSpawnHandler::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingGetProjectileEvent.class, MobAmmoHandler::onGetProjectile);
        // materials live in a dynamic registry, so the tier cache must not survive a server restart
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ServerStoppedEvent.class, event -> RandomTierMaterials.invalidate());
    }
}
