package com.bubble.dragon.physics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** 驗證 AABB 的重疊、分離與僅接觸邊界三種情況。 */
class OverlapCheckerTest {
    @Test
    void detectsOverlap() {
        assertTrue(OverlapChecker.overlaps(0, 0, 10, 10, 5, 5, 10, 10));
    }

    @Test
    void rejectsSeparatedObjects() {
        assertFalse(OverlapChecker.overlaps(0, 0, 10, 10, 11, 0, 10, 10));
    }

    @Test
    void touchingEdgesIsNotOverlap() {
        assertFalse(OverlapChecker.overlaps(0, 0, 10, 10, 10, 0, 10, 10));
    }
}
