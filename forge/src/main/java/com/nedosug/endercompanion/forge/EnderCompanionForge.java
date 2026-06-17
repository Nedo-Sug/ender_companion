package com.nedosug.endercompanion.forge;

import com.nedosug.endercompanion.EnderCompanionMod;
import com.nedosug.endercompanion.event.CompanionChatHandler;
import com.nedosug.endercompanion.networking.ModNetworking;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EnderCompanionMod.MOD_ID)
public final class EnderCompanionForge {
    public EnderCompanionForge() {
        EventBuses.registerModEventBus(EnderCompanionMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        EnderCompanionMod.init();
        ModNetworking.initCommon();

        // Listen for chat and lifecycle events on the Forge game event bus. This mirrors the
        // Fabric ServerMessageEvents.CHAT_MESSAGE listener so both platforms route chat to the AI.
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        CompanionChatHandler.onPlayerChat(event.getPlayer(), event.getRawText());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        EnderCompanionMod.shutdown();
    }
}
