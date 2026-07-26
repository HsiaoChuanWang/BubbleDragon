package com.bubble.dragon.entity.enemy;

import com.bubble.dragon.entity.GameObject;
import com.bubble.dragon.util.Constants;
import com.bubble.dragon.util.CountdownTimer;

public final class Enemy extends GameObject {
    private static final double WIDTH = 40;
    private static final double HEIGHT = 40;

    private EnemyState state = EnemyState.MOVING;
    private final CountdownTimer trapTimer = new CountdownTimer(Constants.TRAP_SECONDS); // 掙脫倒數
    private int direction;

    public Enemy(double x, double y, int direction) {
        super(x, y, WIDTH, HEIGHT);
        this.direction = direction >= 0 ? 1 : -1;
    }
    public EnemyState getState() { return state; }
    public CountdownTimer getTrapTimer() { return trapTimer; }
    public int getDirection() { return direction; }
    public void reverse() { direction *= -1; }
    public void trap() { state = EnemyState.TRAPPED; 
        velocityX = velocityY = 0; // 將速度歸零避免移動
        trapTimer.start(); }
    public void escape() { state = EnemyState.MOVING; trapTimer.reset(); }
    public void defeat() { state = EnemyState.DEFEATED; trapTimer.stop(); }
}
