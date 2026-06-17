package com.nedosug.endercompanion.client;

import com.nedosug.endercompanion.client.renderer.EnderCompanionRenderer;
import com.nedosug.endercompanion.entity.ModEntities;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;

public final class EnderCompanionClient {
    private EnderCompanionClient() {
    }

    public static void init() {
        EntityRendererRegistry.register(ModEntities.ENDER_COMPANION, EnderCompanionRenderer::new);
    }
}
