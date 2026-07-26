package com.bubble.dragon.util;

// 目前僅用於敵人被泡泡困住後的三秒倒數
public final class CountdownTimer {
    private final double duration;
    private double remaining;
    private boolean running;

    public CountdownTimer(double durationSeconds) {
        duration = durationSeconds;
        remaining = durationSeconds;
    }

    public void start() { remaining = duration; running = true; }
    public void stop() { running = false; }
    public void reset() { remaining = duration; running = false; }

    public void update(double deltaSeconds) {
        if (!running || deltaSeconds <= 0) return;
        remaining = Math.max(0, remaining - deltaSeconds);
        if (remaining == 0) running = false;
    }

    public boolean isFinished() { return remaining <= 0; }
    public boolean isRunning() { return running; }
    public double getRemainingSeconds() { return remaining; }
}
