package com.ombremoon.sentinellib.api.compat;

import com.ombremoon.sentinellib.util.ReflectionHelper;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.keyframe.AnimationPoint;
import software.bernie.geckolib.animation.keyframe.BoneAnimationQueue;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

import java.util.Map;

@SuppressWarnings("unchecked")
public class ServerAnimationProcessor<T extends GeoSentinel<T>> extends AnimationProcessor<GeoSentinel<T>> {
    public ServerAnimationProcessor(ServerGeoModel<T> model) {
        super((GeoModel<GeoSentinel<T>>) model);
    }

    @Override
    public void tickAnimation(GeoSentinel<T> animatable, GeoModel<GeoSentinel<T>> model, AnimatableManager<GeoSentinel<T>> animatableManager, double animTime, AnimationState<GeoSentinel<T>> state, boolean crashWhenCantFindBone) {
        Map<String, BoneSnapshot> boneSnapshots = updateBoneSnapshots(animatableManager.getBoneSnapshotCollection());

        for (AnimationController<GeoSentinel<T>> controller : animatableManager.getAnimationControllers().values()) {
            if (this.reloadAnimations) {
                controller.forceAnimationReset();
                controller.getBoneAnimationQueues().clear();
            }

            ReflectionHelper.AnimationControllerAccess.isJustStarting.set(controller, animatableManager.isFirstTick());

            state.withController(controller);
            controller.process(model, state, ReflectionHelper.AnimationProcessorAccess.bones.apply(this), boneSnapshots, animTime, crashWhenCantFindBone);

            for (BoneAnimationQueue boneAnimation : controller.getBoneAnimationQueues().values()) {
                GeoBone bone = boneAnimation.bone();
                BoneSnapshot snapshot = boneSnapshots.get(bone.getName());
                BoneSnapshot initialSnapshot = bone.getInitialSnapshot();

                AnimationPoint rotXPoint = boneAnimation.rotationXQueue().poll();
                AnimationPoint rotYPoint = boneAnimation.rotationYQueue().poll();
                AnimationPoint rotZPoint = boneAnimation.rotationZQueue().poll();
                AnimationPoint posXPoint = boneAnimation.positionXQueue().poll();
                AnimationPoint posYPoint = boneAnimation.positionYQueue().poll();
                AnimationPoint posZPoint = boneAnimation.positionZQueue().poll();
                AnimationPoint scaleXPoint = boneAnimation.scaleXQueue().poll();
                AnimationPoint scaleYPoint = boneAnimation.scaleYQueue().poll();
                AnimationPoint scaleZPoint = boneAnimation.scaleZQueue().poll();
                EasingType easingType = ReflectionHelper.AnimationControllerAccess.overrideEasingTypeFunction.apply(controller).apply(animatable);

                if (rotXPoint != null && rotYPoint != null && rotZPoint != null) {
                    bone.setRotX((float)EasingType.lerpWithOverride(rotXPoint, easingType) + initialSnapshot.getRotX());
                    bone.setRotY((float)EasingType.lerpWithOverride(rotYPoint, easingType) + initialSnapshot.getRotY());
                    bone.setRotZ((float)EasingType.lerpWithOverride(rotZPoint, easingType) + initialSnapshot.getRotZ());
                    snapshot.updateRotation(bone.getRotX(), bone.getRotY(), bone.getRotZ());
                    snapshot.startRotAnim();
                    bone.markRotationAsChanged();
                }

                if (posXPoint != null && posYPoint != null && posZPoint != null) {
                    bone.setPosX((float)EasingType.lerpWithOverride(posXPoint, easingType));
                    bone.setPosY((float)EasingType.lerpWithOverride(posYPoint, easingType));
                    bone.setPosZ((float)EasingType.lerpWithOverride(posZPoint, easingType));
                    snapshot.updateOffset(bone.getPosX(), bone.getPosY(), bone.getPosZ());
                    snapshot.startPosAnim();
                    bone.markPositionAsChanged();
                }

                if (scaleXPoint != null && scaleYPoint != null && scaleZPoint != null) {
                    bone.setScaleX((float)EasingType.lerpWithOverride(scaleXPoint, easingType));
                    bone.setScaleY((float)EasingType.lerpWithOverride(scaleYPoint, easingType));
                    bone.setScaleZ((float)EasingType.lerpWithOverride(scaleZPoint, easingType));
                    snapshot.updateScale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
                    snapshot.startScaleAnim();
                    bone.markScaleAsChanged();
                }
            }
        }

        this.reloadAnimations = false;
        double resetTickLength = animatable.getBoneResetTime();

        for (GeoBone bone : getRegisteredBones()) {
            if (!bone.hasRotationChanged()) {
                BoneSnapshot initialSnapshot = bone.getInitialSnapshot();
                BoneSnapshot saveSnapshot = boneSnapshots.get(bone.getName());

                if (saveSnapshot.isRotAnimInProgress())
                    saveSnapshot.stopRotAnim(animTime);

                double percentageReset = Math.min((animTime - saveSnapshot.getLastResetRotationTick()) / resetTickLength, 1);

                bone.setRotX((float) Mth.lerp(percentageReset, saveSnapshot.getRotX(), initialSnapshot.getRotX()));
                bone.setRotY((float)Mth.lerp(percentageReset, saveSnapshot.getRotY(), initialSnapshot.getRotY()));
                bone.setRotZ((float)Mth.lerp(percentageReset, saveSnapshot.getRotZ(), initialSnapshot.getRotZ()));

                if (percentageReset >= 1)
                    saveSnapshot.updateRotation(bone.getRotX(), bone.getRotY(), bone.getRotZ());
            }

            if (!bone.hasPositionChanged()) {
                BoneSnapshot initialSnapshot = bone.getInitialSnapshot();
                BoneSnapshot saveSnapshot = boneSnapshots.get(bone.getName());

                if (saveSnapshot.isPosAnimInProgress())
                    saveSnapshot.stopPosAnim(animTime);

                double percentageReset = Math.min((animTime - saveSnapshot.getLastResetPositionTick()) / resetTickLength, 1);

                bone.setPosX((float)Mth.lerp(percentageReset, saveSnapshot.getOffsetX(), initialSnapshot.getOffsetX()));
                bone.setPosY((float)Mth.lerp(percentageReset, saveSnapshot.getOffsetY(), initialSnapshot.getOffsetY()));
                bone.setPosZ((float)Mth.lerp(percentageReset, saveSnapshot.getOffsetZ(), initialSnapshot.getOffsetZ()));

                if (percentageReset >= 1)
                    saveSnapshot.updateOffset(bone.getPosX(), bone.getPosY(), bone.getPosZ());
            }

            if (!bone.hasScaleChanged()) {
                BoneSnapshot initialSnapshot = bone.getInitialSnapshot();
                BoneSnapshot saveSnapshot = boneSnapshots.get(bone.getName());

                if (saveSnapshot.isScaleAnimInProgress())
                    saveSnapshot.stopScaleAnim(animTime);

                double percentageReset = Math.min((animTime - saveSnapshot.getLastResetScaleTick()) / resetTickLength, 1);

                bone.setScaleX((float)Mth.lerp(percentageReset, saveSnapshot.getScaleX(), initialSnapshot.getScaleX()));
                bone.setScaleY((float)Mth.lerp(percentageReset, saveSnapshot.getScaleY(), initialSnapshot.getScaleY()));
                bone.setScaleZ((float)Mth.lerp(percentageReset, saveSnapshot.getScaleZ(), initialSnapshot.getScaleZ()));

                if (percentageReset >= 1)
                    saveSnapshot.updateScale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
            }
        }

        getRegisteredBones().forEach(GeoBone::resetStateChanges);
        ReflectionHelper.AnimatableManagerAccess.isFirstTick.set(animatableManager, false);
    }

    private Map<String, BoneSnapshot> updateBoneSnapshots(Map<String, BoneSnapshot> snapshots) {
        for (GeoBone bone : getRegisteredBones()) {
            if (!snapshots.containsKey(bone.getName()))
                snapshots.put(bone.getName(), BoneSnapshot.copy(bone.getInitialSnapshot()));
        }

        return snapshots;
    }
}
