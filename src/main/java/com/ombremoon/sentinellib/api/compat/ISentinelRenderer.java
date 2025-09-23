package com.ombremoon.sentinellib.api.compat;

import com.ombremoon.sentinellib.networking.PayloadHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

import java.util.List;
import java.util.Optional;

public interface ISentinelRenderer<T extends Entity & GeoAnimatable & GeoSentinel<?>> {

    T getSentinel();

    List<String> getSentinelBones();

    default void trackSentinelModel(BakedGeoModel model) {
        for (String name : getSentinelBones()) {
            Optional<GeoBone> optional = model.getBone(name);
            if (optional.isPresent() && getSentinel().level().isClientSide) {
                GeoBone bone = optional.get();
                bone.setTrackingMatrices(true);
            }
        }
    }
}
