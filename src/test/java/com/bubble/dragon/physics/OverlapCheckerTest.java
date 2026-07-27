package com.bubble.dragon.physics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OverlapCheckerTest {
    // 發生重疊時，應回傳 true
    @Test
    void detectsOverlap() {
        assertTrue(OverlapChecker.overlaps(0, 0, 10, 10, 5, 5, 10, 10));
    }

    // 完全分離時，應回傳 false
    @Test
    void rejectsSeparatedObjects() {
        assertFalse(OverlapChecker.overlaps(0, 0, 10, 10, 11, 0, 10, 10));
    }

    // 僅接觸邊界時，應回傳 false
    @Test
    void touchingEdgesIsNotOverlap() {
        assertFalse(OverlapChecker.overlaps(0, 0, 10, 10, 10, 0, 10, 10));
    }
}
