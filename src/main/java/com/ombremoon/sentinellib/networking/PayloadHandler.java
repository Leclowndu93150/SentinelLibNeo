package com.ombremoon.sentinellib.networking;

import com.ombremoon.sentinellib.Constants;
import com.ombremoon.sentinellib.api.box.BoxInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PayloadHandler {

    public static void syncRotation(int entityID, String boxId, BoxInstance.BoxRotation rotation) {
        PacketDistributor.sendToServer(new ServerboundSyncRotation(entityID, boxId, rotation));
    }

    public static void triggerSentinelBox(int entityID, String boxID) {
        PacketDistributor.sendToAllPlayers(new ClientboundTriggerSentinelBox(entityID, boxID));
    }

    public static void removeSentinelBox(int entityID, String boxID) {
        PacketDistributor.sendToAllPlayers(new ClientboundRemoveSentinelBox(entityID, boxID));
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                ServerboundSyncRotation.TYPE,
                ServerboundSyncRotation.CODEC,
                ServerboundSyncRotation::handle
        );

        registrar.playToClient(
                ClientboundTriggerSentinelBox.TYPE,
                ClientboundTriggerSentinelBox.CODEC,
                ClientboundTriggerSentinelBox::handle
        );
        registrar.playToClient(
                ClientboundRemoveSentinelBox.TYPE,
                ClientboundRemoveSentinelBox.CODEC,
                ClientboundRemoveSentinelBox::handle
        );
    }
}
