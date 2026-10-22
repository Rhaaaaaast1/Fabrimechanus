package dev.rhaaaaaast1.fabrimechanus;

import com.mojang.logging.LogUtils;
import dev.rhaaaaaast1.fabrimechanus.tinkers.TinkersIntegration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Fabrimechanus.MODID)
public class Fabrimechanus {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "fabrimechanus";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Fabrimechanus(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register our mod's ForgeConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Tinkers' Construct is optional: only hook its loot/AI integration when the mod is actually loaded
        if (TinkersIntegration.isLoaded()) {
            TinkersIntegration.register();
            LOGGER.info("Tinkers' Construct detected - mob equipment and ranged attack AI enabled");
        } else {
            LOGGER.info("Tinkers' Construct not installed - mob equipment and ranged attack AI disabled");
        }
    }
}
