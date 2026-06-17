package com.nedosug.endercompanion;

import com.nedosug.endercompanion.entity.ModEntities;
import com.nedosug.endercompanion.item.ModItems;

/**
 * Central registration hub. All game objects are registered here via DeferredRegister,
 * never instantiated with hardcoded registry keys at runtime.
 */
public final class ModRegistries {
    private ModRegistries() {
    }

    public static void register() {
        ModEntities.register();
        ModEntities.registerAttributes();
        ModItems.register();
    }
}
