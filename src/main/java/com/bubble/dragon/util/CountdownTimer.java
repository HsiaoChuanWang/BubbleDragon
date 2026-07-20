package com.bubble.dragon.util;

public final class CountdownTimer {
    private final double duration;
    private double remaining;
    private boolean running;

    public CountdownTimer(double durationSeconds) {
        if (durationSeconds < 0) throw new IllegalArgumentException("duration must be non-negative");
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
