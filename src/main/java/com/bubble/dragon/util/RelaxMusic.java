package com.bubble.dragon.util;

import java.net.URL;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/** 控制非戰鬥畫面共用的循環背景音樂。 */
public final class RelaxMusic {
    private final MediaPlayer player;

    public RelaxMusic() {
        URL resource = RelaxMusic.class.getResource("/sounds/relax-music.mp3");
        if (resource == null) {
            System.err.println("Music resource not found: /sounds/relax-music.mp3");
            player = null;
            return;
        }
        player = new MediaPlayer(new Media(resource.toExternalForm()));
        player.setCycleCount(MediaPlayer.INDEFINITE);
    }

    public void play() {
        if (player != null) player.play();
    }

    public void pause() {
        if (player != null) player.pause();
    }

    public void dispose() {
        if (player != null) player.dispose();
    }
}
