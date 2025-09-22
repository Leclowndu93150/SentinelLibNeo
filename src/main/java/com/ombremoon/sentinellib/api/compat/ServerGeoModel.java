package com.ombremoon.sentinellib.api.compat;

import com.ombremoon.sentinellib.util.MatrixHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.RenderUtil;

import java.util.Optional;

@SuppressWarnings("unchecked")
public abstract class ServerGeoModel<T extends GeoSentinel<T>> extends GeoModel<T> {
    private final ServerAnimationProcessor<T> processor = new ServerAnimationProcessor<>(this);

    private BakedGeoModel currentModel = null;
    private double animTime;
    private double lastGameTickTime;
    private long lastRenderedInstance = -1;

    @Override
    public final ResourceLocation getTextureResource(T animatable) {
        return null;
    }

    @Override
    public BakedGeoModel getBakedModel(ResourceLocation location) {
        BakedGeoModel model = SentinelModelReloadListener.geBakedModels().get(location);

        if (model == null) {
            if (!location.getPath().contains("geo_sentinel/"))
                throw GeckoLibConstants.exception(location, "Invalid model resource path provided - Sentinel model data must be placed in data/<modid>/geo_sentinel/");

            throw GeckoLibConstants.exception(location, "Unable to find model");
        }

        if (model != this.currentModel) {
            this.processor.setActiveModel(model);
            this.currentModel = model;
        }

        return this.currentModel;
    }

    @Override
    public Animation getAnimation(T animatable, String name) {
        ResourceLocation location = getAnimationResource(animatable);
        BakedAnimations bakedAnimations = SentinelAnimationReloadListener.getBakedAnimations().get(location);

        if (bakedAnimations == null) {
            if (!location.getPath().contains("sentinel_anim/"))
                throw GeckoLibConstants.exception(location, "Invalid animation data path provided - Sentinel animation data must be placed in data/<modid>/sentinel_anim/");

            throw GeckoLibConstants.exception(location, "Unable to find animation file.");
        }

        return bakedAnimations.getAnimation(name);
    }

    public Vec3 getBonePivot(T animatable, String name) {
        var optional = this.getBone(name);
        if (optional.isPresent()) {
            GeoBone bone = optional.get();
            return new Vec3(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        }
        return Vec3.ZERO;
    }

    public Optional<GeoBone> getRootBone(T animatable) {
        return this.currentModel != null ? this.currentModel.getBone(BuiltInRegistries.ENTITY_TYPE.getKey(animatable.getSentinel().getType()).getPath()) : Optional.empty();
    }

    public Vec3 getBonePosition(T animatable, String name) {
        var optional = this.getBone(name);
        if (optional.isPresent()) {
            GeoBone bone = optional.get();
            Entity sentinel = animatable.getSentinel();
            Matrix4f worldSpaceMatrix = new Matrix4f();
            while (bone.getParent() != null) {
                Matrix4f localMatrix = new Matrix4f();
                MatrixHelper.translateMatrixToBone(localMatrix, bone);
                MatrixHelper.translateToPivotPoint(localMatrix, bone);
                MatrixHelper.rotateMatrixAroundBone(localMatrix, bone);
                MatrixHelper.scaleMatrixForBone(localMatrix, bone);
                Matrix4f boneMatrix = new Matrix4f(localMatrix);
                worldSpaceMatrix = RenderUtil.translateMatrix(new Matrix4f(boneMatrix), sentinel.position().toVector3f());
                bone = bone.getParent();
            }
            return new Vec3(worldSpaceMatrix.m30(), worldSpaceMatrix.m31(), worldSpaceMatrix.m32());
        }
        return Vec3.ZERO;
    }

    public void assignBoneMatrices(T animatable) {
        BakedGeoModel model = getBakedModel(getModelResource(animatable));
        for (GeoBone bone : model.topLevelBones()) {
            setBoneWorldSpaceMatrix(animatable, bone);
        }
    }

    public void setBoneWorldSpaceMatrix(T animatable, GeoBone bone) {
        Entity sentinel = animatable.getSentinel();
        Matrix4f localMatrix = new Matrix4f();
        MatrixHelper.translateMatrixToBone(localMatrix, bone);
        MatrixHelper.translateToPivotPoint(localMatrix, bone);
        MatrixHelper.rotateMatrixAroundBone(localMatrix, bone);
        MatrixHelper.scaleMatrixForBone(localMatrix, bone);

        Matrix4f boneMatrix = bone.getParent() != null ? new Matrix4f(bone.getParent().getLocalSpaceMatrix()) : new Matrix4f();
        boneMatrix.mul(localMatrix);

        Matrix4f worldSpaceMatrix = RenderUtil.translateMatrix(new Matrix4f(boneMatrix), sentinel.position().toVector3f());
        bone.setLocalSpaceMatrix(boneMatrix);
        bone.setWorldSpaceMatrix(worldSpaceMatrix);

        for (GeoBone geoBone : bone.getChildBones()) {
            setBoneWorldSpaceMatrix(animatable, geoBone);
        }
    }

/*    public Vec3 getBonePosition(T animatable, String name) {
        Matrix4f matrix4f = new Matrix4f();
        Entity sentinel = animatable.getSentinel();
        Vec3 root = sentinel.position();
        Vec3 bonePos = root.add(getBonePivot(animatable, name).scale((double) 1/16));
        Matrix4f centerMatrix = new Matrix4f().translate((float) bonePos.x, (float) bonePos.y, (float) bonePos.z);
        var optional = this.getBone(name);
        if (optional.isPresent()) {
            GeoBone bone = optional.get();
        }
            return bonePos;
    }*/

    public void handleServerAnimations(T animatable, long instanceId, AnimationState<GeoSentinel<T>> animationState) {
        AnimatableManager<T> animatableManager = animatable.getAnimatableInstanceCache().getManagerForId(instanceId);
        Double currentTick = animationState.getData(DataTickets.TICK);

        if (!(animatable instanceof Entity entity))
            return;

        if (currentTick == null)
            currentTick = (double) entity.tickCount;

        if (animatableManager.getFirstTickTime() == -1)
            animatableManager.startedAt(currentTick);

        double currentFrameTime = animatable instanceof GeoReplacedEntity ? currentTick : currentTick - animatableManager.getFirstTickTime();
        boolean isReRender = !animatableManager.isFirstTick() && currentFrameTime == animatableManager.getLastUpdateTime();

        if (isReRender && instanceId == this.lastRenderedInstance)
            return;

        if (animatable.shouldPlayAnimsWhileGamePaused()) {
            animatableManager.updatedAt(currentFrameTime);

            double lastUpdateTime = animatableManager.getLastUpdateTime();
            this.animTime += lastUpdateTime - this.lastGameTickTime;
            this.lastGameTickTime = lastUpdateTime;
        }

        animationState.animationTick = this.animTime;
        this.lastRenderedInstance = instanceId;

        processor.preAnimationSetup(animationState, this.animTime);

        if (!processor.getRegisteredBones().isEmpty())
            processor.tickAnimation(animatable, (GeoModel<GeoSentinel<T>>) this, (AnimatableManager<GeoSentinel<T>>) animatableManager, this.animTime, animationState, crashIfBoneMissing());

        setCustomAnimations(animatable, instanceId, (AnimationState<T>) animationState);
    }
}
