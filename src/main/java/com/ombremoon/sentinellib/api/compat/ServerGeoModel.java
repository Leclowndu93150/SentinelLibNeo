package com.ombremoon.sentinellib.api.compat;

import com.ombremoon.sentinellib.Constants;
import com.ombremoon.sentinellib.util.MatrixHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
    public Optional<GeoBone> getBone(String name) {
        return Optional.ofNullable(this.processor.getBone(name));
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

    public void assignBoneMatrices(T animatable) {
        BakedGeoModel model = getBakedModel(getModelResource(animatable));
        for (GeoBone bone : model.topLevelBones()) {
            setBoneWorldSpaceMatrix(animatable, bone);
        }
    }

    public void setBoneWorldSpaceMatrix(T animatable, GeoBone bone) {
        // This matrix represents the transformation of the bone in relation to its parent
        Matrix4f localMatrix = new Matrix4f();
        MatrixHelper.translateMatrixToBone(localMatrix, bone);
        MatrixHelper.translateToPivotPoint(localMatrix, bone);
        MatrixHelper.rotateMatrixAroundBone(localMatrix, bone);
        MatrixHelper.scaleMatrixForBone(localMatrix, bone);
        MatrixHelper.translateAwayFromPivotPoint(localMatrix, bone); // Crucial for correct pivot-based transformations

        // This is the bone's transformation in model space.
        // We get the parent's model space matrix and multiply this bone's local transform onto it.
        // For root bones, the parent matrix is the identity matrix.
        Matrix4f modelSpaceMatrix = bone.getParent() != null ? new Matrix4f(bone.getParent().getLocalSpaceMatrix()) : new Matrix4f();
        modelSpaceMatrix.mul(localMatrix);

        // Store the calculated model-space matrix in the bone.
        // NOTE: The field name `localSpaceMatrix` is a bit confusing here, as it's storing the model-space matrix.
        // This matches the old code's intent to avoid further changes.
        bone.setLocalSpaceMatrix(modelSpaceMatrix);

        // Now, calculate the final world-space matrix.
        // This includes the entity's position and rotation in the world.
        Entity sentinel = animatable.getSentinel();
        Matrix4f worldSpaceMatrix = new Matrix4f();

        // 1. Translate to the entity's world position
        worldSpaceMatrix.translate(sentinel.position().toVector3f());

        // 2. Apply the entity's rotation, mimicking the client-side renderer's 180-degree flip
        float yaw = (sentinel instanceof LivingEntity living) ? living.yBodyRot : sentinel.getYRot();
        worldSpaceMatrix.rotate((180 - yaw) * Mth.DEG_TO_RAD, 0, 1, 0);

        // 3. Apply the bone's full model-space transformation
        worldSpaceMatrix.mul(modelSpaceMatrix);

        bone.setWorldSpaceMatrix(worldSpaceMatrix);

        // Recurse for all children
        for (GeoBone childBone : bone.getChildBones()) {
            setBoneWorldSpaceMatrix(animatable, childBone);
        }
    }

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

        animatableManager.updatedAt(currentFrameTime);
        double lastUpdateTime = animatableManager.getLastUpdateTime();
        this.animTime += lastUpdateTime - this.lastGameTickTime;
        this.lastGameTickTime = lastUpdateTime;

        animationState.animationTick = this.animTime;
        this.lastRenderedInstance = instanceId;

        processor.preAnimationSetup(animationState, this.animTime);

        if (!processor.getRegisteredBones().isEmpty())
            processor.tickAnimation(animatable, (GeoModel<GeoSentinel<T>>) this, (AnimatableManager<GeoSentinel<T>>) animatableManager, this.animTime, animationState, crashIfBoneMissing());

        setCustomAnimations(animatable, instanceId, (AnimationState<T>) animationState);
    }
}
