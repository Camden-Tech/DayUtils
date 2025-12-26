package me.BaddCamden.DayUtils.config;

/**
 * Centralises configuration defaults and validation bounds.
 */
public final class SettingsConstraints {
    public static final long DEFAULT_DAY_LENGTH = 12000L;
    public static final long DEFAULT_NIGHT_LENGTH = 12000L;
    public static final double DEFAULT_SPEED = 1.0d;

    public static final long MIN_LENGTH_TICKS = 20L;
    public static final long MAX_LENGTH_TICKS = 240000L;
    public static final double MIN_SPEED = 0.1d;
    public static final double MAX_SPEED = 10.0d;

    public static final long MIN_CUSTOM_INTERVAL = 1L;

    private SettingsConstraints() {
    }

    /**
     * Clamps a day or night length to the configured limits.
     */
    public static long clampLength(long value) {
        return clamp(value, MIN_LENGTH_TICKS, MAX_LENGTH_TICKS);
    }

    /**
     * Clamps a speed multiplier to the allowed range.
     */
    public static double clampSpeed(double value) {
        return clamp(value, MIN_SPEED, MAX_SPEED);
    }

    /**
     * Clamps a custom day interval to the same bounds as cycle lengths.
     */
    public static long clampCustomInterval(long value) {
        return clamp(value, MIN_CUSTOM_INTERVAL, MAX_LENGTH_TICKS);
    }

    /**
     * Restricts a long value to the provided range.
     */
    private static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Restricts a double value to the provided range.
     */
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
