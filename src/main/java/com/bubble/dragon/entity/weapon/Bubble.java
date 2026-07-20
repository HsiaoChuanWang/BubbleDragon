package com.bubble.dragon.entity.weapon;

import com.bubble.dragon.entity.GameObject;
import com.bubble.dragon.entity.enemy.Enemy;

public final class Bubble extends GameObject {
    private Enemy trappedEnemy;
    private double age;
    private boolean active = true;

    public Bubble(double x, double y, double velocityX) {
        super(x, y, 30, 30);
        this.velocityX = velocityX;
    }
    public void updateAge(double dt) { age += dt; }
    public double getAge() { return age; }
    public boolean isActive() { return active; }
    public void deactivate() { active = false; }
    public Enemy getTrappedEnemy() { return trappedEnemy; }
    public boolean hasTrappedEnemy() { return trappedEnemy != null; }
    public void trap(Enemy enemy) { trappedEnemy = enemy; velocityX = 0; velocityY = -45; }
}
