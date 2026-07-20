package com.bubble.dragon.physics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OverlapCheckerTest {
    @Test void detectsOverlap() { assertTrue(OverlapChecker.overlaps(0, 0, 10, 10, 5, 5, 10, 10)); }
    @Test void rejectsSeparatedObjects() { assertFalse(OverlapChecker.overlaps(0, 0, 10, 10, 11, 0, 10, 10)); }
    @Test void touchingEdgesIsNotOverlap() { assertFalse(OverlapChecker.overlaps(0, 0, 10, 10, 10, 0, 10, 10)); }
}
