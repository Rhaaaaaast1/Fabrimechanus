package dev.rhaaaaaast1.fabrimechanus.primitive;

import dev.rhaaaaaast1.fabrimechanus.Fabrimechanus;
import dev.rhaaaaaast1.fabrimechanus.tinkers.FabriTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/** Client-only pose for the empty-hand plant pull charge. */
@EventBusSubscriber(modid = Fabrimechanus.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class PrimitiveClientEvents {
    private PrimitiveClientEvents() {
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)
                || player != Minecraft.getInstance().player
                || !Minecraft.getInstance().options.keyUse.isDown()) {
            return;
        }

        HitResult hit = player.pick(4.0D, 1.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)
                || !player.level().getBlockState(blockHit.getBlockPos()).is(FabriTags.PLANT_FIBER_SOURCES)) {
            return;
        }

        PlayerRenderer renderer = event.getRenderer();
        var model = renderer.getModel();
        model.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
        model.leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;

        ModelPart right = model.rightArm;
        ModelPart left = model.leftArm;
        right.xRot = -1.35F;
        left.xRot = -1.35F;
        right.yRot = -0.28F;
        left.yRot = 0.28F;
        right.zRot = 0.12F;
        left.zRot = -0.12F;
    }
}
