package com.bubble.dragon.entity.player;

import com.bubble.dragon.entity.GameObject;

public final class Player extends GameObject {
    private PlayerState state = PlayerState.IDLE;
    private int hp = 3;
    private boolean facingRight = true;
    private boolean onGround;
    private double invulnerableTime;

    public Player(double x, double y) { super(x, y, 42, 52); }
    public PlayerState getState() { return state; }
    public void setState(PlayerState state) { this.state = state; }
    public int getHp() { return hp; }
    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    public boolean isOnGround() { return onGround; }
    public void setOnGround(boolean onGround) { this.onGround = onGround; }
    public boolean isInvulnerable() { return invulnerableTime > 0; }
    public void updateInvulnerability(double dt) { invulnerableTime = Math.max(0, invulnerableTime - dt); }
    public void damage() {
        if (isInvulnerable() || hp <= 0) return;
        hp--; invulnerableTime = 1.2;
        if (hp == 0) state = PlayerState.DEAD;
    }
}
