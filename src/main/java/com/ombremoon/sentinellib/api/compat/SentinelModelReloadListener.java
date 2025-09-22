package com.ombremoon.sentinellib.api.compat;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.ombremoon.sentinellib.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.FileLoader;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

import java.util.Map;

public class SentinelModelReloadListener extends SimpleJsonResourceReloadListener {
    private static final Map<ResourceLocation, BakedGeoModel> MODELS = Maps.newHashMap();
    private static final Map<ResourceLocation, BakedAnimations> ANIMATIONS = Maps.newHashMap();

    public SentinelModelReloadListener() {
        super(KeyFramesAdapter.GEO_GSON, "geo_sentinel");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        resourceManager.listResources("geo_sentinel", resourceLocation -> resourceLocation.getPath().endsWith(".json")).keySet().forEach(location -> {
            try {
                Model model = KeyFramesAdapter.GEO_GSON.fromJson(FileLoader.loadFile(location, resourceManager), Model.class);
                switch (model.formatVersion()) {
                    case V_1_12_0 -> {}
                    case V_1_14_0 -> throw new IllegalArgumentException("Unsupported geometry json version: 1.14.0. Supported versions: 1.12.0");
                    case V_1_21_0 -> throw new IllegalArgumentException("Unsupported geometry json version: 1.21.0. Supported versions: 1.12.0. Remove any rotated face UVs and re-export the model to fix");
                    case null, default -> throw new IllegalArgumentException("Unsupported geometry json version. Supported versions: 1.12.0");
                }

                BakedGeoModel geoModel = BakedModelFactory.getForNamespace(location.getNamespace()).constructGeoModel(GeometryTree.fromModel(model));
                MODELS.put(location, geoModel);
            } catch (Exception e) {
                throw GeckoLibConstants.exception(location, "Error loading geo-sentinel model file", e);
            }
        });

        Constants.LOG.info("Loaded {} sentinel models", MODELS.size());
    }

    public static Map<ResourceLocation, BakedGeoModel> geBakedModels() {
        return MODELS;
    }
}
