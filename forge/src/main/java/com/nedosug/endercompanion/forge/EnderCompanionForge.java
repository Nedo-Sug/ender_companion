package com.nedosug.endercompanion.forge;

import com.nedosug.endercompanion.EnderCompanionMod;
import com.nedosug.endercompanion.networking.ModNetworking;
import dev.architectury.platform.forge.EventBuses;
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
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        EnderCompanionMod.shutdown();
    }
}
