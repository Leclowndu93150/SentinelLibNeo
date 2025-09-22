package com.ombremoon.sentinellib.networking;

import com.ombremoon.sentinellib.CommonClass;
import com.ombremoon.sentinellib.api.box.BoxInstance;
import com.ombremoon.sentinellib.common.ISentinel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSyncRotation(int entityId, String boxName, BoxInstance.BoxRotation rotation) implements CustomPacketPayload {
    public static final Type<ServerboundSyncRotation> TYPE = new Type<>(CommonClass.customLocation("sync_rotation"));
    public static final StreamCodec<ByteBuf, ServerboundSyncRotation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSyncRotation::entityId,
            ByteBufCodecs.STRING_UTF8, ServerboundSyncRotation::boxName,
            BoxInstance.BoxRotation.STREAM_CODEC, ServerboundSyncRotation::rotation,
            ServerboundSyncRotation::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ServerboundSyncRotation payload, final IPayloadContext context) {
//        context.enqueueWork(() -> {
            Level level = context.player().level();
            if (!level.isClientSide) {
                Entity entity = level.getEntity(payload.entityId());

                if (entity == null)
                    return;

                if (entity instanceof ISentinel sentinel) {
                    var instance = sentinel.getBoxManager().getBoxInstance(payload.boxName());
                    if (instance != null) {
                        var rot = payload.rotation;
                        instance.setRotation(rot.xRot(), rot.xRot0(), rot.yRot(), rot.yRot0());
                    }
                }
            }
//        });
    }
}
