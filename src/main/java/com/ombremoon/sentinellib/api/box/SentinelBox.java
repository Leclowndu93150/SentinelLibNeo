package com.ombremoon.sentinellib.api.box;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.*;

/**
 * The base sentinel box class responsible for defining the hitbox and attack conditions. This class is abstracted and not intended for direct use. See either {@link AABBSentinelBox} or {@link OBBSentinelBox}.
 */
public abstract class SentinelBox {
    protected final AABB aabb;
    private final String name;
    private final Vec3 vertexPos;
    private final Vec3 boxOffset;
    private final boolean hasDuration;
    private final int duration;
    private final Predicate<Entity> stopPredicate;
    private final BiPredicate<Entity, Integer> activeDuration;
    private final BiPredicate<Entity, LivingEntity> attackCondition;
    private final BiConsumer<Entity, BoxInstance> boxStart;
    private final BiConsumer<Entity, BoxInstance> boxTick;
    private final BiConsumer<Entity, BoxInstance> boxStop;
    private final BiConsumer<Entity, BoxInstance> boxActive;
    private final BiConsumer<Entity, LivingEntity> boxCollision;
    private final BiConsumer<Entity, LivingEntity> boxHurt;
    private final ResourceKey<DamageType> damageType;
    protected final BiFunction<Entity, LivingEntity, Float> damageFunction;
    private final MoverType moverType;
    private final Int2ObjectOpenHashMap<BiFunction<Integer, Float, Float>> boxMovement;
    private final Int2ObjectOpenHashMap<BiFunction<Integer, Float, Float>> boxRotation;
    private final Int2ObjectOpenHashMap<Function<Integer, Float>> boxScale;
    private final ScaleDirection scaleDirection;
    private final Vec3[] vertices;
    private final Vec3[] normals;

    public SentinelBox(Builder builder) {
        this.aabb = builder.aabb;
        this.name = builder.name;
        this.vertexPos = builder.vertexPos;
        this.boxOffset = builder.boxOffset;
        this.hasDuration = builder.hasDuration;
        this.duration = builder.duration;
        this.stopPredicate = builder.stopPredicate;
        this.activeDuration = builder.activeDuration;
        this.attackCondition = builder.attackCondition;
        this.boxStart = builder.boxStart;
        this.boxTick = builder.boxTick;
        this.boxStop = builder.boxStop;
        this.boxHurt = builder.boxHurt;
        this.boxCollision = builder.boxCollision;
        this.boxActive = builder.boxActive;
        this.damageType = builder.damageType;
        this.damageFunction = builder.damageFunction;
        this.moverType = builder.moverType;
        this.boxMovement = builder.boxMovement;
        this.boxRotation = builder.boxRotation;
        this.boxScale = builder.boxScale;
        this.scaleDirection = builder.scaleDirection;
        this.vertices = new Vec3[]{new Vec3(vertexPos.x, vertexPos.y, -vertexPos.z), new Vec3(vertexPos.x, vertexPos.y, vertexPos.z), new Vec3(-vertexPos.x, vertexPos.y, vertexPos.z), new Vec3(-vertexPos.x, vertexPos.y, -vertexPos.z)};
        this.normals = new Vec3[]{new Vec3(1.0F, 0.0F, 0.0F), new Vec3(0.0F, 1.0F, 0.0F), new Vec3(0.0F, 0.0F, -1.0F)};
    }

    public abstract AABB getSentinelBB(BoxInstance instance);

    public abstract void renderBox(BoxInstance instance, Entity entity, PoseStack poseStack, VertexConsumer vertexConsumer, float partialTicks, float isRed);

    public abstract List<Entity> getEntityCollisions(Entity owner, BoxInstance instance);

    public Vec3 getVertex(int index) {
        return this.vertices[index];
    }

    public Vec3 getNormal(int index) {
        return this.normals[index];
    }

    public String getName() { return this.name; }

    public AABB getAABB() {
        return this.aabb;
    }

    public Vec3 getVertexPos() {
        return this.vertexPos;
    }

    public Vec3 getBoxOffset() {
        return this.boxOffset;
    }

    public boolean hasDuration() {
        return this.hasDuration;
    }

    public int getDuration() {
        return this.duration;
    }

    public Predicate<Entity> getStopPredicate() {
        return this.stopPredicate;
    }

    public BiPredicate<Entity, Integer> getActiveDuration() {
        return this.activeDuration;
    }

    public BiPredicate<Entity, LivingEntity> getAttackCondition() {
        return this.attackCondition;
    }

    public BiConsumer<Entity, BoxInstance> onBoxTrigger() {
        return this.boxStart;
    }

    public BiConsumer<Entity, BoxInstance> onBoxTick() {
        return this.boxTick;
    }

    public BiConsumer<Entity, BoxInstance> onBoxStop() {
        return this.boxStop;
    }

    public BiConsumer<Entity, BoxInstance> onActiveTick() {
        return this.boxActive;
    }

    public BiConsumer<Entity, LivingEntity> onCollisionTick() {
        return this.boxCollision;
    }

