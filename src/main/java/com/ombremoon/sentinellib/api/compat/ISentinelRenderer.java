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
            if (optional.isPresent()) {
                GeoBone bone = optional.get();
                var instance = getSentinel().getBoxManager().getBoxInstance(name);
                if (instance != null) {
                    Entity owner = instance.getBoxOwner();
                    var pos = bone.getWorldPosition();
                    var rot = bone.getRotationVector();
                    var boneScale = bone.getScaleVector();
                    var scale = new Vector3d(boneScale);
                    instance.setAnchorPoint(new Vec3(pos.x, pos.y, pos.z));

                    GeoBone geoBone = bone;
                    while (geoBone.getParent() != null) {
                        scale.mul(geoBone.getParent().getScaleVector());
                        geoBone = geoBone.getParent();
                    }
                    instance.setScaleFactor(new Vec3(scale.x, scale.y, scale.z));

                    float f = owner instanceof LivingEntity living ? living.yBodyRot : owner.getYRot();
                    instance.xRot = (float) Mth.wrapDegrees(Mth.RAD_TO_DEG * rot.x);
                    instance.yRot = -f + (float) Mth.wrapDegrees(Mth.RAD_TO_DEG * rot.y);
                    instance.zRot = (float) Mth.wrapDegrees(Mth.RAD_TO_DEG * rot.z);

                    PayloadHandler.syncGeoBone(
                            getSentinel().getId(),
                            name,
                            new Vector3f((float) pos.x, (float) pos.y, (float) pos.z),
                            new Vector3f(instance.xRot, instance.yRot, instance.zRot),
                            new Vector3f((float) scale.x, (float) scale.y, (float) scale.z)
                    );
                }
            }
        }
    }
}
