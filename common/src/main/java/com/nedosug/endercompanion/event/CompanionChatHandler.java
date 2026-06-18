package com.nedosug.endercompanion.event;

import com.nedosug.endercompanion.EnderCompanionMod;
import com.nedosug.endercompanion.ai.EmbeddedAIHandler;
import com.nedosug.endercompanion.entity.EnderCompanionEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Platform-agnostic entry point for routing in-game chat messages to the AI companion.
 * <p>
 * Both the Fabric and Forge initializers register their respective server chat events and
 * delegate here, so the message-handling logic lives in one place. When a player talks, the
 * nearest companion they own picks up the message and replies asynchronously through
 * {@link EmbeddedAIHandler}.
 */
public final class CompanionChatHandler {

    /** Maximum distance (in blocks) a companion will "hear" its owner's chat from. */
    private static final double HEARING_RADIUS = 32.0D;

    private CompanionChatHandler() {
    }

    /**
     * Handles a chat message sent by a player on the server.
     *
     * @param sender the player who sent the message
     * @param text   the raw text content of the message
     */
    public static void onPlayerChat(@Nullable ServerPlayer sender, @Nullable String text) {
        if (sender == null || text == null || text.isBlank()) {
            return;
        }

        // Debug log so it is obvious the listener is actually being invoked.
        EnderCompanionMod.LOGGER.info("[EnderCompanion-Debug] Intercepted message from {}: {}",
                sender.getName().getString(), text);

        // Guard everything after interception so any failure (lookup, scheduling inference)
        // is logged with a full stack trace instead of being swallowed silently.
        try {
            EnderCompanionEntity companion = findNearestOwnedCompanion(sender);
            if (companion == null) {
                // No companion owned by this player is nearby; nothing to do.
                EnderCompanionMod.LOGGER.info("[EnderCompanion-Debug] No owned companion in range of {}.",
                        sender.getName().getString());
                return;
            }

            EnderCompanionMod.LOGGER.info("[EnderCompanion-Debug] Routing message to companion at {} (friendship {}).",
                    companion.blockPosition(), companion.getFriendshipLevel());

            EmbeddedAIHandler.askEnderGirl(companion, text,
                    response -> deliverResponse(sender, companion, response));
        } catch (Throwable t) {
            EnderCompanionMod.LOGGER.error("[EnderCompanion-Debug] Failed to handle chat message", t);
            t.printStackTrace();
        }
    }

    /**
     * Finds the closest {@link EnderCompanionEntity} owned by the given player within hearing range.
     *
     * @return the nearest owned companion, or {@code null} if none is in range
     */
    @Nullable
    private static EnderCompanionEntity findNearestOwnedCompanion(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        List<EnderCompanionEntity> candidates = level.getEntitiesOfClass(
                EnderCompanionEntity.class,
                player.getBoundingBox().inflate(HEARING_RADIUS),
                companion -> player.getUUID().equals(companion.getOwnerUUID()));

        EnderCompanionEntity nearest = null;
        double nearestDistanceSq = Double.MAX_VALUE;
        for (EnderCompanionEntity companion : candidates) {
            double distanceSq = companion.distanceToSqr(player);
            if (distanceSq < nearestDistanceSq) {
                nearestDistanceSq = distanceSq;
                nearest = companion;
            }
        }
        return nearest;
    }

    /**
     * Sends the AI's reply to chat. The callback may run on a background inference thread, so the
     * actual message broadcast is scheduled back onto the main server thread.
     */
    private static void deliverResponse(ServerPlayer sender, EnderCompanionEntity companion, String response) {
        if (response == null || response.isBlank()) {
            return;
        }

        MinecraftServer server = sender.getServer();
        if (server == null) {
            return;
        }

        Component message = Component.literal("<")
                .append(companion.getDisplayName())
                .append("> ")
                .append(Component.literal(response));

        // The AI callback runs on the inference thread; chat must be sent from the main
        // server thread, so hop back onto it via MinecraftServer.execute().
        server.execute(() -> {
            try {
                server.getPlayerList().broadcastSystemMessage(message, false);
                EnderCompanionMod.LOGGER.info("[EnderCompanion-Debug] Delivered AI reply to chat: {}", response);
            } catch (Throwable t) {
                EnderCompanionMod.LOGGER.error("[EnderCompanion-Debug] Failed to broadcast AI reply", t);
                t.printStackTrace();
            }
        });
    }
}
