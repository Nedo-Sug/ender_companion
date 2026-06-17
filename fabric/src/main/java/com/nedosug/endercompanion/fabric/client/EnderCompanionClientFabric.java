package com.nedosug.endercompanion.fabric.client;

import com.nedosug.endercompanion.client.EnderCompanionClient;
import com.nedosug.endercompanion.client.model.EnderCompanionModel;
import com.nedosug.endercompanion.fabric.client.config.ClothConfigIntegration;
import com.nedosug.endercompanion.networking.ModNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public final class EnderCompanionClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(
                EnderCompanionModel.LAYER_LOCATION,
                EnderCompanionModel::createBodyLayer);
        EnderCompanionClient.init();
        ModNetworking.initClient();
        ClothConfigIntegration.register();
    }
}
