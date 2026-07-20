package com.bubble.dragon.physics;

import com.bubble.dragon.entity.GameObject;

public final class OverlapChecker {
    private OverlapChecker() {}
    public static boolean overlaps(GameObject a, GameObject b) {
        return overlaps(a.getX(), a.getY(), a.getWidth(), a.getHeight(),
                b.getX(), b.getY(), b.getWidth(), b.getHeight());
    }
    public static boolean overlaps(double ax, double ay, double aw, double ah,
                                   double bx, double by, double bw, double bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }
}
