package com.bubble.dragon.map;

import com.bubble.dragon.entity.GameObject;

public final class Tile extends GameObject {
    private final boolean solid;
    public Tile(double x, double y, double width, double height, boolean solid) {
        super(x, y, width, height); this.solid = solid;
    }
    public boolean isSolid() { return solid; }
}
