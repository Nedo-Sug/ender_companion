package com.nedosug.endercompanion.item;

import com.nedosug.endercompanion.EnderCompanionMod;
import com.nedosug.endercompanion.entity.ModEntities;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(EnderCompanionMod.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<SpawnEggItem> ENDER_COMPANION_SPAWN_EGG =
            ITEMS.register("ender_companion_spawn_egg", () ->
                    new SpawnEggItem(
                            ModEntities.ENDER_COMPANION.get(),
                            0x161616,
                            0xCC00FA,
                            new Item.Properties()
                    )
            );

    private ModItems() {
    }

    public static void register() {
        ITEMS.register();
        // Add the spawn egg to the vanilla Spawn Eggs creative tab
        CreativeTabRegistry.modify(CreativeModeTabs.SPAWN_EGGS,
                output -> output.accept(ENDER_COMPANION_SPAWN_EGG.get().getDefaultInstance()));
    }
}
