package com.bubble.dragon.entity.weapon;

import com.bubble.dragon.entity.GameObject;
import com.bubble.dragon.entity.enemy.Enemy;
import com.bubble.dragon.util.Constants;

// 未捕捉敵人時水平飛行，捕捉後則帶著敵人向上飄
public final class Bubble extends GameObject {
    private Enemy trappedEnemy; // null 表示這顆泡泡目前沒有困住敵人
    private double age; // 這顆泡泡存在了多久
    private boolean active = true; // 泡泡是否還在
    private BubbleMovementState movementState = BubbleMovementState.HORIZONTAL;

    public Bubble(double x, double y, double velocityX) {
        super(x, y, Constants.BUBBLE_SIZE, Constants.BUBBLE_SIZE);
        this.velocityX = velocityX;
    }

    public void updateAge(double dt) {
        age += dt;
    }

    public double getAge() {
        return age;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public Enemy getTrappedEnemy() {
        return trappedEnemy;
    }

    public boolean hasTrappedEnemy() {
        return trappedEnemy != null;
    }

    public BubbleMovementState getMovementState() {
        return movementState;
    }

    public void startRising() {
        movementState = BubbleMovementState.RISING;
        velocityX = 0;
        velocityY = -Constants.BUBBLE_RISE_SPEED;
    }

    public void stopAtTop() {
        movementState = BubbleMovementState.STOPPED_AT_TOP;
        velocityX = 0;
        velocityY = 0;
    }

    public void trap(Enemy enemy) {
        trappedEnemy = enemy;
        movementState = BubbleMovementState.RISING;
        velocityX = 0;
        velocityY = Constants.TRAPPED_BUBBLE_VERTICAL_SPEED;
    }
}
