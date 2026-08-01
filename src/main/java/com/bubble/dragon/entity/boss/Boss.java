package com.bubble.dragon.entity.boss;

import com.bubble.dragon.entity.GameObject;
import com.bubble.dragon.util.Constants;

public final class Boss extends GameObject {
    // public static final double SIZE = 40;
    public static final double WIDTH = 60;
    public static final double HEIGHT = 120;
    private static final int INITIAL_HP = 5;

    private BossState state = BossState.INACTIVE;
    private int direction = 1;
    private int hp = INITIAL_HP;
    private boolean chasing;
    private double invulnerableTime; // 無敵時間

    public Boss() {
        super(0, 0, WIDTH, HEIGHT);
    }

    public BossState getState() {
        return state;
    }

    public void setState(BossState state) {
        this.state = state;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        // 將任意數值正規化為 -1 或 1，避免出現方向為 0 而停止移動
        this.direction = direction >= 0 ? 1 : -1;
    }

    public void reverse() {
        direction *= -1;
    }

    public void face(double targetX) {
        // targetX 與 Boss 中心比較，決定面向左或右
        direction = targetX >= getX() + getWidth() / 2 ? 1 : -1;
    }

    public boolean isChasing() {
        return chasing;
    }

    public void setChasing(boolean chasing) {
        this.chasing = chasing;
    }

    public int getHp() {
        return hp;
    }

    public boolean isInvulnerable() {
        return invulnerableTime > 0;
    }

    public void updateInvulnerability(double dt) {
        invulnerableTime = Math.max(0, invulnerableTime - dt);
    }

    public boolean damage() {
        if (isInvulnerable() || isDefeated())
            return false;
        hp--;

        if (hp <= 0) {
            // 第五次有效命中後立即停止所有位移
            state = BossState.DEFEATED;
            velocityX = velocityY = 0;
        } else {
            // 尚未死亡時開啟無敵時間，避免同一群重疊泡泡瞬間扣完五條命
            invulnerableTime = Constants.BOSS_INVULNERABILITY_SECONDS;
            chasing = false;
        }
        return true;
    }

    public boolean isActive() {
        return state != BossState.INACTIVE;
    }

    public boolean isDefeated() {
        return state == BossState.DEFEATED;
    }
}
