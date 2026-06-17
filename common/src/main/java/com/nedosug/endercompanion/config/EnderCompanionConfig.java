package com.nedosug.endercompanion.config;

/**
 * Shared config model. Platform-specific Cloth Config screens live in fabric/forge client packages.
 */
public final class EnderCompanionConfig {
    public boolean enableCompanion = true;
    public double followRadius = 12.0D;

    private static EnderCompanionConfig instance = new EnderCompanionConfig();

    private EnderCompanionConfig() {
    }

    public static EnderCompanionConfig get() {
        return instance;
    }

    public static void set(EnderCompanionConfig config) {
        instance = config;
    }
}
