package com.nedosug.endercompanion.fabric.client.config;

import com.nedosug.endercompanion.config.EnderCompanionConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

/**
 * ModMenu + Cloth Config integration for Fabric.
 */
public final class ClothConfigIntegration implements ModMenuApi {
    public static void register() {
        // Manual screen registration via ModMenu entrypoint (this class).
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.translatable("endercompanion.config.title"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory general = builder.getOrCreateCategory(Component.translatable("endercompanion.config.category.general"));
            EnderCompanionConfig config = EnderCompanionConfig.get();

            general.addEntry(entryBuilder.startBooleanToggle(
                            Component.translatable("endercompanion.config.enable_companion"),
                            config.enableCompanion)
                    .setDefaultValue(true)
                    .setSaveConsumer(value -> config.enableCompanion = value)
                    .build());

            general.addEntry(entryBuilder.startDoubleField(
                            Component.translatable("endercompanion.config.follow_radius"),
                            config.followRadius)
                    .setDefaultValue(12.0D)
                    .setMin(1.0D)
                    .setMax(64.0D)
                    .setSaveConsumer(value -> config.followRadius = value)
                    .build());

            builder.setSavingRunnable(() -> EnderCompanionConfig.set(config));
            return builder.build();
        };
    }
}
