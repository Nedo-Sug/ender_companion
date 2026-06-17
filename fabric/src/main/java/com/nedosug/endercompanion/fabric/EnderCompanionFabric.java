package com.nedosug.endercompanion.fabric;

import com.nedosug.endercompanion.EnderCompanionMod;
import com.nedosug.endercompanion.event.CompanionChatHandler;
import com.nedosug.endercompanion.networking.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

public final class EnderCompanionFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EnderCompanionMod.init();
        ModNetworking.initCommon();

        // Route player chat to the AI companion using the canonical Fabric server event.
        // This fires reliably for every chat message on the server, unlike custom mixins
        // or deprecated client-side events.
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            String text = message.decoratedContent().getString();
            CompanionChatHandler.onPlayerChat(sender, text);
        });

        // Register shutdown hook
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            EnderCompanionMod.shutdown();
        });
    }
}
