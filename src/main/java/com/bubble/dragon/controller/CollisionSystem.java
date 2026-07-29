package com.bubble.dragon.controller;

import java.util.List;

import com.bubble.dragon.entity.GameObject;
import com.bubble.dragon.entity.player.Player;
import com.bubble.dragon.map.Tile;
import com.bubble.dragon.physics.OverlapChecker;
import com.bubble.dragon.util.Constants;

// 碰種相關: 處理物件移動、地圖邊界以及與實心磚塊的碰撞修正
public final class CollisionSystem {
    private final List<Tile> tiles;

    public CollisionSystem(List<Tile> tiles) {
        this.tiles = tiles;
    }

    // 物件移動，並檢查是否碰到實心磚塊，若碰到則修正位置與速度
    public void moveWithTiles(GameObject object, double dt) {
        // 先水平移動
        object.setX(object.getX() + object.getVelocityX() * dt);

        // 若物件碰到實心磚塊，修正位置與速度
        for (Tile tile : tiles) {
            if (!tile.isSolid() || !OverlapChecker.overlaps(object, tile))
                continue;

            // 速度大於 0，表示物件正往右移動。此時撞到的是磚塊左側，把物件放回磚塊左側
            if (object.getVelocityX() > 0)
                object.setX(tile.getX() - object.getWidth());
            else if (object.getVelocityX() < 0)
                object.setX(tile.getRight());
            object.setVelocityX(0); // 碰到磚塊後，水平速度歸零，避免物件卡在磚塊內持續移動
        }

        double maxX = Constants.WINDOW_WIDTH - object.getWidth();
        object.setX(Math.max(0, Math.min(maxX, object.getX())));
        object.setY(object.getY() + object.getVelocityY() * dt);

        boolean grounded = false;

        // 逐一檢查地圖中的所有磚塊
        for (Tile tile : tiles) {
            // 非實心磚塊不會阻擋物件；沒有重疊時也不需要處理碰撞
            if (!tile.isSolid() || !OverlapChecker.overlaps(object, tile))
                continue;

            // 垂直速度大於 0，表示物件正在向下掉落。
            if (object.getVelocityY() > 0) {
                // 將物件放到磚塊頂部，使物件底部剛好貼齊磚塊頂面
                object.setY(tile.getY() - object.getHeight());
                grounded = true;
            } else if (object.getVelocityY() < 0) {
                // 從下面撞到磚塊時，把玩家推出磚塊並放到磚塊正下方
                object.setY(tile.getBottom());
            }

            // 垂直碰撞發生後停止垂直移動，避免下一幀繼續穿入磚塊
            object.setVelocityY(0);
        }

        // 只有玩家需要保存是否站在地面上的狀態，供跳躍與動畫判斷使用
        if (object instanceof Player player)
            player.setOnGround(grounded);

        if (object.getY() > Constants.WINDOW_HEIGHT) {
            object.setY(Constants.RESPAWN_Y_POSITION);

            // 清除原本的垂直速度，避免重生後繼續高速掉落
            object.setVelocityY(0);

            // 如果掉出畫面的是玩家，扣除生命值並啟動受傷效果
            if (object instanceof Player player)
                player.damage();
        }
    }

    /** 檢查物件移到指定 X 後，腳底是否仍有可站立的實心磚塊。 */
    public boolean hasSupportAt(GameObject object, double targetX) {
        double footY = object.getBottom();
        double leftFoot = targetX + 2;
        double rightFoot = targetX + object.getWidth() - 2;
        return hasSupportAtPoint(leftFoot, footY) && hasSupportAtPoint(rightFoot, footY);
    }

    // 檢查指定的腳底點是否正站在任一實體平台上
    private boolean hasSupportAtPoint(double x, double footY) {
        for (Tile tile : tiles) {
            // 檢查腳底高度是否接近平台頂端；允許少量誤差以避免浮點數無法完全相等
            boolean touchesTop = Math.abs(footY - tile.getY()) <= Constants.PLATFORM_SUPPORT_TOLERANCE;

            // 檢查腳底點的 X 座標是否落在平台的左、右邊界之間
            boolean horizontallyOverlaps = x >= tile.getX() && x <= tile.getRight();

            // 平台必須是實體、腳底高度碰到平台頂端，而且腳底點位於平台寬度內
            if (tile.isSolid() && touchesTop && horizontallyOverlaps)
                return true;
        }
        return false;
    }
}
