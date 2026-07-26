package com.bubble.dragon.util;

/** 集中保存會影響遊戲手感與版面的固定值，方便日後統一調整。 */
public final class Constants {
    public static final String GAME_TITLE = "Bubble Dragon";
    public static final double WIDTH = 960;
    public static final double HEIGHT = 640;
    public static final double HUD_HEIGHT = 54;
    public static final double DOOR_X = 850;
    public static final double DOOR_Y = 430;
    public static final double DOOR_WIDTH = 48;
    public static final double DOOR_HEIGHT = 70;
    // 速度單位為 pixel/second，重力為 pixel/second²。
    public static final double GRAVITY = 1_500;
    public static final double PLAYER_SPEED = 260;
    public static final double JUMP_SPEED = 610;
    public static final double PLAYER_DAMAGE_BOUNCE_SPEED = 360;
    public static final double PLAYER_HIT_KNOCKBACK_DISTANCE = 35;
    public static final double PLAYER_MIN_X_AFTER_HIT = 5;
    public static final double RESPAWN_Y_POSITION = 100;
    public static final double ENEMY_SPEED = 90;
    public static final double BUBBLE_SPEED = 390;
    public static final double BUBBLE_RISE_SPEED = 12;
    public static final double BUBBLE_LIFETIME = 5;
    public static final double BUBBLE_HORIZONTAL_MARGIN = 40;
    public static final double BUBBLE_TOP_MARGIN = 50;
    public static final double BUBBLE_SIZE = 30;
    public static final double SHOOT_COOLDOWN_SECONDS = 0.32;
    public static final double TRAP_SECONDS = 3;
    // 視窗卡頓時限制單幀時間，避免角色一次穿過整個平台。
    public static final double MAX_DELTA = 1.0 / 30.0; // 允許遊戲邏輯處理的最低更新頻率約為 30 FPS

    private Constants() {}
}
