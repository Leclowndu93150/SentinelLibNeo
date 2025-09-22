package com.ombremoon.sentinellib.example;

import com.ombremoon.sentinellib.CommonClass;
import com.ombremoon.sentinellib.Constants;
import com.ombremoon.sentinellib.SentinelLib;
import com.ombremoon.sentinellib.example.renderer.IceMistRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
final class SLClient {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        if (CommonClass.isDevEnv())
            event.registerEntityRenderer(SentinelLib.MIST.get(), IceMistRenderer::new);
    }
}
