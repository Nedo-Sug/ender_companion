package com.nedosug.endercompanion.fabric;

import com.nedosug.endercompanion.EnderCompanionMod;
import com.nedosug.endercompanion.networking.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class EnderCompanionFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EnderCompanionMod.init();
        ModNetworking.initCommon();

        // Register shutdown hook
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            EnderCompanionMod.shutdown();
        });
    }
}
