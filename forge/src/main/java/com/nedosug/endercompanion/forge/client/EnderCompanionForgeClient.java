package com.nedosug.endercompanion.forge.client;

import com.nedosug.endercompanion.EnderCompanionMod;
import com.nedosug.endercompanion.client.EnderCompanionClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = EnderCompanionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class EnderCompanionForgeClient {
    private EnderCompanionForgeClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(EnderCompanionClient::init);
    }
}
