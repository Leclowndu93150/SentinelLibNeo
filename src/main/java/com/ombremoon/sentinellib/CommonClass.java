package com.ombremoon.sentinellib;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLLoader;

public class CommonClass {

    public static boolean isDevEnv() {
        return !FMLLoader.isProduction();
    }

    public static ResourceLocation customLocation(String name) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
    }
}
