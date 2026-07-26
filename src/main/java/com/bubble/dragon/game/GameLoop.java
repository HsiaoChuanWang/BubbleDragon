package com.bubble.dragon.game;

import java.util.function.DoubleConsumer;

import com.bubble.dragon.util.Constants;

import javafx.animation.AnimationTimer;

// 持續觸發每一幀的更新，控制「什麼時候更新」
// JavaFX 的動畫計時器 AnimationTimer，delta time 每一幀計算經過的時間 
public final class GameLoop extends AnimationTimer {
    // DoubleConsumer 是 Java 內建的 Functional Interface，接受一個 double，不回傳任何東西
    private final DoubleConsumer frame; // 影格 frame 指畫面更新一次
    private long previous; // long: 目前時間（奈秒）

    public GameLoop(DoubleConsumer frame) { this.frame = frame; }

    // 呼叫 start() 後，JavaFX 會在每次畫面更新時呼叫 handle(now)
    @Override
    public void start() { previous = 0; super.start(); }

    @Override
    public void handle(long now) {
        if (previous == 0) { previous = now; return; }

        // 設置上限，避免視窗卡頓時角色一次穿過整個平台
        double dt = Math.min((now - previous) / 1_000_000_000.0, Constants.MAX_DELTA);
        previous = now;
        frame.accept(dt);
    }
}
