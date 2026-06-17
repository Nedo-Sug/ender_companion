package com.nedosug.endercompanion.ai;

import com.nedosug.endercompanion.EnderCompanionMod;
import de.kherud.llama.InferenceParameters;
import de.kherud.llama.LlamaModel;
import de.kherud.llama.ModelParameters;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Manages the embedded AI model lifecycle and provides async inference.
 * This handler runs the GGUF model in a separate thread pool to avoid blocking the game.
 */
public final class EmbeddedAIHandler {
    private static final int MAX_TOKENS = 64;
    private static final float TEMPERATURE = 0.8f;

    // Thread pool for AI inference (single thread to avoid context issues)
    private static final ExecutorService inferenceExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "EnderCompanion-AI");
        thread.setDaemon(true);
        return thread;
    });

    private static final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private static final AtomicBoolean isInitializing = new AtomicBoolean(false);

    @Nullable
    private static LlamaModel model;

    private EmbeddedAIHandler() {
    }

    /**
     * Initializes the AI model. Should be called after world loads.
     * This is a non-blocking operation - initialization happens in background.
     */
    public static void initializeAsync() {
        if (isInitialized.get() || isInitializing.get()) {
            return;
        }

        if (!isInitializing.compareAndSet(false, true)) {
            return;
        }

        // Ensure system prompt file exists
        ModelDownloader.ensureSystemPromptExists();

        CompletableFuture.runAsync(() -> {
            try {
                if (!ModelDownloader.isModelAvailable()) {
                    EnderCompanionMod.LOGGER.info("AI model not found, starting download...");
                    boolean downloaded = ModelDownloader.downloadModelAsync().join();
                    if (!downloaded) {
                        EnderCompanionMod.LOGGER.error("Failed to download AI model");
                        isInitializing.set(false);
                        return;
                    }
                }

                Path modelPath = ModelDownloader.getModelPath();
                EnderCompanionMod.LOGGER.info("Loading AI model from: {}", modelPath.toAbsolutePath());

                ModelParameters modelParams = new ModelParameters()
                        .setNGpuLayers(0) // CPU-only for compatibility
                        .setModelFilePath(modelPath.toString());

                model = new LlamaModel(modelParams);

                isInitialized.set(true);
                EnderCompanionMod.LOGGER.info("AI model initialized successfully");

            } catch (Exception e) {
                EnderCompanionMod.LOGGER.error("Failed to initialize AI model", e);
                model = null;
                isInitialized.set(false);
            } finally {
                isInitializing.set(false);
            }
        }, inferenceExecutor);
    }

    /**
     * Asks the Ender Companion AI a question asynchronously.
     * The callback will be invoked with the response on the inference thread.
     *
     * @param companion The companion entity (for friendship level context)
     * @param playerMessage The message from the player
     * @param callback Consumer that receives the AI response
     */
    public static void askEnderGirl(com.nedosug.endercompanion.entity.EnderCompanionEntity companion,
                                     String playerMessage,
                                     Consumer<String> callback) {
        // Check if downloading
        if (ModelDownloader.isDownloading()) {
            callback.accept(getLocalizedFallback("chat.endercompanion.ai.downloading"));
            return;
        }

        // Check if initializing
        if (isInitializing.get() && !isInitialized.get()) {
            callback.accept(getLocalizedFallback("chat.endercompanion.ai.initializing"));
            return;
        }

        // Check if failed to initialize
        if (!isInitialized.get()) {
            callback.accept(getLocalizedFallback("chat.endercompanion.ai.error_confused"));
            return;
        }

        // Perform inference in background
        CompletableFuture.runAsync(() -> {
            try {
                String response = generateResponse(companion, playerMessage);
                callback.accept(response);
            } catch (Exception e) {
                EnderCompanionMod.LOGGER.error("Error during AI inference", e);
                callback.accept(getLocalizedFallback("chat.endercompanion.ai.error_words"));
            }
        }, inferenceExecutor);
    }

    /**
     * Gets a localized fallback message.
     * Since we're in a background thread, we use the translation key directly.
     */
    private static String getLocalizedFallback(String key) {
        return net.minecraft.network.chat.Component.translatable(key).getString();
    }

    /**
     * Loads the system prompt from the configuration file.
     */
    private static String loadSystemPrompt() {
        try {
            Path promptPath = ModelDownloader.getSystemPromptPath();
            if (Files.exists(promptPath)) {
                return Files.readString(promptPath, StandardCharsets.UTF_8).trim();
            }
        } catch (Exception e) {
            EnderCompanionMod.LOGGER.error("Failed to load system prompt from file", e);
        }

        // Fallback to default if file reading fails
        return "Ты — застенчивая девушка-эндермен, верная спутница игрока в Minecraft. " +
               "Отвечай коротко, дружелюбно и на русском языке.";
    }

    /**
     * Generates a response using the loaded model.
     * This method MUST be called from the inference thread.
     *
     * @param companion The companion entity (for friendship context)
     * @param userMessage The player's message
     */
    private static String generateResponse(com.nedosug.endercompanion.entity.EnderCompanionEntity companion,
                                           String userMessage) {
        if (model == null) {
            return getLocalizedFallback("chat.endercompanion.ai.error_confused");
        }

        try {
            // Load fresh system prompt from config file
            String systemPrompt = loadSystemPrompt();

            // Get current friendship level
            int friendshipLevel = companion.getFriendshipLevel();

            // Build context string with friendship
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append(String.format("[Текущий уровень дружбы с игроком: %d из 100]", friendshipLevel));

            // Add backpack context if equipped
            if (companion.hasBackpack()) {
                contextBuilder.append("\n[На твоей спине надет походный рюкзак игрока. Ты рада помогать ему нести вещи]");
            }

            // Build full prompt
            String fullPrompt = String.format("%s\n%s\n\nUser: %s\nAssistant:",
                    contextBuilder.toString(), systemPrompt, userMessage);

            InferenceParameters inferParams = new InferenceParameters(fullPrompt)
                    .setTemperature(TEMPERATURE)
                    .setNPredict(MAX_TOKENS)
                    .setStopStrings("\n", "User:");

            // Generate response (blocking call, but we're in a separate thread)
            StringBuilder response = new StringBuilder();
            for (LlamaModel.Output output : model.generate(inferParams)) {
                response.append(output.text);
            }

            String result = response.toString().trim();

            // Fallback if empty response
            if (result.isEmpty()) {
                return getLocalizedFallback("chat.endercompanion.ai.empty_response");
            }

            return result;

        } catch (Exception e) {
            EnderCompanionMod.LOGGER.error("Error generating AI response", e);
            return getLocalizedFallback("chat.endercompanion.ai.distracted");
        }
    }

    /**
     * Checks if the AI is ready to respond.
     */
    public static boolean isReady() {
        return isInitialized.get() && model != null;
    }

    /**
     * Checks if the AI is currently initializing.
     */
    public static boolean isInitializing() {
        return isInitializing.get();
    }

    /**
     * Shuts down the AI handler and releases resources.
     * Should be called when the game closes.
     */
    public static void shutdown() {
        if (model != null) {
            try {
                model.close();
                model = null;
                isInitialized.set(false);
                EnderCompanionMod.LOGGER.info("AI model closed successfully");
            } catch (Exception e) {
                EnderCompanionMod.LOGGER.error("Error closing AI model", e);
            }
        }

        inferenceExecutor.shutdown();
    }
}
