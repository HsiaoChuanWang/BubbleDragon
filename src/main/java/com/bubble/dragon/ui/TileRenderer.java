package com.bubble.dragon.ui;

import com.bubble.dragon.map.Tile;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

// 依地圖的 texturePattern 繪製固定寬 24px 的完整地磚圖片，長度不限
final class TileRenderer {
    private static final double TEXTURE_WIDTH = 24;

    void draw(GraphicsContext graphics, Iterable<Tile> tiles, double offsetY) {
        for (Tile tile : tiles) {
            if (tile.getTexturePattern().isEmpty())
                continue;

            int columns = (int) Math.ceil(tile.getWidth() / TEXTURE_WIDTH);
            for (int column = 0; column < columns; column++) {
                // 計算目前這一小塊材質的 X；每增加一欄就向右移動一個材質寬度
                double blockX = tile.getX() + column * TEXTURE_WIDTH;

                // 計算材質的 Y；offsetY 用來讓地磚在關卡轉場時整體上下移動
                double blockY = tile.getY() + offsetY;

                // 一般材質寬度為 24px；最後一塊若超出地磚右側，就只繪製剩餘寬度
                double blockWidth = Math.min(TEXTURE_WIDTH, tile.getRight() - blockX);

                // 循環選取 texturePattern 中的圖片路徑
                String path = tile.getTexturePattern().get(column % tile.getTexturePattern().size());
                Image texture = ImageLoader.load(path);
                double renderHeight = blockWidth * texture.getHeight() / texture.getWidth();
                graphics.drawImage(texture, blockX, blockY, blockWidth, renderHeight);
            }
        }
    }
}
