package com.ombremoon.sentinellib.networking;

import com.ombremoon.sentinellib.Constants;
import com.ombremoon.sentinellib.api.box.BoxInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.joml.Vector3f;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PayloadHandler {

    public static void triggerSentinelBox(int entityID, String boxID) {
        PacketDistributor.sendToAllPlayers(new ClientboundTriggerSentinelBox(entityID, boxID));
    }

    public static void removeSentinelBox(int entityID, String boxID) {
        PacketDistributor.sendToAllPlayers(new ClientboundRemoveSentinelBox(entityID, boxID));
    }

    public static void syncRotation(int entityID, String boxId, BoxInstance.BoxRotation rotation) {
        PacketDistributor.sendToServer(new ServerboundSyncRotation(entityID, boxId, rotation));
    }

    public static void syncGeoBone(int entityID, String boxId, Vector3f posVector, Vector3f rotVector, Vector3f scaleVector) {
        PacketDistributor.sendToServer(new ServerboundSyncGeoBone(entityID, boxId, posVector, rotVector, scaleVector));
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToServer(
                ServerboundSyncRotation.TYPE,
                ServerboundSyncRotation.STREAM_CODEC,
                ServerboundSyncRotation::handle
        );
        registrar.playToServer(
                ServerboundSyncGeoBone.TYPE,
                ServerboundSyncGeoBone.STREAM_CODEC,
                ServerboundSyncGeoBone::handle
        );

        registrar.playToClient(
                ClientboundTriggerSentinelBox.TYPE,
                ClientboundTriggerSentinelBox.STREAM_CODEC,
                ClientboundTriggerSentinelBox::handle
        );
        registrar.playToClient(
                ClientboundRemoveSentinelBox.TYPE,
                ClientboundRemoveSentinelBox.STREAM_CODEC,
                ClientboundRemoveSentinelBox::handle
        );
    }
}
