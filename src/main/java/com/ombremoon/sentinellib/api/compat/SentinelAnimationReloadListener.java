package com.ombremoon.sentinellib.api.compat;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.ombremoon.sentinellib.Constants;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.FileLoader;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;
import software.bernie.geckolib.util.CompoundException;

import java.util.Map;

public class SentinelAnimationReloadListener extends SimpleJsonResourceReloadListener {
    private static final Map<ResourceLocation, BakedAnimations> ANIMATIONS = Maps.newHashMap();

    public SentinelAnimationReloadListener() {
        super(KeyFramesAdapter.GEO_GSON, "sentinel_anim");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        resourceManager.listResources("sentinel_anim", resourceLocation -> resourceLocation.getPath().endsWith(".json")).keySet().forEach(location -> {
            try {
                BakedAnimations animations = KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.getAsJsonObject(FileLoader.loadFile(location, resourceManager), "animations"), BakedAnimations.class);
                ANIMATIONS.put(location, animations);
            } catch (CompoundException ex) {
                ex.withMessage(location.toString() + ": Error loading animation file").printStackTrace();

                BakedAnimations animations = new BakedAnimations(new Object2ObjectOpenHashMap<>());
                ANIMATIONS.put(location, animations);
            } catch (Exception ex) {
                throw GeckoLibConstants.exception(location, "Error loading animation file", ex);
            }
        });

        Constants.LOG.info("Loaded {} sentinel animations", ANIMATIONS.size());
    }

    public static Map<ResourceLocation, BakedAnimations> getBakedAnimations() {
        return ANIMATIONS;
    }
}
