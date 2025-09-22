package com.ombremoon.sentinellib.networking;

import com.ombremoon.sentinellib.CommonClass;
import com.ombremoon.sentinellib.common.ISentinel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

public record ServerboundSyncGeoBone(int entityId, String boxName, Vector3f posVector, Vector3f rotVector, Vector3f scaleVector) implements CustomPacketPayload {
    public static final Type<ServerboundSyncGeoBone> TYPE = new Type<>(CommonClass.customLocation("sync_geo_bone"));
    public static final StreamCodec<ByteBuf, ServerboundSyncGeoBone> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSyncGeoBone::entityId,
            ByteBufCodecs.STRING_UTF8, ServerboundSyncGeoBone::boxName,
            ByteBufCodecs.VECTOR3F, ServerboundSyncGeoBone::posVector,
            ByteBufCodecs.VECTOR3F, ServerboundSyncGeoBone::rotVector,
            ByteBufCodecs.VECTOR3F, ServerboundSyncGeoBone::scaleVector,
            ServerboundSyncGeoBone::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ServerboundSyncGeoBone payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = context.player().level();
            Entity entity = level.getEntity(payload.entityId());

            if (entity == null)
                return;

            if (entity instanceof ISentinel sentinel) {
                var instance = sentinel.getBoxManager().getBoxInstance(payload.boxName());
                if (instance != null) {
                    var pos = payload.posVector;
                    var rot = payload.rotVector;
                    var scale = payload.scaleVector;
                    instance.setAnchorPoint(new Vec3(pos.x, pos.y, pos.z));
                    instance.setScaleFactor(new Vec3(scale.x, scale.y, scale.z));
                    instance.xRot = rot.x;
                    instance.yRot = rot.y;
                    instance.zRot = rot.z;
                }
            }
        });
    }
}
