package com.nedosug.endercompanion.fabric.client;

import com.nedosug.endercompanion.client.EnderCompanionClient;
import com.nedosug.endercompanion.fabric.client.config.ClothConfigIntegration;
import com.nedosug.endercompanion.networking.ModNetworking;
import net.fabricmc.api.ClientModInitializer;

public final class EnderCompanionClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EnderCompanionClient.init();
        ModNetworking.initClient();
        ClothConfigIntegration.register();
    }
}
