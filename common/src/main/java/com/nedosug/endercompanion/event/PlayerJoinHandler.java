package com.nedosug.endercompanion.event;

import com.nedosug.endercompanion.EnderCompanionMod;
import com.nedosug.endercompanion.entity.EnderCompanionEntity;
import com.nedosug.endercompanion.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class PlayerJoinHandler {
    private static final String NBT_KEY_SPAWNED_COMPANION = "spawned_ender_companion";
    private static final int SPAWN_RADIUS = 3;
    private static final int MAX_SPAWN_ATTEMPTS = 16;

    private PlayerJoinHandler() {
    }

    public static void onPlayerJoin(ServerPlayer player) {
        if (player.getTags().contains(NBT_KEY_SPAWNED_COMPANION)) {
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 playerPos = player.position();

        BlockPos spawnPos = findSafeSpawnPosition(level, playerPos);
        if (spawnPos == null) {
            EnderCompanionMod.LOGGER.warn("Failed to find safe spawn position for Ender Companion near player {}", player.getName().getString());
            return;
        }

        EnderCompanionEntity companion = ModEntities.ENDER_COMPANION.get().create(level);
        if (companion == null) {
            EnderCompanionMod.LOGGER.error("Failed to create Ender Companion entity");
            return;
        }

        companion.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
        companion.setOwnerUUID(player.getUUID());

        if (level.addFreshEntity(companion)) {
            player.addTag(NBT_KEY_SPAWNED_COMPANION);
            EnderCompanionMod.LOGGER.info("Spawned Ender Companion for player {} at {}",
                    player.getName().getString(), spawnPos);
        } else {
            EnderCompanionMod.LOGGER.warn("Failed to add Ender Companion entity to world");
        }
    }

    private static BlockPos findSafeSpawnPosition(ServerLevel level, Vec3 playerPos) {
        BlockPos playerBlockPos = BlockPos.containing(playerPos);

        for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
            int offsetX = level.random.nextInt(SPAWN_RADIUS * 2 + 1) - SPAWN_RADIUS;
            int offsetZ = level.random.nextInt(SPAWN_RADIUS * 2 + 1) - SPAWN_RADIUS;

            BlockPos candidatePos = playerBlockPos.offset(offsetX, 0, offsetZ);

            for (int yOffset = -2; yOffset <= 2; yOffset++) {
                BlockPos testPos = candidatePos.offset(0, yOffset, 0);
                if (isSafeSpawnLocation(level, testPos)) {
                    return testPos;
                }
            }
        }

        return null;
    }

    private static boolean isSafeSpawnLocation(Level level, BlockPos pos) {
        BlockState groundState = level.getBlockState(pos.below());
        BlockState feetState = level.getBlockState(pos);
        BlockState headState = level.getBlockState(pos.above());

        return groundState.isSolid()
                && !groundState.liquid()
                && feetState.isAir()
                && headState.isAir()
                && level.getWorldBorder().isWithinBounds(pos);
    }
}
