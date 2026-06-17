package com.nedosug.endercompanion.entity.ai;

import com.nedosug.endercompanion.entity.EnderCompanionEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FollowPlayerGoal extends Goal {
    private static final double MIN_DISTANCE = 2.0D;
    private static final double MAX_DISTANCE = 10.0D;
    private static final double TELEPORT_DISTANCE = 12.0D;
    private static final double SPEED_MODIFIER = 1.0D;

    private final EnderCompanionEntity companion;
    private Player targetPlayer;
    private int teleportCooldown;

    public FollowPlayerGoal(EnderCompanionEntity companion) {
        this.companion = companion;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Player owner = this.companion.getOwner();
        if (owner != null && !owner.isSpectator() && owner.isAlive()) {
            this.targetPlayer = owner;
            return true;
        }

        this.targetPlayer = this.companion.level().getNearestPlayer(this.companion, 32.0D);
        return this.targetPlayer != null && !this.targetPlayer.isSpectator();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetPlayer == null || !this.targetPlayer.isAlive() || this.targetPlayer.isSpectator()) {
            return false;
        }
        return this.companion.distanceToSqr(this.targetPlayer) < 1024.0D;
    }

    @Override
    public void start() {
        this.teleportCooldown = 0;
    }

    @Override
    public void stop() {
        this.targetPlayer = null;
        this.companion.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.targetPlayer == null) {
            return;
        }

        this.companion.getLookControl().setLookAt(this.targetPlayer, 10.0F, this.companion.getMaxHeadXRot());

        double distanceSqr = this.companion.distanceToSqr(this.targetPlayer);
        double distance = Math.sqrt(distanceSqr);

        if (this.teleportCooldown > 0) {
            this.teleportCooldown--;
        }

        if (distance > TELEPORT_DISTANCE && this.teleportCooldown == 0) {
            this.teleportToPlayer();
            this.teleportCooldown = 100;
        } else if (distance > MIN_DISTANCE && distance <= MAX_DISTANCE) {
            this.companion.getNavigation().moveTo(this.targetPlayer, SPEED_MODIFIER);
        } else if (distance <= MIN_DISTANCE) {
            this.companion.getNavigation().stop();
        }
    }

    private void teleportToPlayer() {
        if (this.targetPlayer == null) {
            return;
        }

        Level level = this.companion.level();
        Vec3 playerPos = this.targetPlayer.position();

        Vec3 oldPos = this.companion.position();
        this.spawnTeleportParticles(oldPos);

        for (int attempt = 0; attempt < 16; attempt++) {
            double offsetX = (this.companion.getRandom().nextDouble() - 0.5D) * 4.0D;
            double offsetZ = (this.companion.getRandom().nextDouble() - 0.5D) * 4.0D;
            double newX = playerPos.x + offsetX;
            double newY = playerPos.y;
            double newZ = playerPos.z + offsetZ;

            if (this.companion.randomTeleport(newX, newY, newZ, true)) {
                this.companion.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                this.spawnTeleportParticles(this.companion.position());
                break;
            }
        }
    }

    private void spawnTeleportParticles(Vec3 pos) {
        Level level = this.companion.level();
        for (int i = 0; i < 32; i++) {
            double offsetX = (this.companion.getRandom().nextDouble() - 0.5D) * 2.0D;
            double offsetY = this.companion.getRandom().nextDouble() * 2.0D;
            double offsetZ = (this.companion.getRandom().nextDouble() - 0.5D) * 2.0D;
            level.addParticle(
                    ParticleTypes.PORTAL,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    0.0D, 0.0D, 0.0D
            );
        }
    }
}
