package com.ombremoon.sentinellib.example.entity;

import com.ombremoon.sentinellib.CommonClass;
import com.ombremoon.sentinellib.Constants;
import com.ombremoon.sentinellib.SentinelLib;
import com.ombremoon.sentinellib.api.box.SentinelBox;
import com.ombremoon.sentinellib.api.compat.GeoSentinel;
import com.ombremoon.sentinellib.api.compat.ServerGeoModel;
import com.ombremoon.sentinellib.common.BoxInstanceManager;
import com.ombremoon.sentinellib.api.compat.GeoBoneOBBSentinelBox;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

public class IceMist extends Entity implements TraceableEntity, GeoSentinel<IceMist> {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final BoxInstanceManager manager = new BoxInstanceManager(this);
    private final IceMistSentinelModel model = new IceMistSentinelModel();
    protected static final RawAnimation MIST = RawAnimation.begin().thenPlay("mist");
    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;

    public static final GeoBoneOBBSentinelBox CENTER = GeoBoneOBBSentinelBox.Builder.of("mist_center")
            .sizeAndOffset(0.3125F)
            .activeTicks((entity, ticks) -> true)
            .typeDamage(DamageTypes.FREEZE, (entity, livingEntity) -> 1.0F)
            .noDuration(Entity::isRemoved).build();

    public static final GeoBoneOBBSentinelBox ROT = GeoBoneOBBSentinelBox.Builder.of("mist1")
            .sizeAndOffset(0.375F)
            .activeTicks((entity, ticks) -> true)
            .typeDamage(DamageTypes.FREEZE, (entity, livingEntity) -> 1.0F)
            .noDuration(Entity::isRemoved).build();

    public IceMist(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    public IceMist(Level pLevel, double pX, double pY, double pZ, LivingEntity iceQueen) {
        this(SentinelLib.MIST.get(), pLevel);
        this.setOwner(iceQueen);
        this.setPos(pX, pY, pZ);
    }

    @Override
    public BoxInstanceManager getBoxManager() {
        return this.manager;
    }

    @Override
    public List<SentinelBox> getSentinelBoxes() {
        return ObjectArrayList.of(
                CENTER,
                ROT
        );
    }

    @Override
    public ServerGeoModel<IceMist> getSentinelModel() {
        return this.model;
    }

    public void setOwner(@Nullable LivingEntity pOwner) {
        this.owner = pOwner;
        this.ownerUUID = pOwner == null ? null : pOwner.getUUID();
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel) {
            Entity entity = ((ServerLevel)this.level()).getEntity(this.ownerUUID);
            if (entity instanceof LivingEntity) {
                this.owner = (LivingEntity)entity;
            }
        }

        return this.owner;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        if (pCompound.hasUUID("Owner")) {
            this.ownerUUID = pCompound.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        if (this.ownerUUID != null) {
            pCompound.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public void tick() {
        tickBoxes();
        super.tick();

        if (this.tickCount >= 100) {
//            this.discard();
        }

        if (this.tickCount == 1) {
            triggerAllSentinelBoxes();
        }

        if (!level().isClientSide) {
//            getSentinelModel().getBonePosition(this, "mist1");
//            Constants.LOG.info("{}", getSentinelModel().getBonePosition(this, "mist_center"));
            Constants.LOG.info("{}", getSentinelModel().getBone("mist1").get().getWorldPosition());
//            Constants.LOG.debug("{}", getSentinelModel().getBone("mist1").get().getRotationVector());
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Mist", 0, state -> state.setAndContinue(MIST)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static class IceMistSentinelModel extends ServerGeoModel<IceMist> {

        @Override
        public ResourceLocation getModelResource(IceMist animatable) {
            return CommonClass.customLocation("geo_sentinel/entity/ice_mist.json");
        }

        @Override
        public ResourceLocation getAnimationResource(IceMist animatable) {
            return CommonClass.customLocation("sentinel_anim/entity/ice_mist.json");
        }
    }
}
