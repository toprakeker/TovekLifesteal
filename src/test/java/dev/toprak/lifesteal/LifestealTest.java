package dev.toprak.lifesteal;

import dev.toprak.lifesteal.manager.HeartMath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifestealTest {

    @Test
    void healthHeartRoundTrip() {
        assertEquals(10, HeartMath.healthToHearts(20.0));
        assertEquals(22.0, HeartMath.heartsToHealth(11));
        assertEquals(1, HeartMath.healthToHearts(1.0));
    }

    @Test
    void healthToHeartsNeverUnderflowsOrOverflows() {
        assertEquals(1, HeartMath.healthToHearts(0.0));
        assertEquals(1, HeartMath.healthToHearts(-50.0));
        assertEquals(1, HeartMath.healthToHearts(Double.NaN));
        assertEquals(HeartMath.HARD_MAX_HEARTS, HeartMath.healthToHearts(Double.MAX_VALUE));
        assertEquals(HeartMath.HARD_MAX_HEARTS, HeartMath.healthToHearts(Double.POSITIVE_INFINITY));
        assertEquals(HeartMath.HARD_MAX_HEARTS, HeartMath.healthToHearts(1.0e12));
    }

    @Test
    void setHeartsIsHardClamped() {
        assertEquals(2.0, HeartMath.heartsToHealth(Integer.MIN_VALUE));
        assertEquals(2.0, HeartMath.heartsToHealth(0));
        assertEquals(1024.0, HeartMath.heartsToHealth(Integer.MAX_VALUE));
        assertEquals(1024.0, HeartMath.heartsToHealth(513));
    }

    @Test
    void configSanitizing() {
        assertEquals(1, HeartMath.sanitizeMin(0));
        assertEquals(1, HeartMath.sanitizeMin(-5));
        assertEquals(3, HeartMath.sanitizeMin(3));
        assertEquals(20, HeartMath.sanitizeMax(20, 1));
        assertEquals(5, HeartMath.sanitizeMax(2, 5));
        assertEquals(HeartMath.HARD_MAX_HEARTS, HeartMath.sanitizeMax(Integer.MAX_VALUE, 1));
        assertEquals(10, HeartMath.clampHearts(10, 1, 20));
        assertEquals(20, HeartMath.clampHearts(99, 1, 20));
        assertEquals(1, HeartMath.clampHearts(-1, 1, 20));
    }

    @Test
    void withdrawIsOverflowSafe() {
        assertTrue(HeartMath.canWithdraw(10, 9, 1));
        assertFalse(HeartMath.canWithdraw(10, 10, 1));
        assertFalse(HeartMath.canWithdraw(1, 1, 1));
        assertFalse(HeartMath.canWithdraw(10, Integer.MAX_VALUE, 1));
        assertFalse(HeartMath.canWithdraw(10, Integer.MIN_VALUE, 1));
        assertFalse(HeartMath.canWithdraw(10, 0, 1));
        assertFalse(HeartMath.canWithdraw(Integer.MAX_VALUE, 1, Integer.MAX_VALUE));
        // min-hearts of 0 in config is treated as 1
        assertFalse(HeartMath.canWithdraw(1, 1, 0));
    }

    @Test
    void eliminationTriggersAtMinimum() {
        int min = HeartMath.sanitizeMin(1);
        assertTrue(1 - 1 < min);
        assertFalse(2 - 1 < min);
        // min-hearts: 0 must still eliminate a player dying on their last heart
        int badMin = HeartMath.sanitizeMin(0);
        assertTrue(1 - 1 < badMin);
    }

    @Test
    void stackSplittingNeverProducesOversizedStacks() {
        assertArrayEquals(new int[0], HeartMath.splitStacks(0, 64));
        assertArrayEquals(new int[0], HeartMath.splitStacks(-3, 64));
        assertArrayEquals(new int[]{5}, HeartMath.splitStacks(5, 64));
        assertArrayEquals(new int[]{64}, HeartMath.splitStacks(64, 64));
        assertArrayEquals(new int[]{64, 64, 2}, HeartMath.splitStacks(130, 64));
        assertArrayEquals(new int[]{1, 1, 1}, HeartMath.splitStacks(3, 1));
        for (int size : HeartMath.splitStacks(1000, 16)) {
            assertTrue(size >= 1 && size <= 16);
        }
        int sum = 0;
        for (int size : HeartMath.splitStacks(1000, 16)) sum += size;
        assertEquals(1000, sum);
    }
}
