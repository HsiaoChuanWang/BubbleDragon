package com.bubble.dragon.map;

import com.bubble.dragon.entity.GameObject;

// 關卡中的矩形地磚；solid 為 true 時會阻擋角色移動
public final class Tile extends GameObject {
    private final boolean solid;

    public Tile(double x, double y, double width, double height, boolean solid) {
        super(x, y, width, height);
        this.solid = solid;
    }

    public boolean isSolid() {
        return solid;
    }
}
