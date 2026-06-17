package com.nedosug.endercompanion;

import com.nedosug.endercompanion.ai.EmbeddedAIHandler;
import com.nedosug.endercompanion.event.ModEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EnderCompanionMod {
    public static final String MOD_ID = "endercompanion";
    public static final Logger LOGGER = LoggerFactory.getLogger("Ender Companion");

    private EnderCompanionMod() {
    }

    public static void init() {
        LOGGER.info("Initializing {}", MOD_ID);
        ModRegistries.register();
        ModEvents.register();

        // Start AI initialization asynchronously
        EmbeddedAIHandler.initializeAsync();
    }

    public static void shutdown() {
        LOGGER.info("Shutting down {}", MOD_ID);
        EmbeddedAIHandler.shutdown();
    }
}
