package dev.toprak.lifesteal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LifestealTest {

    @Test
    void testHeartCalculation() {
        double maxHealth = 20.0;
        int hearts = (int) Math.round(maxHealth / 2.0);
        assertEquals(10, hearts);

        int newHearts = hearts + 1;
        double newHealth = newHearts * 2.0;
        assertEquals(22.0, newHealth);
    }

    @Test
    void testBoundaryChecks() {
        int minHearts = 1;
        int maxHearts = 20;

        int current = 10;
        assertTrue(current - 1 >= minHearts);
        assertTrue(current + 1 <= maxHearts);

        int lowest = 1;
        assertFalse(lowest - 1 >= minHearts);
    }
}
