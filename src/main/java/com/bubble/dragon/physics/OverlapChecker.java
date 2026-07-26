package com.bubble.dragon.physics;

import com.bubble.dragon.entity.GameObject;

// 判斷有沒有兩個物件重疊在一起
// Axis-Aligned Bounding Box (AABB) collision detection 軸對齊邊界框
// 不管物件的形狀，都用方正的隱形矩形（且不能旋轉）包住，計算這 2 個矩形 (a 和 b) 有沒有疊在一起
public final class OverlapChecker {
    private OverlapChecker() {}

    public static boolean overlaps(GameObject a, GameObject b) {
        return overlaps(a.getX(), a.getY(), a.getWidth(), a.getHeight(),
                b.getX(), b.getY(), b.getWidth(), b.getHeight());
    }

    // A 完全在 B 的左邊、右邊、上面或下面，這 4 種情況都不是重疊即算 "不重疊"，反之則是 "重疊"
    // ax < bx + bw：A 的左邊界，必須在 B 的右邊界以左
    // ax + aw > bx：A 的右邊界，必須在 B 的左邊界以右
    // ay < by + bh：A 的上邊界，必須在 B 的下邊界之上
    // ay + ah > by：A 的下邊界，必須在 B 的上邊界之下
    public static boolean overlaps(double ax, double ay, double aw, double ah,
                                   double bx, double by, double bw, double bh) {
        // 僅邊緣相接不算重疊，因此使用嚴格小於與大於
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }
}
