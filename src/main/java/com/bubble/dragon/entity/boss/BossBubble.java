package com.bubble.dragon.entity.boss;

import com.bubble.dragon.entity.GameObject;
import com.bubble.dragon.util.Constants;

// Boss 發射的直線攻擊泡泡；大小直接共用玩家泡泡的全域常數
public final class BossBubble extends GameObject {
    public BossBubble(double x, double y, double velocityX, double velocityY) {
        super(x, y, Constants.BUBBLE_SIZE, Constants.BUBBLE_SIZE);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public void update(double dt) {
        // 以「速度 × 秒數」更新，讓移動速度不受畫面幀率影響
        x += velocityX * dt;
        y += velocityY * dt;
    }
}
