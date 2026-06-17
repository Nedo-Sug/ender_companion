package com.nedosug.endercompanion.networking;

import com.nedosug.endercompanion.EnderCompanionMod;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
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
        if (Platform.getEnvironment().isClient()) {
            boolean enabled = buf.readBoolean();
            double radius = buf.readDouble();
            EnderCompanionMod.LOGGER.debug("Received config sync: enabled={}, radius={}", enabled, radius);
        }
        context.queue(() -> {
        });
    }
}
