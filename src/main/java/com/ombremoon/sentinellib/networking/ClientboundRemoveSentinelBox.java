package com.ombremoon.sentinellib.networking;

import com.ombremoon.sentinellib.CommonClass;
import com.ombremoon.sentinellib.api.box.SentinelBox;
import com.ombremoon.sentinellib.common.ISentinel;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundRemoveSentinelBox(int entityId, String boxName) implements CustomPacketPayload {
    public static final Type<ClientboundRemoveSentinelBox> TYPE = new Type<>(CommonClass.customLocation("remove_box"));
    public static final StreamCodec<ByteBuf, ClientboundRemoveSentinelBox> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundRemoveSentinelBox::entityId,
            ByteBufCodecs.STRING_UTF8, ClientboundRemoveSentinelBox::boxName,
            ClientboundRemoveSentinelBox::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ClientboundRemoveSentinelBox payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            Entity entity = level.getEntity(payload.entityId());

            if (entity == null)
                return;

            if (entity instanceof ISentinel sentinel) {
                SentinelBox sentinelBox = sentinel.getBoxFromID(payload.boxName());

                if (sentinelBox == null)
                    return;

                sentinel.removeSentinelInstance(sentinelBox);
            }
        });
    }
}
