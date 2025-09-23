package com.ombremoon.sentinellib.api.compat;

import com.ombremoon.sentinellib.api.box.SentinelBox;
import com.ombremoon.sentinellib.common.ISentinel;
import com.ombremoon.sentinellib.networking.PayloadHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;

import java.util.List;

@SuppressWarnings("unchecked")
public interface GeoSentinel<T extends GeoSentinel<T>> extends ISentinel, GeoEntity {

    ServerGeoModel<T> getSentinelModel();

    @Override
    default void tickBoxes() {
        ISentinel.super.tickBoxes();
        if (!getLevel().isClientSide) {
            Entity entity = getSentinel();
            T animatable = (T) entity;
            BakedGeoModel geoModel = getSentinelModel().getBakedModel(getSentinelModel().getModelResource(animatable));
            Vec3 velocity = entity.getDeltaMovement();
            float avgVelocity = (float)(Math.abs(velocity.x) + Math.abs(velocity.z) / 2f);
            AnimationState<T> animationState = new AnimationState<>(animatable, 0, 0, 0, avgVelocity >= 0.015F);
            long instanceId = entity.getId();
            ServerGeoModel<T> currentModel = getSentinelModel();

            animationState.setData(DataTickets.TICK, animatable.getTick(animatable));
            animationState.setData(DataTickets.ENTITY, entity);
            currentModel.addAdditionalStateData(animatable, instanceId, animationState::setData);
            currentModel.handleServerAnimations(animatable, instanceId, (AnimationState<GeoSentinel<T>>) animationState);

            enableBoneTracking((T) this);
            getSentinelModel().assignBoneMatrices((T) this);
            syncBonesToClients();
        }
    }
    
    default void enableBoneTracking(T animatable) {
        BakedGeoModel model = getSentinelModel().getBakedModel(getSentinelModel().getModelResource(animatable));
        for (GeoBone bone : model.topLevelBones()) {
            enableBoneTrackingRecursive(bone);
        }
    }
    
    default void enableBoneTrackingRecursive(GeoBone bone) {
        bone.setTrackingMatrices(true);
        for (GeoBone child : bone.getChildBones()) {
            enableBoneTrackingRecursive(child);
        }
    }
    
    default void syncBonesToClients() {
        for (String boneName : getSentinelBones()) {
            var bone = getSentinelModel().getBone(boneName);
            if (bone.isPresent()) {
                var geoBone = bone.get();
                var worldPos = geoBone.getWorldPosition();
                var rotVector = geoBone.getRotationVector();
                var scaleVector = geoBone.getScaleVector();
                
                var instance = getBoxManager().getBoxInstance(boneName);
                if (instance != null) {
                    instance.setAnchorPoint(new Vec3(worldPos.x, worldPos.y, worldPos.z));
                    instance.setScaleFactor(new Vec3(scaleVector.x, scaleVector.y, scaleVector.z));
                    instance.xRot = (float) Math.toDegrees(rotVector.x);
                    instance.yRot = (float) Math.toDegrees(rotVector.y);
                    instance.zRot = (float) Math.toDegrees(rotVector.z);
                    
                    PayloadHandler.syncGeoBoneToClients(
                            getSentinel().getId(),
                            boneName,
                            new Vector3f((float) worldPos.x, (float) worldPos.y, (float) worldPos.z),
                            new Vector3f(instance.xRot, instance.yRot, instance.zRot),
                            new Vector3f((float) scaleVector.x, (float) scaleVector.y, (float) scaleVector.z)
                    );
                }
            }
        }
    }

    default List<String> getSentinelBones() {
        return getSentinelBoxes().stream().map(SentinelBox::getName).toList();
    }
}
