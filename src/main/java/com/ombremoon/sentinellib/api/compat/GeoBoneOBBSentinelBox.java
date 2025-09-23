package com.ombremoon.sentinellib.api.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ombremoon.sentinellib.CommonClass;
import com.ombremoon.sentinellib.Constants;
import com.ombremoon.sentinellib.api.box.BoxInstance;
import com.ombremoon.sentinellib.api.box.OBBSentinelBox;
import com.ombremoon.sentinellib.util.MatrixHelper;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.object.BakedModelFactory;

import java.util.function.*;

public final class GeoBoneOBBSentinelBox extends OBBSentinelBox {
    public GeoBoneOBBSentinelBox(Builder builder) {
        super(builder);
    }

    @Override
    public void renderBox(BoxInstance instance, Entity entity, PoseStack poseStack, VertexConsumer vertexConsumer, float partialTicks, float isRed) {
        /*if (ADVANCED DEBUG MODE) {
            poseStack.pushPose();
            Vec3 center = instance.getCenter();
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, this.getSentinelBB(instance).move(-center.x, -center.y, -center.z), 1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }*/

        poseStack.pushPose();
        Matrix4f transpose = MatrixHelper.getMovementMatrix(entity, instance, partialTicks, MoverType.BONE);
        Constants.LOG.info("{}", GeckoLibCache.getBakedModels().get(CommonClass.customLocation("geo/entity/ice_mist.geo.json")).getBone("mist1").get().getWorldPosition());
        var transpose1 = ((GeoSentinel<?>)entity).getSentinelModel().getBone("mist1");
        if (transpose1.isPresent())
            Constants.LOG.info("Hi");
        Vec3 offset = this.getBoxOffset();
        float x = (float) (transpose.m30() - entity.position().x);
        float y = (float) (transpose.m31() - entity.position().y);
        float z = (float) (transpose.m32() - entity.position().z);
        poseStack.translate(x, y, z);
        poseStack.mulPose(MatrixHelper.quaternion(transpose));
        poseStack.translate(offset.x, offset.y, offset.z);
        Matrix4f matrix = poseStack.last().pose();
        PoseStack.Pose pose = poseStack.last();
        Vec3 scale = this.getScaleFactor(instance);
        Vec3 vertex = this.getVertexPos().multiply(scale.x, scale.y, scale.z);
        float maxX = (float)(offset.x + vertex.x);
        float maxY = (float)(offset.y + vertex.y);
        float maxZ = (float)(offset.z + vertex.z);
        float minX = (float)(offset.x - vertex.x);
        float minY = (float)(offset.y - vertex.y);
        float minZ = (float)(offset.z - vertex.z);
        renderBox(vertexConsumer, matrix, minX, minY, minZ, maxX, maxY, maxZ, isRed, pose);
        poseStack.popPose();
    }

    @Override
    public Vec3 getScaleFactor(BoxInstance instance) {
        Vec3 vec3 = instance.getScaleFactor();
        return vec3 != null ? vec3 : Vec3.ZERO;
    }

    public static class Builder extends OBBSentinelBox.Builder {

        public Builder(String name) {
            super(name);
            this.moverType = MoverType.BONE;
        }

        /**
         * Creates a new builder.<br> The name should be equal to the name of the bone the box should track.
         * @param name The name of the sentinel box
         * @return The builder
         */
        public static Builder of(String name) { return new Builder(name); }

        public Builder sizeAndOffset(float xSize) {
            sizeAndOffset(xSize, xSize, 0, 0, 0);
            return this;
        }

        public Builder sizeAndOffset(float xSize, float xOffset, float yOffset, float zOffset) {
            sizeAndOffset(xSize, xSize, xOffset, yOffset, zOffset);
            return this;
        }

        public Builder sizeAndOffset(float xzSize, float ySize, float xOffset, float yOffset, float zOffset) {
            sizeAndOffset(xzSize, ySize, xzSize, xOffset, yOffset, zOffset);
            return this;
        }