    public BiConsumer<Entity, LivingEntity> onHurtTick() {
        return this.boxHurt;
    }

    public ResourceKey<DamageType> getDamageType() {
        return this.damageType;
    }

    public float getDamageAmount(Entity owner, LivingEntity target) {
        return this.damageFunction.apply(owner, target);
    }

    public MoverType getMoverType() {
        return this.moverType;
    }

    public BiFunction<Integer, Float, Float> getBoxMovement(MovementAxis axis) {
        return this.boxMovement.getOrDefault(axis.ordinal(), (ticks, partialTicks) -> 0.0F);
    }

    public BiFunction<Integer, Float, Float> getBoxRotation(MovementAxis axis) {
        return this.boxRotation.getOrDefault(axis.ordinal(), (ticks, partialTicks) -> 0.0F);
    }

    public Function<Integer, Float> getBoxScale(MovementAxis axis) {
        return this.boxScale.getOrDefault(axis.ordinal(), ticks -> 1.0F);
    }

    public ScaleDirection getScaleDirection() {
        return this.scaleDirection;
    }

    public Vec3 getBoxPath(BoxInstance instance, float partialTicks) {
        return Vec3.ZERO;
    }

    public Vec3 getBoxAngle(BoxInstance instance, float partialTicks) {
        return Vec3.ZERO;
    }

    public Vec3 getScaleFactor(BoxInstance instance) {
        return new Vec3(1, 1, 1);
    }

    public boolean isEntityControlled() {
        return this.moverType != MoverType.BONE;
    }

    public Pair<Float, Float> getYRot(Entity entity) {
        float f0;
        float f1;
        if (entity instanceof LivingEntity livingEntity) {
            switch (this.moverType) {
                case BODY, CUSTOM_BODY -> {
                    f0 = livingEntity.yBodyRot;
                    f1 = livingEntity.yBodyRotO;
                }
                case HEAD, HEAD_NO_X, CUSTOM_HEAD -> {
                    f0 = livingEntity.yHeadRot;
                    f1 = livingEntity.yHeadRotO;
                }
                default -> {
                    f0 = 0;
                    f1 = 0;
                }
            }
        } else {
            f0 = entity.getYRot();
            f1 = entity.yRotO;
        }
        return Pair.of(f0, f1);
    }

    public enum MoverType {
        HEAD(false, true, true),
        HEAD_NO_X(false, true, false),
        BODY(false, false, false),
        CUSTOM(true, false, false),
        CUSTOM_HEAD(true, true, false),
        CUSTOM_BODY(true, false, false),
        BONE(false, false, false);

        private final boolean isDefined;
        private final boolean followsPitch;
        private final boolean followsYaw;

        MoverType(boolean isDefined, boolean followsPitch, boolean followsYaw) {
            this.isDefined = isDefined;
            this.followsPitch = followsPitch;
            this.followsYaw = followsYaw;
        }

        public boolean isDefined() {
            return this.isDefined;
        }

        public boolean followsPitch() {
            return this.followsPitch;
        }

        public boolean followsYaw() {
            return this.followsYaw;
        }
    }

    public enum MovementAxis {
        X, Y, Z
    }

    public enum ScaleDirection {
        IN, OUT
    }

    protected static class Builder {
        protected final String name;
        protected AABB aabb;
        protected Vec3 boxOffset;
        protected Vec3 vertexPos;
        protected boolean hasDuration = true;
        protected int duration = 30;
        protected Predicate<Entity> stopPredicate = livingEntity -> false;
        protected BiPredicate<Entity, Integer> activeDuration = (entity, integer) -> integer % 10 == 0;
        protected BiPredicate<Entity, LivingEntity> attackCondition = (entity, livingEntity) -> true;
        protected BiConsumer<Entity, BoxInstance> boxStart = (attacker, instance) -> {};
        protected BiConsumer<Entity, BoxInstance> boxTick = (attacker, instance) -> {};
        protected BiConsumer<Entity, BoxInstance> boxStop = (attacker, instance) -> {};
        protected BiConsumer<Entity, BoxInstance> boxActive = (attacker, instance) -> {};
        protected BiConsumer<Entity, LivingEntity> boxHurt = (attacker, target) -> {};
        protected BiConsumer<Entity, LivingEntity> boxCollision = (attacker, target) -> {};
        protected ResourceKey<DamageType> damageType;
        protected BiFunction<Entity, LivingEntity, Float> damageFunction = (entity, living) -> 0F;
        protected MoverType moverType = MoverType.HEAD;
        protected Int2ObjectOpenHashMap<BiFunction<Integer, Float, Float>> boxMovement = new Int2ObjectOpenHashMap<>();
        protected Int2ObjectOpenHashMap<BiFunction<Integer, Float, Float>> boxRotation = new Int2ObjectOpenHashMap<>();
        protected Int2ObjectOpenHashMap<Function<Integer, Float>> boxScale = new Int2ObjectOpenHashMap<>();
        protected ScaleDirection scaleDirection = ScaleDirection.OUT;

        protected Builder(String name) {
            this.name = name;
        }
    }
}
