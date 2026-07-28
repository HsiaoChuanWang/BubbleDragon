package com.bubble.dragon.controller;

import com.bubble.dragon.entity.player.Player;
import com.bubble.dragon.entity.player.PlayerState;
import com.bubble.dragon.map.Tile;
import com.bubble.dragon.util.Constants;

// 負責從第一關轉場到第二關
public final class LevelTransition {
    private final Player player;
    private final LevelManager levels;
    private boolean active;
    private boolean complete;
    private double progress;
    private double playerStartX;
    private double playerStartY;

    public LevelTransition(Player player, LevelManager levels) {
        this.player = player;
        this.levels = levels;
    }

    /*
     * 開始第一關到第二關的轉場
     * 重設轉場進度、記錄玩家的起始位置，並停止玩家原本的移動
     * clearPlayerControl 由 GameController 傳入，用來清除泡泡和目前保存的按鍵狀態
     */
    public void start(Runnable clearPlayerControl) {
        active = true;
        progress = 0;

        // 保存玩家轉場前的位置，之後由 update() 從這個位置逐漸移動到第二關
        playerStartX = player.getX();
        playerStartY = player.getY();

        // 停止玩家原本的移動，並切換成轉場時使用的跳躍狀態與面向
        player.setVelocityX(0);
        player.setVelocityY(0);
        player.setOnGround(false);
        player.setState(PlayerState.JUMPING);
        player.setFacingRight(true);

        // 通知 GameController 清除轉場期間不應保留的遊戲操作
        clearPlayerControl.run();
    }

    // 從第一關的原始位置，平滑移動到第二關右側的落腳位置
    public void update(double dt) {
        // 尚未開始轉場或轉場已完成時，不需要繼續更新
        if (!active || complete)
            return;

        // 將經過時間換算成 0 ~ 1 的轉場進度，並限制最大值為 1
        progress = Math.min(1, progress + dt / Constants.LEVEL_TRANSITION_SECONDS);

        // smoothstep 數學公式：讓玩家起步與停止較平滑，不會突然加速或停止
        double movementProgress = progress * progress * (3 - 2 * progress);

        double targetX = Constants.WINDOW_WIDTH
                - player.getWidth()
                - Constants.LEVEL_TRANSITION_PLAYER_MARGIN;

        double targetY = findLandingY(targetX);

        // 依照轉場進度，在起始位置與目標位置之間計算玩家目前的位置
        player.setX(playerStartX + (targetX - playerStartX) * movementProgress);
        player.setY(playerStartY + (targetY - playerStartY) * movementProgress);
        player.setState(PlayerState.JUMPING);

        // 進度到達 1，代表玩家已到達目標位置，完成關卡切換。
        if (progress >= 1)
            complete();
    }

    private double findLandingY(double targetX) {
        double landingY = Double.NEGATIVE_INFINITY; // 設為 Java double 能表示的「負無限大」，表示目前還沒有找到任何有效的落地 Y
        for (Tile tile : levels.getLevelTwoTiles()) {
            // 判斷玩家放在 targetX 時，水平範圍是否會與地磚重疊
            boolean overlaps = targetX < tile.getRight()
                    && targetX + player.getWidth() > tile.getX();

            // tile.getY() 減去玩家高度，可讓玩家底部剛好貼齊地磚頂部
            if (tile.isSolid() && overlaps)
                landingY = Math.max(landingY, tile.getY() - player.getHeight());
        }

        // 找不到地磚時使用預設落點，避免回傳無效座標
        return landingY == Double.NEGATIVE_INFINITY
                ? Constants.WINDOW_HEIGHT - Constants.HUD_HEIGHT - player.getHeight()
                : landingY;
    }

    // 完成轉場並正式切換到第二關
    private void complete() {
        levels.switchToLevelTwo();
        active = false;
        complete = true;
        player.setVelocityX(0);
        player.setVelocityY(0);
        player.setOnGround(true);
        player.setState(PlayerState.IDLE);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isComplete() {
        return complete;
    }

    public double getProgress() {
        return progress;
    }
}
