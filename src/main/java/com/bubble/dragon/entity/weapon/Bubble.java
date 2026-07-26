package com.bubble.dragon.entity.weapon;

import com.bubble.dragon.entity.GameObject;
import com.bubble.dragon.entity.enemy.Enemy;

// 未捕捉敵人時水平飛行，捕捉後則帶著敵人向上飄
public final class Bubble extends GameObject {
    private static final double WIDTH = 30;
    private static final double HEIGHT = 30;
    private static final double TRAPPED_VERTICAL_VELOCITY = -45; // 捕捉敵人後向上飄的速度

    private Enemy trappedEnemy; // null 表示這顆泡泡目前沒有困住敵人
    private double age; // 這顆泡泡存在了多久
    private boolean active = true; // 泡泡是否還在

    public Bubble(double x, double y, double velocityX) {
        super(x, y, WIDTH, HEIGHT);
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

    public void trap(Enemy enemy) {
        trappedEnemy = enemy;
        velocityX = 0;
        velocityY = TRAPPED_VERTICAL_VELOCITY;
    }
}
