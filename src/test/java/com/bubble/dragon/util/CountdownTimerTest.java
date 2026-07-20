package com.bubble.dragon.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CountdownTimerTest {
    @Test void finishesAfterDuration() {
        CountdownTimer timer = new CountdownTimer(3); timer.start(); timer.update(2); assertFalse(timer.isFinished());
        timer.update(1); assertTrue(timer.isFinished()); assertFalse(timer.isRunning());
    }
    @Test void resetRestoresDuration() {
        CountdownTimer timer = new CountdownTimer(3); timer.start(); timer.update(1); timer.reset();
        assertEquals(3, timer.getRemainingSeconds()); assertFalse(timer.isRunning());
    }
    @Test void rejectsNegativeDuration() { assertThrows(IllegalArgumentException.class, () -> new CountdownTimer(-1)); }
}
