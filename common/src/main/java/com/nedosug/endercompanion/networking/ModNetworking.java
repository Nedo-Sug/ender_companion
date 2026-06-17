package com.nedosug.endercompanion.networking;

import com.nedosug.endercompanion.EnderCompanionMod;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Cross-platform networking via Architectury NetworkManager.
 */
public final class ModNetworking {
    public static final ResourceLocation SYNC_CONFIG = new ResourceLocation(EnderCompanionMod.MOD_ID, "sync_config");

    private ModNetworking() {
    }

    public static void initCommon() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                SYNC_CONFIG,
                ModNetworking::handleConfigSync
        );
    }

    public static void initClient() {
        // Client-side packet handlers go here
    }

    private static void handleConfigSync(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        // This handler is registered S2C, so it only runs on the client.
        boolean enabled = buf.readBoolean();
        double radius = buf.readDouble();
        context.queue(() ->
            EnderCompanionMod.LOGGER.debug("Received config sync: enabled={}, radius={}", enabled, radius)
        );
    }
}
