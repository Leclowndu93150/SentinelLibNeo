package com.ombremoon.sentinellib.api.compat;

import com.ombremoon.sentinellib.api.box.SentinelBox;
import com.ombremoon.sentinellib.common.ISentinel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
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

            if (getSentinelModel().getAnimationProcessor().getRegisteredBones().isEmpty())
                getSentinelModel().getAnimationProcessor().setActiveModel(geoModel);

            Vec3 velocity = entity.getDeltaMovement();
            float avgVelocity = (float)(Math.abs(velocity.x) + Math.abs(velocity.z) / 2f);
            AnimationState<T> animationState = new AnimationState<>(animatable, 0, 0, 0, avgVelocity >= 0.015F);
            long instanceId = entity.getId();
            ServerGeoModel<T> currentModel = getSentinelModel();

            animationState.setData(DataTickets.TICK, animatable.getTick(animatable));
            animationState.setData(DataTickets.ENTITY, entity);
            currentModel.addAdditionalStateData(animatable, instanceId, animationState::setData);
            currentModel.handleServerAnimations(animatable, instanceId, (AnimationState<GeoSentinel<T>>) animationState);

            getSentinelModel().assignBoneMatrices((T) this);
        }
    }

    default List<String> getSentinelBones() {
        return getSentinelBoxes().stream().map(SentinelBox::getName).toList();
    }
}
