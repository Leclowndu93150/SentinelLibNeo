package com.ombremoon.sentinellib.util;

import com.ombremoon.sentinellib.api.compat.GeoSentinel;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.EasingType;
import software.bernie.geckolib.cache.object.GeoBone;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ReflectionHelper {
    public static <FIELDHOLDER,FIELDTYPE> Function<FIELDHOLDER,FIELDTYPE> getInstanceFieldGetter(Class<FIELDHOLDER> fieldHolderClass, String fieldName) {
        Field field = ObfuscationReflectionHelper.findField(fieldHolderClass, fieldName);
        return getInstanceFieldGetter(field);
    }

    public static <FIELDHOLDER,FIELDTYPE> MutableInstanceField<FIELDHOLDER,FIELDTYPE> getInstanceField(Class<FIELDHOLDER> fieldHolderClass, String fieldName) {
        return new MutableInstanceField<>(fieldHolderClass, fieldName);
    }

    @SuppressWarnings("unchecked")
    private static <FIELDHOLDER,FIELDTYPE> Function<FIELDHOLDER,FIELDTYPE> getInstanceFieldGetter(Field field) {
        return instance -> {
            try {
                return (FIELDTYPE)(field.get(instance));
            }
            catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static class MutableInstanceField<FIELDHOLDER, FIELDTYPE> {
        private final Function<FIELDHOLDER,FIELDTYPE> getter;
        private final BiConsumer<FIELDHOLDER,FIELDTYPE> setter;

        private MutableInstanceField(Class<FIELDHOLDER> fieldHolderClass, String fieldName) {
            Field field = ObfuscationReflectionHelper.findField(fieldHolderClass, fieldName);
            this.getter = getInstanceFieldGetter(field);
            this.setter = getInstanceFieldSetter(field);
        }

        public FIELDTYPE get(FIELDHOLDER instance) {
            return this.getter.apply(instance);
        }

        public void set(FIELDHOLDER instance, FIELDTYPE value) {
            this.setter.accept(instance, value);
        }

        private static <FIELDHOLDER, FIELDTYPE> BiConsumer<FIELDHOLDER, FIELDTYPE> getInstanceFieldSetter(Field field) {
            return (instance,value) -> {
                try {
                    field.set(instance, value);
                }
                catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    public static class AnimationControllerAccess {
        public static final MutableInstanceField<AnimationController, Boolean> isJustStarting =
                getInstanceField(AnimationController.class, "isJustStarting");
        public static final Function<AnimationController, Function<GeoSentinel<?>, EasingType>> overrideEasingTypeFunction =
                getInstanceFieldGetter(AnimationController.class, "overrideEasingTypeFunction");
    }

    public static class AnimatableManagerAccess {
        public static final MutableInstanceField<AnimatableManager, Boolean> isFirstTick =
                getInstanceField(AnimatableManager.class, "isFirstTick");
    }

    public static class AnimationProcessorAccess {
        public static final Function<AnimationProcessor, Map<String, GeoBone>> bones =
                getInstanceFieldGetter(AnimationProcessor.class, "bones");
    }
}
