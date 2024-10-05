package com.ombremoon.sentinellib.networking;

import com.ombremoon.sentinellib.CommonClass;
import com.ombremoon.sentinellib.common.ISentinel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSyncRotation(int entityId, String boxName, float yRot, float yRot0) implements CustomPacketPayload {
    public static final Type<ServerboundSyncRotation> TYPE = new Type<>(CommonClass.customLocation("sync_rotation"));
    public static final StreamCodec<ByteBuf, ServerboundSyncRotation> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSyncRotation::entityId,
            ByteBufCodecs.STRING_UTF8, ServerboundSyncRotation::boxName,
            ByteBufCodecs.FLOAT, ServerboundSyncRotation::yRot,
            ByteBufCodecs.FLOAT, ServerboundSyncRotation::yRot0,
            ServerboundSyncRotation::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ServerboundSyncRotation payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = context.player().level();
            Entity entity = level.getEntity(payload.entityId());

            if (entity == null)
                return;

            if (entity instanceof ISentinel sentinel) {
                var instance = sentinel.getBoxManager().getBoxInstance(payload.boxName());
                if (instance != null)
                    instance.setYRotation(payload.yRot(), payload.yRot0());
            }
        });
    }
}
