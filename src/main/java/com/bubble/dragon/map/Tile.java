package com.bubble.dragon.map;

import java.util.List;

import com.bubble.dragon.entity.GameObject;

// 關卡中的矩形地磚
// solid 為 true 時會阻擋角色移動
public final class Tile extends GameObject {
    public static final double COLLISION_HEIGHT = 24;

    private final boolean solid;
    private final List<String> texturePattern;
    private final double renderY; // tile 圖片最上邊的 y 座標

    public Tile(
            double x,
            double y,
            double width,
            double collisionOffsetY, // 可以手動調整碰撞箱的高度，避免碰撞箱太高導致玩家懸浮
            boolean solid,
            List<String> texturePattern) {

        // 磚塊圖片可向下完整延伸，物理碰撞永遠只使用最上方 24px
        super(x, y + collisionOffsetY, width, COLLISION_HEIGHT);
        this.renderY = y;
        this.solid = solid;
        this.texturePattern = List.copyOf(texturePattern);
    }

    public boolean isSolid() {
        return solid;
    }

    public List<String> getTexturePattern() {
        return texturePattern;
    }

    public double getRenderY() {
        return renderY;
    }
}
