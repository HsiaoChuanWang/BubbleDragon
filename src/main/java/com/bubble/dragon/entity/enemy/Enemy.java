package com.bubble.dragon.entity.enemy;

import com.bubble.dragon.entity.GameObject;
import com.bubble.dragon.util.Constants;
import com.bubble.dragon.util.CountdownTimer;

public final class Enemy extends GameObject {
    private static final double WIDTH = 40;
    private static final double HEIGHT = 40;

    private EnemyState state = EnemyState.JUMP;
    private final CountdownTimer trapTimer = new CountdownTimer(Constants.TRAP_SECONDS); // 掙脫倒數
    private int direction;
    private boolean chasing;
    private double stateElapsed;

    public Enemy(double x, double y, int direction) {
        super(x, y, WIDTH, HEIGHT);
        this.direction = direction >= 0 ? 1 : -1;
    }

    public EnemyState getState() {
        return state;
    }

    public CountdownTimer getTrapTimer() {
        return trapTimer;
    }

    public int getDirection() {
        return direction;
    }

    public void reverse() {
        direction *= -1;
    }

    // 讓敵人面向目標；目標在敵人中心右側時向右，否則向左
    public void face(double targetX) {
        direction = targetX >= getX() + getWidth() / 2 ? 1 : -1;
    }

    public boolean isChasing() {
        return chasing;
    }

    public void setChasing(boolean chasing) {
        this.chasing = chasing;
    }

    // 敵人維持目前狀態多久
    public void updateStateTime(double dt) {
        stateElapsed += dt;
    }

    public double getStateElapsed() {
        return stateElapsed;
    }

    public void startJump() {
        state = EnemyState.JUMP;
        stateElapsed = 0;
    }

    public void startFly() {
        state = EnemyState.FLY;
        stateElapsed = 0;
    }

    public void startStop() {
        state = EnemyState.STOP;
        stateElapsed = 0;
        velocityX = velocityY = 0;
    }

    public boolean canBeTrapped() {
        return state == EnemyState.JUMP || state == EnemyState.FLY;
    }

    public void trap() {
        if (!canBeTrapped())
            return;
        state = EnemyState.TRAPPED;
        stateElapsed = 0;
        velocityX = velocityY = 0; // 將速度歸零避免移動
        trapTimer.start();
    }

    public void escape() {
        trapTimer.reset();
        chasing = false;
        startJump();
    }

    public void defeat() {
        state = EnemyState.DEFEATED;
        trapTimer.stop();
    }
}
