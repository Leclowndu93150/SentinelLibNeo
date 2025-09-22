package com.ombremoon.sentinellib;

import com.ombremoon.sentinellib.api.Easing;
import com.ombremoon.sentinellib.api.box.BoxInstance;
import com.ombremoon.sentinellib.api.box.OBBSentinelBox;
import com.ombremoon.sentinellib.api.box.SentinelBox;
import com.ombremoon.sentinellib.common.BoxInstanceManager;
import com.ombremoon.sentinellib.common.IPlayerSentinel;
import com.ombremoon.sentinellib.common.ISentinel;
import com.ombremoon.sentinellib.common.event.RegisterPlayerSentinelBoxEvent;
import com.ombremoon.sentinellib.api.compat.GeoEvents;
import com.ombremoon.sentinellib.example.entity.IceMist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/*
CHANGELOG:
- BoxUtil static fields
- Fixed Collision Tick
- Changed typeDamage to BiFunction
- Added box instance parameter to box specific callbacks
- Added additional x/z axis rotation, as well as dynamic rotating sentinel boxes
- Added dynamic scaling sentinel boxes
- Finished implementing Geckolib compat
 */


/*
* TODO:
- Re-implement offset to GeoBone OBBs
- Finish documentation
*/

@Mod(Constants.MOD_ID)
@EventBusSubscriber(modid = Constants.MOD_ID)
public class SentinelLib {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Constants.MOD_ID);

    public static Supplier<EntityType<IceMist>> MIST;

    public static final OBBSentinelBox TEST_ELASTIC = OBBSentinelBox.Builder.of("test")
            .sizeAndOffset(2F, 0.0F, 1, 1.0F)
            .activeTicks((entity, integer) -> true)
            .boxDuration(100)
            .moverType(SentinelBox.MoverType.CUSTOM_HEAD)
            .scaleOut(SentinelBox.MovementAxis.X, ticks -> Easing.QUAD_IN.easing((float) ticks / 100))
            .scaleOut(SentinelBox.MovementAxis.Y, ticks -> Easing.QUAD_IN.easing((float) ticks / 100))
            .scaleOut(SentinelBox.MovementAxis.Z, ticks -> Easing.QUAD_IN.easing((float) ticks / 100))
            .defineMovement(SentinelBox.MovementAxis.Z, (ticks, partialTicks) -> {
                return Easing.ELASTIC_OUT.easing(2.0F, (float) ticks / 200);
            })
            .typeDamage(DamageTypes.FREEZE, (entity, living) -> 15F).build();

    public static final OBBSentinelBox TEST_CIRCLE = OBBSentinelBox.Builder.of("circle")
            .sizeAndOffset(0.5F, 0, 0.5F, 0)
            .activeTicks((entity, integer) -> integer > 0)
            .boxDuration(100)
            .moverType(SentinelBox.MoverType.CUSTOM)
            .circleMovement(2.0F, 0.15F)
            .typeDamage(DamageTypes.FREEZE, (entity, living) -> 15F).build();

    public static final OBBSentinelBox BEAM_BOX = OBBSentinelBox.Builder.of("beam")
            .sizeAndOffset(0.3F, 0.3F, 3, 0.0F, 1.7F, 4)
            .noDuration(Entity::isCrouching)
            .defineRotation(SentinelBox.MovementAxis.Z, (ticks, partialTicks) -> Float.valueOf(ticks))
            .activeTicks((entity, integer) -> integer > 45 && integer % 10 == 0)
            .typeDamage(DamageTypes.FREEZE, (entity, living) -> 15F).build();


    public SentinelLib(IEventBus modEventBus, ModContainer modContainer) {
        renderGeckolibSentinelBoxes();
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        if (CommonClass.isDevEnv()) {
            ITEMS.register("debug", () -> new DebugItem(new Item.Properties()));
            MIST = ENTITY_TYPES.register("ice_mist", () -> EntityType.Builder.<IceMist>of(IceMist::new, MobCategory.MISC).sized(1, 1).clientTrackingRange(64).build("ice_mist"));
        }
    }

    @SubscribeEvent
    public static void renderSentinelBox(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
        LivingEntity entity = event.getEntity();
        Minecraft minecraft = Minecraft.getInstance();

        if (entity.level() == null) {
            return;
        }

        if (minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes() && !minecraft.showOnlyReducedInfo() && entity instanceof ISentinel sentinel) {
            BoxInstanceManager manager = sentinel.getBoxManager();
            for (BoxInstance instance : manager.getInstances()) {
                instance.getSentinelBox().renderBox(instance, entity, event.getPoseStack(), event.getMultiBufferSource().getBuffer(RenderType.lines()), event.getPartialTick(), instance.isActive() ? 0.0F : 1.0F);
            }
        }
    }

    @SubscribeEvent
    public static void tickPlayerBoxes(PlayerTickEvent.Post event) {
        IPlayerSentinel sentinel = (IPlayerSentinel) event.getEntity();
        sentinel.tickBoxes();
    }

    @SubscribeEvent
    public static void registerSentinelBox(RegisterPlayerSentinelBoxEvent event) {
        event.addEntry(TEST_ELASTIC);
        event.addEntry(TEST_CIRCLE);
        event.addEntry(BEAM_BOX);
    }

    private void renderGeckolibSentinelBoxes() {
        if (ModList.get().isLoaded("geckolib")) {
            NeoForge.EVENT_BUS.register(GeoEvents.getInstance());
        }
    }
}
