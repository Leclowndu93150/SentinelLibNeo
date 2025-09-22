package com.ombremoon.sentinellib.api.box;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ombremoon.sentinellib.common.ISentinel;
import com.ombremoon.sentinellib.util.MatrixHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.*;

/**
 * An OBB based sentinel box, used for more dynamic functionality.<br> Can be offset, translated, and rotated based on the {@link ISentinel Sentinels'} needs.
 */
public class OBBSentinelBox extends SentinelBox {
    public OBBSentinelBox(Builder builder) {
        super(builder);
    }

    @Override
    public AABB getSentinelBB(BoxInstance instance) {
        Vec3 center = instance.getCenter();
        float f0 = (float) this.getScaleFactor(instance).x;
        float f1 = (float) this.getScaleFactor(instance).y;
        float f2 = (float) this.getScaleFactor(instance).z;
        return this.aabb.inflate(this.aabb.maxX - this.aabb.minX, this.aabb.maxY - this.aabb.minY, this.aabb.maxZ - this.aabb.minZ).inflate((f0 - 1) * this.aabb.maxX, (f1 - 1) * this.aabb.maxY, (f2 - 1) * this.aabb.maxZ).move(center.x, center.y, center.z);
    }

    @Override
    public void renderBox(BoxInstance instance, Entity entity, PoseStack poseStack, VertexConsumer vertexConsumer, float partialTicks, float isRed) {
        /*if (ADVANCED DEBUG MODE) {
            poseStack.pushPose();
            Vec3 center = instance.getCenter();
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, this.getSentinelBB(instance).move(-center.x, -center.y, -center.z), 1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }*/

        Matrix4f transpose = MatrixHelper.getMovementMatrix(entity, instance, partialTicks, this.getMoverType());
        Vec3 offset = this.getBoxOffset();
        poseStack.pushPose();
        poseStack.translate(0.0F, (float) offset.y, 0.0F);
        poseStack.mulPose(MatrixHelper.quaternion(transpose).conjugate());
        poseStack.translate(0.0F, (float) -offset.y, 0.0F);
        Matrix4f matrix = poseStack.last().pose();
        if (this.getMoverType().isDefined()) {
            Vec3 vec3 = this.getBoxPath(instance, partialTicks);
            matrix.translate((float) vec3.x, (float) vec3.y, (float) vec3.z);
        }

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

    protected void renderBox(VertexConsumer vertexConsumer, Matrix4f matrix, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float isRed, PoseStack.Pose pose) {
        vertexConsumer.addVertex(matrix, minX, minY, minZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 1.0F, 0.0F, 0.0F);
        vertexConsumer.addVertex(matrix, maxX, minY, minZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 1.0F, 0.0F, 0.0F);
        vertexConsumer.addVertex(matrix, minX, minY, minZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, minX, maxY, minZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, minX, minY, minZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vertexConsumer.addVertex(matrix, minX, minY, maxZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vertexConsumer.addVertex(matrix, maxX, minY, minZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, maxX, maxY, minZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, maxX, maxY, minZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, -1.0F, 0.0F, 0.0F);
        vertexConsumer.addVertex(matrix, minX, maxY, minZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, -1.0F, 0.0F, 0.0F);
        vertexConsumer.addVertex(matrix, minX, maxY, minZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vertexConsumer.addVertex(matrix, minX, maxY, maxZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vertexConsumer.addVertex(matrix, minX, maxY, maxZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, -1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, minX, minY, maxZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, -1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, minX, minY, maxZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 1.0F, 0.0F, 0.0F);
        vertexConsumer.addVertex(matrix, maxX, minY, maxZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 1.0F, 0.0F, 0.0F);
        vertexConsumer.addVertex(matrix, maxX, minY, maxZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, 0.0F, -1.0F);
        vertexConsumer.addVertex(matrix, maxX, minY, minZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, 0.0F, -1.0F);
        vertexConsumer.addVertex(matrix, minX, maxY, maxZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 1.0F, 0.0F, 0.0F);
        vertexConsumer.addVertex(matrix, maxX, maxY, maxZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 1.0F, 0.0F, 0.0F);
        vertexConsumer.addVertex(matrix, maxX, minY, maxZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, maxX, maxY, maxZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, maxX, maxY, minZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, 0.0F, 1.0F);
        vertexConsumer.addVertex(matrix, maxX, maxY, maxZ).setColor(1.0F, isRed, isRed, 1.0F).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public List<Entity> getEntityCollisions(Entity owner, BoxInstance instance) {
        return owner.level().getEntities(owner, this.getSentinelBB(instance), entity -> {
            if (!(entity instanceof LivingEntity livingEntity))
                return false;

            return performSATTest(livingEntity, instance);
        });
    }

    private boolean performSATTest(LivingEntity target, BoxInstance instance) {
        BoxInstance targetBox = new BoxInstance(target);
        Vec3 direction = instance.getCenter().subtract(targetBox.getCenter());

        for (Vec3 normal : instance.instanceNormals) {
            if (!performSATTest(instance, targetBox, normal, direction))
                return false;
        }

        for (Vec3 normal : targetBox.instanceNormals) {
            if (!performSATTest(instance, targetBox, normal, direction))
                return false;
        }
        return true;
    }

    private boolean performSATTest(BoxInstance ownerBox, BoxInstance targetBox, Vec3 normal, Vec3 direction) {
        Vec3 maxProj1 = null, maxProj2 = null;
        double maxDot1 = -1, maxDot2 = -1;
        Vec3 distance = normal.dot(direction) > 0.0F ? direction : direction.scale(-1.0D);

        for (Vec3 vertexVector : ownerBox.instanceVertices) {
            Vec3 temp = normal.dot(vertexVector) > 0.0F ? vertexVector : vertexVector.scale(-1.0D);
            double dot = normal.dot(temp);

            if (dot > maxDot1) {
                maxDot1 = dot;
                maxProj1 = temp;
            }
        }

        for (Vec3 vertexVector : targetBox.instanceVertices) {
            Vec3 temp = normal.dot(vertexVector) > 0.0F ? vertexVector : vertexVector.scale(-1.0D);
            double dot = normal.dot(temp);

            if (dot > maxDot2) {
                maxDot2 = dot;
                maxProj2 = temp;
            }
        }

        return !(MatrixHelper.project(distance, normal).length() > MatrixHelper.project(maxProj1, normal).length() + MatrixHelper.project(maxProj2, normal).length());
    }

    @Override
    public Vec3 getBoxPath(BoxInstance instance, float partialTicks) {
        float xMovement = this.getBoxMovement(MovementAxis.X).apply(instance.tickCount, partialTicks);
        float yMovement = this.getBoxMovement(MovementAxis.Y).apply(instance.tickCount, partialTicks);
        float zMovement = this.getBoxMovement(MovementAxis.Z).apply(instance.tickCount, partialTicks);
        return new Vec3(xMovement, yMovement, zMovement);
    }

    @Override
    public Vec3 getBoxAngle(BoxInstance instance, float partialTicks) {
        float xRotation = this.getBoxRotation(MovementAxis.X).apply(instance.tickCount, partialTicks);
        float yRotation = this.getBoxRotation(MovementAxis.Y).apply(instance.tickCount, partialTicks);
        float zRotation = this.getBoxRotation(MovementAxis.Z).apply(instance.tickCount, partialTicks);
        return new Vec3(xRotation, yRotation, zRotation);
    }

    @Override
    public Vec3 getScaleFactor(BoxInstance instance) {
        float xScale = this.getBoxScale(MovementAxis.X).apply(instance.tickCount);
        float yScale = this.getBoxScale(MovementAxis.Y).apply(instance.tickCount);
        float zScale = this.getBoxScale(MovementAxis.Z).apply(instance.tickCount);
        xScale = this.getScaleDirection() == ScaleDirection.OUT ? xScale : (float) (1 - xScale / this.getVertexPos().x);
        yScale = this.getScaleDirection() == ScaleDirection.OUT ? yScale : (float) (1 - yScale / this.getVertexPos().y);
        zScale = this.getScaleDirection() == ScaleDirection.OUT ? zScale : (float) (1 - zScale / this.getVertexPos().z);
        return new Vec3(xScale, yScale, zScale);
    }

    /**
     * Builder pattern for OBB based sentinel boxes
     */
    public static class Builder extends SentinelBox.Builder {

        public Builder(String name) {
            super(name);
            this.boxMovement.put(0, (ticks, partialTicks) -> 0.0F);
            this.boxMovement.put(1, (ticks, partialTicks) -> 0.0F);
            this.boxMovement.put(2, (ticks, partialTicks) -> 0.0F);
        }

        /**
         * Creates a new builder
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

        public Builder sizeAndOffset(float xSize, float ySize, float xOffset, float yOffset, float zOffset) {
            sizeAndOffset(xSize, ySize, xSize, xOffset, yOffset, zOffset);
            return this;
        }

        /**
         * Defines the size and offset of the sentinel box.
         * @param xVertex The x distance from the center of the box to the top right vertex
         * @param yVertex The y distance from the center of the box to the top right vertex
         * @param zVertex The z distance from the center of the box to the top right vertex
         * @param xOffset The x offset
         * @param yOffset The y offset
         * @param zOffset The z offset
         * @return The builder
         */
        public Builder sizeAndOffset(float xVertex, float yVertex, float zVertex, float xOffset, float yOffset, float zOffset) {
            double xSize = Math.abs(xVertex) + Math.abs(xOffset);
            double ySize = Math.abs(yVertex) + Math.abs(yOffset);
            double zSize = Math.abs(zVertex) + Math.abs(zOffset);
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

        public Builder moverType(MoverType moverType) {
            this.moverType = moverType;
            return this;
        }

        public Builder defineMovement(MovementAxis axis, BiFunction<Integer, Float, Float> boxMovement) {
            this.boxMovement.put(axis.ordinal(), boxMovement);
            return this;
        }

        public Builder defineRotation(MovementAxis axis, BiFunction<Integer, Float, Float> boxRotation) {
            this.boxRotation.put(axis.ordinal(), boxRotation);
            return this;
        }

        public Builder scaleOut(MovementAxis axis, Function<Integer, Float> boxScale) {
            this.scaleDirection = ScaleDirection.OUT;
            this.boxScale.put(axis.ordinal(), boxScale);
            return this;
        }

        public Builder scaleIn(MovementAxis axis, Function<Integer, Float> boxScale) {
            this.scaleDirection = ScaleDirection.IN;
            this.boxScale.put(axis.ordinal(), boxScale);
            return this;
        }

        public Builder scaleOut(Function<Integer, Float> boxScale) {
            for (int i = 0; i < 3; i++) {
                this.scaleDirection = ScaleDirection.OUT;
                this.boxScale.put(MovementAxis.values()[i].ordinal(), boxScale);
            }
            return this;
        }

        public Builder scaleIn(Function<Integer, Float> boxScale) {
            for (int i = 0; i < 3; i++) {
                this.scaleDirection = ScaleDirection.IN;
                this.boxScale.put(MovementAxis.values()[i].ordinal(), boxScale);
            }
            return this;
        }

        public Builder squareMovement(float length, int cycleSpeed) {
            int i1 = cycleSpeed * 40;
            int i2 = i1 / 4;
            this.boxMovement.put(0, (ticks, partialTicks) -> {
                if (ticks % i1 < i1 * 0.25F) {
                    return (length / i2) * (ticks % i2);
                } else if (ticks % i1 < i1 * 0.5F) {
                    return length;
                } else if (ticks % i1 < i1 * 0.75F) {
                    return length - (length / i2) * (ticks % i2);
                } else {
                    return 0.0F;
                }
            });
            this.boxMovement.put(2, (ticks, partialTicks) -> {
                if (ticks % i1 < i1 * 0.25F) {
                    return  0.0F;
                } else if (ticks % i1 < i1 * 0.5F) {
                    return (length / i2) * (ticks % i2);
                } else if (ticks % i1 < i1 * 0.75F) {
                    return length;
                } else {
                    return length - (length / i2) * (ticks % i2);
                }
            });
            return this;
        }

        public Builder circleMovement(float radius, float cycleSpeed) {
            this.boxMovement.put(0, (ticks, partialTicks) -> {
                return radius * (float) Math.sin(cycleSpeed * ticks);
            });
            this.boxMovement.put(2, (ticks, partialTicks) -> {
                return radius * (float) Math.cos(cycleSpeed * ticks);
            });
            return this;
        }

        /**
         * Builds the Sentinel Box
         * @return An OBBSentinelBox
         */
        public OBBSentinelBox build() {
            return new OBBSentinelBox(this);
        }
    }
}
