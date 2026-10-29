package dev.toprak.lifesteal.manager;

/**
 * Pure integer/double helpers for heart math. No Bukkit dependency so it can be unit tested.
 */
public final class HeartMath {
    /** Vanilla max_health attribute is capped at 1024.0 = 512 hearts. */
    public static final int HARD_MAX_HEARTS = 512;
    public static final int HARD_MIN_HEARTS = 1;

    private HeartMath() {}

    public static int clampHearts(int hearts) {
        return Math.max(HARD_MIN_HEARTS, Math.min(HARD_MAX_HEARTS, hearts));
    }

    public static int clampHearts(int hearts, int min, int max) {
        int lo = clampHearts(min);
        int hi = Math.max(lo, clampHearts(max));
        return Math.max(lo, Math.min(hi, hearts));
    }

    /** Sanitizes a configured minimum: never below 1 (0 would make elimination unreachable). */
    public static int sanitizeMin(int configuredMin) {
        return clampHearts(configuredMin);
    }

    /** Sanitizes a configured maximum: never below the minimum, never above the attribute cap. */
    public static int sanitizeMax(int configuredMax, int min) {
        return Math.max(sanitizeMin(min), clampHearts(configuredMax));
    }

    public static int healthToHearts(double baseHealth) {
        if (Double.isNaN(baseHealth) || baseHealth <= 0) return HARD_MIN_HEARTS;
        long rounded = Math.round(baseHealth / 2.0);
        return clampHearts((int) Math.min(rounded, HARD_MAX_HEARTS));
    }

    public static double heartsToHealth(int hearts) {
        return clampHearts(hearts) * 2.0;
    }

    /**
     * Whether a withdrawal of {@code amount} hearts is allowed without going under {@code min}.
     * Overflow-safe for any int inputs.
     */
    public static boolean canWithdraw(int current, int amount, int min) {
        if (amount <= 0) return false;
        long sanitizedMin = sanitizeMin(min);
        long sanitizedCurrent = Math.min((long) current, HARD_MAX_HEARTS);
        return sanitizedCurrent - (long) amount >= sanitizedMin;
    }

    /** Splits a total into stack sizes no larger than {@code maxStack}. */
    public static int[] splitStacks(int total, int maxStack) {
        if (total <= 0) return new int[0];
        int stackSize = Math.max(1, maxStack);
        int full = total / stackSize;
        int rem = total % stackSize;
        int[] out = new int[full + (rem > 0 ? 1 : 0)];
        for (int i = 0; i < full; i++) out[i] = stackSize;
        if (rem > 0) out[full] = rem;
        return out;
    }
}