        /**
         * Defines the size and offset of the sentinel box.
         * @param xVertex The x distance from the center of the box to the top right vertex
         * @param yVertex The y distance from the center of the box to the top right vertex
         * @param zVertex The z distance from the center of the box to the top right vertex
         * @return The builder
         */
        public Builder sizeAndOffset(float xVertex, float yVertex, float zVertex, float xOffset, float yOffset, float zOffset) {
            double xSize = Math.abs(xVertex);
            double ySize = Math.abs(yVertex);
            double zSize = Math.abs(zVertex);
            double maxLength = Math.max(xSize, Math.max(ySize, zSize));
            this.vertexPos = new Vec3(xVertex, yVertex, zVertex);
            this.boxOffset = new Vec3(xOffset, yOffset, zOffset);
            this.aabb = new AABB(maxLength, maxLength, maxLength, -maxLength, -maxLength, -maxLength);
            return this;
        }

        /**
         * Defines the duration the box should tick.
         * @param durationTicks
         * @return The builder
         */
        public Builder boxDuration(int durationTicks) {
            this.duration = durationTicks;
            return this;
        }

        /**
         * Sets the box to have an infinite duration.<br> Must define a predicate for when the box should stop.
         * @param stopPredicate
         * @return The builder
         */
        public Builder noDuration(Predicate<Entity> stopPredicate) {
            this.hasDuration = false;
            this.stopPredicate = stopPredicate;
            return this;
        }

        /**
         * Callback to define when/if a box should stop before its intended duration.
         * @param stopPredicate
         * @return The builder
         */
        public Builder stopIf(Predicate<Entity> stopPredicate) {
            this.stopPredicate = stopPredicate;
            return this;
        }

        /**
         * Callback to define when the box should be active.
         * @param activeDuration
         * @return The builder
         */
        public Builder activeTicks(BiPredicate<Entity, Integer> activeDuration) {
            this.activeDuration = activeDuration;
            return this;
        }

        /**
         * Callback to determine what entities should be affected by the box
         * @param attackCondition
         * @return The builder
         */
        public Builder attackCondition(BiPredicate<Entity, LivingEntity> attackCondition) {
            this.attackCondition = attackCondition;
            return this;
        }

        /**
         * Callback to add extra functionality to the sentinel box when triggered
         * @param startConsumer
         * @return
         */
        public Builder onBoxTrigger(BiConsumer<Entity, BoxInstance> startConsumer) {
            this.boxStart = startConsumer;
            return this;
        }

        /**
         * Callback to add extra functionality to the sentinel box every tick
         * @param tickConsumer
         * @return
         */
        public Builder onBoxTick(BiConsumer<Entity, BoxInstance> tickConsumer) {
            this.boxTick = tickConsumer;
            return this;
        }

        /**
         * Callback to add extra functionality to the sentinel box at the end of its duration
         * @param stopConsumer
         * @return
         */
        public Builder onBoxStop(BiConsumer<Entity, BoxInstance> stopConsumer) {
            this.boxStop = stopConsumer;
            return this;
        }

        /**
         * Callback to add extra functionality to the sentinel box while active
         * @param activeConsumer
         * @return
         */
        public Builder onActiveTick(BiConsumer<Entity, BoxInstance> activeConsumer) {
            this.boxActive = activeConsumer;
            return this;
        }

        public Builder onCollisionTick(BiConsumer<Entity, LivingEntity> attackConsumer) {
            this.boxCollision = attackConsumer;
            return this;
        }

        /**
         * Callback to add extra functionality to the sentinel box when colliding with an entity
         * @param attackConsumer
         * @return
         */
        public Builder onHurtTick(BiConsumer<Entity, LivingEntity> attackConsumer) {
            this.boxHurt = attackConsumer;
            return this;
        }

        /**
         * Defines the damage type and amount the box causes while active
         * @param damageType
         * @return The builder
         */
        public Builder typeDamage(ResourceKey<DamageType> damageType, BiFunction<Entity, LivingEntity, Float> damageFunction) {
            this.damageType = damageType;
            this.damageFunction = damageFunction;
            return this;
        }

        public GeoBoneOBBSentinelBox build() { return new GeoBoneOBBSentinelBox(this); }
    }
}
