package com.bubble.dragon.util;

/** 集中保存會影響遊戲手感與版面的固定值，方便日後統一調整。 */
public final class Constants {
    // 視窗與遊戲畫面
    public static final String GAME_TITLE = "Bubble Dragon";
    public static final double WINDOW_WIDTH = 960;
    public static final double WINDOW_HEIGHT = 640;
    public static final double HUD_HEIGHT = 54; // 上方資訊欄高度

    // 關卡位置與尺寸
    public static final double DOOR_X = 850;
    public static final double DOOR_Y = 430;
    public static final double DOOR_WIDTH = 48;
    public static final double DOOR_HEIGHT = 70;

    // 共用物理與遊戲迴圈
    // 速度單位為 pixel/second，重力為 pixel/second²
    public static final double GRAVITY = 1_500; // 每秒向下增加的速度
    public static final double RESPAWN_Y_POSITION = 100; // 物件掉出畫面後回到的 Y 座標
    public static final double MAX_DELTA = 1.0 / 30.0; // 單幀最多計算的秒數，避免卡頓時移動過遠

    // 玩家移動與受傷反應
    public static final double PLAYER_SPEED = 260;
    public static final double JUMP_SPEED = 610;
    public static final double PLAYER_DAMAGE_BOUNCE_SPEED = 360; // 玩家受傷後向上彈起的速度
    public static final double PLAYER_HIT_KNOCKBACK_DISTANCE = 35; // 玩家受傷後的水平擊退距離
    public static final double PLAYER_HIT_SCREEN_MARGIN = 5; // 玩家受傷擊退後與畫面左右邊界保留的距離

    // 敵人移動與受困時間
    public static final double ENEMY_SPEED = 90;
    public static final double TRAP_SECONDS = 3;

    // 泡泡移動、存在時間與活動範圍
    public static final double BUBBLE_SPEED = 390;
    public static final double BUBBLE_RISE_SPEED = 12; // 普通泡泡飛行時的上升速度
    public static final double BUBBLE_LIFETIME = 5; // 泡泡最多存在的秒數
    public static final double BUBBLE_HORIZONTAL_MARGIN = 40; // 泡泡超出畫面左右多少距離後消失
    public static final double BUBBLE_TOP_MARGIN = 50; // 泡泡超出畫面上方多少距離後消失
    public static final double BUBBLE_SIZE = 30;
    public static final double SHOOT_COOLDOWN_SECONDS = 0.32; // 兩次發射泡泡之間需等待的秒數

    private Constants() {
    }
}
