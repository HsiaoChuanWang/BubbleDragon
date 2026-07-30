package com.bubble.dragon.util;

/** 集中保存會影響遊戲手感與版面的固定值，方便日後統一調整。 */
public final class Constants {
    // 視窗與遊戲畫面
    public static final String GAME_TITLE = "Bubble Dragon";
    public static final double WINDOW_WIDTH = 960;
    public static final double WINDOW_HEIGHT = 640;
    public static final double HUD_HEIGHT = 54; // 上方資訊欄高度

    // 漫畫過場
    public static final double COMIC_SLIDE_SPEED = 1_200; // 漫畫過場速度（pixel/second）；數值越大，圖片加入得越快
    public static final double COMIC_FINAL_HOLD_SECONDS = 1.2; // 幾秒後進入 GameView

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
    public static final double PLATFORM_SUPPORT_TOLERANCE = 3; // 腳底接觸平台時允許的像素誤差
    public static final double LEVEL_TRANSITION_SECONDS = 5; // 第一關切換到第二關的背景捲動與玩家移動時間
    public static final double LEVEL_TRANSITION_PLAYER_MARGIN = 24; // 轉場結束時，玩家與畫面右側保留的距離

    // 玩家移動與受傷反應
    public static final double PLAYER_SPEED = 260;
    public static final double JUMP_SPEED = 610;
    public static final double PLAYER_DAMAGE_BOUNCE_SPEED = 360; // 玩家受傷後向上彈起的速度
    public static final double PLAYER_HIT_KNOCKBACK_DISTANCE = 35; // 玩家受傷後的水平擊退距離
    public static final double PLAYER_HIT_SCREEN_MARGIN = 5; // 玩家受傷擊退後與畫面左右邊界保留的距離

    // 敵人移動與受困時間
    public static final double ENEMY_PATROL_SPEED = 67.5; // 尚未發現玩家時，在平台上巡邏的水平速度
    public static final double ENEMY_CHASE_SPEED = 120; // 發現玩家後，朝目前方向追擊的水平速度
    public static final double ENEMY_NOTICE_DISTANCE = 160; // 在敵人左右 160px 內時，才可能被敵人發現
    public static final double ENEMY_FORGET_DISTANCE = 220; // 水平中心距離超過此值時，敵人停止追擊
    public static final double ENEMY_NOTICE_HEIGHT = 48; // 在敵人上下 48px 內，敵人才會發現玩家
    public static final double ENEMY_FORGET_HEIGHT = 72; // 垂直中心距離超過此值時，敵人停止追擊
    public static final double ENEMY_JUMP_SECONDS = 1; // 敵人維持 JUMP 狀態後切換成 FLY 的秒數
    public static final double ENEMY_FLY_SECONDS = 1; // 敵人維持 FLY 狀態後切換成 STOP 的秒數
    public static final double ENEMY_STOP_SECONDS = 1; // 敵人維持 STOP 狀態後重新切換成 JUMP 的秒數
    public static final double TRAP_SECONDS = 20;

    // 泡泡移動、存在時間與活動範圍
    public static final double BUBBLE_SPEED = 390;
    public static final double BUBBLE_HORIZONTAL_TRAVEL_SECONDS = 1; // 普通泡泡水平飛行的秒數
    public static final double BUBBLE_RISE_SPEED = 12; // 普通泡泡飛行時的上升速度
    public static final double TRAPPED_BUBBLE_VERTICAL_SPEED = -45; // 捕捉敵人後向上飄的速度
    public static final double BUBBLE_LIFETIME = 20; // 未困住敵人的普通泡泡最多存在秒數
    public static final double BUBBLE_SIZE = 56; // 大於 40x40 的敵人，讓泡泡能完整包覆敵人
    public static final double SHOOT_COOLDOWN_SECONDS = 0.32; // 兩次發射泡泡之間需等待的秒數

    // Boss 登場由「展翅攻擊 1 秒 + 收翅停火 1 秒」組成，總共重複 2 輪
    public static final double BOSS_SCREAM_SECONDS = 1;
    public static final double BOSS_FOLD_WINGS_SECONDS = 1;
    public static final double BOSS_APPEAR_DELAY_SECONDS = 0.5; // Player 轉場完成後的等待時間
    public static final int BOSS_INTRO_CYCLES = 2;
    public static final double BOSS_BUBBLE_INTERVAL_SECONDS = 0.2; // Boss 展翅時每隔幾秒向視窗四個角落各發射一顆泡泡
    public static final double BOSS_BUBBLE_SPEED = 520;
    public static final double BOSS_INVULNERABILITY_SECONDS = 2; // 有效命中後的透明無敵時間
    public static final double BOSS_RELOCATE_DELAY_SECONDS = 0.5; // Boss 受傷先透明再換平台的視覺停頓
    public static final double BOSS_DIVE_INTERVAL_SECONDS = 10; // 每巡邏 10 秒觸發三段俯衝
    public static final double BOSS_DIVE_SPEED = 900; // Boss 俯衝時每秒移動的像素

    private Constants() {
    }
}
