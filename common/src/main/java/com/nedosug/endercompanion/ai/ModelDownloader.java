package com.nedosug.endercompanion.ai;

import com.nedosug.endercompanion.EnderCompanionMod;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles automatic downloading of the GGUF AI model from Hugging Face.
 * Downloads asynchronously to avoid blocking Minecraft's startup thread.
 */
public final class ModelDownloader {
    private static final String MODEL_FILENAME = "qwen2.5-1.5b-instruct-q4_k_m.gguf";
    private static final String SYSTEM_PROMPT_FILENAME = "system_prompt.txt";
    private static final String DEFAULT_SYSTEM_PROMPT =
            "Ты — застенчивая девушка-эндермен, верная спутница игрока в Minecraft. " +
            "Отвечай коротко, дружелюбно и на русском языке.";

    private static final String HUGGINGFACE_URL =
            "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf";
    private static final long EXPECTED_SIZE_BYTES = 1_073_741_824L; // ~1GB

    private static final AtomicBoolean isDownloading = new AtomicBoolean(false);
    private static final AtomicLong downloadedBytes = new AtomicLong(0);

    private ModelDownloader() {
    }

    /**
     * Gets the path where the model should be stored.
     * Creates config/endercompanion/ directory if it doesn't exist.
     */
    public static Path getModelPath() {
        try {
            Path configDir = Path.of("config", "endercompanion");
            Files.createDirectories(configDir);
            return configDir.resolve(MODEL_FILENAME);
        } catch (IOException e) {
            EnderCompanionMod.LOGGER.error("Failed to create config directory", e);
            return Path.of(MODEL_FILENAME);
        }
    }

    /**
     * Gets the path to the system prompt configuration file.
     */
    public static Path getSystemPromptPath() {
        try {
            Path configDir = Path.of("config", "endercompanion");
            Files.createDirectories(configDir);
            return configDir.resolve(SYSTEM_PROMPT_FILENAME);
        } catch (IOException e) {
            EnderCompanionMod.LOGGER.error("Failed to create config directory", e);
            return Path.of(SYSTEM_PROMPT_FILENAME);
        }
    }

    /**
     * Ensures system prompt file exists. Creates it with default content if missing.
     */
    public static void ensureSystemPromptExists() {
        Path promptPath = getSystemPromptPath();

        if (!Files.exists(promptPath)) {
            try {
                Files.writeString(promptPath, DEFAULT_SYSTEM_PROMPT, StandardCharsets.UTF_8);
                EnderCompanionMod.LOGGER.info("Created default system prompt file at: {}", promptPath.toAbsolutePath());
            } catch (IOException e) {
                EnderCompanionMod.LOGGER.error("Failed to create system prompt file", e);
            }
        }
    }

    /**
     * Checks if the model file exists and has a reasonable size.
     */
    public static boolean isModelAvailable() {
        Path modelPath = getModelPath();
        if (!Files.exists(modelPath)) {
            return false;
        }

        try {
            long size = Files.size(modelPath);
            // Check if file is at least 100MB (partial/corrupt downloads detection)
            return size > 100_000_000L;
        } catch (IOException e) {
            EnderCompanionMod.LOGGER.error("Failed to check model file size", e);
            return false;
        }
    }

    /**
     * Starts asynchronous download of the model if not already downloading.
     * Returns a CompletableFuture that completes when download finishes.
     */
    public static CompletableFuture<Boolean> downloadModelAsync() {
        if (!isDownloading.compareAndSet(false, true)) {
            EnderCompanionMod.LOGGER.warn("Model download already in progress");
            return CompletableFuture.completedFuture(false);
        }

        EnderCompanionMod.LOGGER.info("Starting AI model download: {}", MODEL_FILENAME);
        EnderCompanionMod.LOGGER.info("Download URL: {}", HUGGINGFACE_URL);
        EnderCompanionMod.LOGGER.info("This may take several minutes depending on your connection...");

        return CompletableFuture.supplyAsync(() -> {
            try {
                Path modelPath = getModelPath();
                Path tempPath = modelPath.resolveSibling(MODEL_FILENAME + ".tmp");

                // Clean up any previous partial downloads
                Files.deleteIfExists(tempPath);

                downloadedBytes.set(0);
                boolean success = downloadFile(HUGGINGFACE_URL, tempPath);

                if (success) {
                    Files.move(tempPath, modelPath, StandardCopyOption.REPLACE_EXISTING);
                    EnderCompanionMod.LOGGER.info("Model download completed successfully!");
                    EnderCompanionMod.LOGGER.info("Model saved to: {}", modelPath.toAbsolutePath());
                    return true;
                } else {
                    Files.deleteIfExists(tempPath);
                    EnderCompanionMod.LOGGER.error("Model download failed");
                    return false;
                }
            } catch (Exception e) {
                EnderCompanionMod.LOGGER.error("Exception during model download", e);
                return false;
            } finally {
                isDownloading.set(false);
            }
        });
    }

    /**
     * Downloads a file from URL to target path with progress logging.
     * Uses chunked reading to avoid memory issues with large files.
     */
    private static boolean downloadFile(String urlString, Path targetPath) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                EnderCompanionMod.LOGGER.error("HTTP error code: {}", responseCode);
                return false;
            }

            long contentLength = connection.getContentLengthLong();
            EnderCompanionMod.LOGGER.info("Download size: {} MB", contentLength / (1024 * 1024));

            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            long finalSize = Files.size(targetPath);
            EnderCompanionMod.LOGGER.info("Downloaded {} MB", finalSize / (1024 * 1024));

            return true;

        } catch (IOException e) {
            EnderCompanionMod.LOGGER.error("Failed to download model", e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Checks if a download is currently in progress.
     */
    public static boolean isDownloading() {
        return isDownloading.get();
    }

    /**
     * Gets the number of bytes downloaded so far (for progress tracking).
     */
    public static long getDownloadedBytes() {
        return downloadedBytes.get();
    }
}
