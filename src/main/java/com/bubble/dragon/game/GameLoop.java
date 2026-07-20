package com.bubble.dragon.game;

import com.bubble.dragon.util.Constants;
import javafx.animation.AnimationTimer;
import java.util.function.DoubleConsumer;

public final class GameLoop extends AnimationTimer {
    private final DoubleConsumer frame;
    private long previous;

    public GameLoop(DoubleConsumer frame) { this.frame = frame; }

    @Override
    public void start() { previous = 0; super.start(); }

    @Override
    public void handle(long now) {
        if (previous == 0) { previous = now; return; }
        double dt = Math.min((now - previous) / 1_000_000_000.0, Constants.MAX_DELTA);
        previous = now;
        frame.accept(dt);
    }
}
