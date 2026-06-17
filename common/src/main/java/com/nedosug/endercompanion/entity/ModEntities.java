package com.nedosug.endercompanion.entity;

import com.nedosug.endercompanion.EnderCompanionMod;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(EnderCompanionMod.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<EnderCompanionEntity>> ENDER_COMPANION =
            ENTITIES.register("ender_companion", () -> EntityType.Builder
                    .of(EnderCompanionEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 2.7F)
                    .clientTrackingRange(10)
                    .build("ender_companion"));

    private ModEntities() {
    }

    public static void register() {
        ENTITIES.register();
    }

    public static void registerAttributes() {
        EntityAttributeRegistry.register(ENDER_COMPANION, EnderCompanionEntity::createAttributes);
    }
}
