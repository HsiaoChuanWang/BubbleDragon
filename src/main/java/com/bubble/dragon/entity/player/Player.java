package com.bubble.dragon.entity.player;

import com.bubble.dragon.entity.GameObject;

// 不負責移動玩家，只保存玩家目前的狀態
public final class Player extends GameObject {
    private static final int INITIAL_HP = 3;
    private static final double WIDTH = 68;
    private static final double HEIGHT = 52;
    private static final double INVULNERABILITY_DURATION_SECONDS = 1.2; // 無敵時間

    private PlayerState state = PlayerState.IDLE;
    private int hp = INITIAL_HP;
    private boolean facingRight = true; // 角色面向哪裡
    private boolean onGround;
    private double invulnerableTime;

    public Player(double x, double y) {
        super(x, y, WIDTH, HEIGHT);
    }

    public PlayerState getState() {
        return state;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public int getHp() {
        return hp;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean isInvulnerable() {
        return invulnerableTime > 0;
    }

    public void updateInvulnerability(double dt) {
        invulnerableTime = Math.max(0, invulnerableTime - dt);
    }

    public void damage() {
        if (isInvulnerable() || hp <= 0)
            return;
        hp--;
        invulnerableTime = INVULNERABILITY_DURATION_SECONDS;
        if (hp == 0)
            state = PlayerState.DEAD;
    }
}
