package com.ombremoon.sentinellib.example;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ombremoon.sentinellib.Constants;
import com.ombremoon.sentinellib.common.ISentinelRenderer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.List;

public class IceMistRenderer extends GeoEntityRenderer<IceMist> implements ISentinelRenderer<IceMist> {
    public IceMistRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new IceMistModel());
    }

    @Override
    public IceMist getSentinel() {
        return this.animatable;
    }

    @Override
    public List<String> getSentinelBones() {
        return ObjectArrayList.of("mist_center", "mist1");
    }

    @Override
    public void renderFinal(PoseStack poseStack, IceMist animatable, BakedGeoModel model, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, int colour) {
        this.model.getBone("mist_rot7").ifPresent(geoBone -> {
            Constants.LOG.info("{}", geoBone.getWorldPosition());
        });
        super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, colour);
    }
}
