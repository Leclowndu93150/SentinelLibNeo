package com.ombremoon.sentinellib.example.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.ombremoon.sentinellib.Constants;
import com.ombremoon.sentinellib.api.compat.ISentinelRenderer;
import com.ombremoon.sentinellib.example.entity.IceMist;
import com.ombremoon.sentinellib.example.model.IceMistModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.List;

public class IceMistRenderer extends GeoEntityRenderer<IceMist> implements ISentinelRenderer<IceMist> {
    public IceMistRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new IceMistModel());
    }

    @Override
    protected void applyRotations(IceMist animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
        poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getYRot()));
    }

    @Override
    public void renderRecursively(PoseStack poseStack, IceMist animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
//        Constants.LOG.info("{}", this.entityRenderTranslations);
//        if (bone.getName().equals("mist5"))
//            Constants.LOG.info("{}", bone.getWorldPosition());
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
    public boolean shouldRender(IceMist livingEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }
}
