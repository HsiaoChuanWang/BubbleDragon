package com.bubble.dragon.util;

import java.net.URL;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/** 管理遊戲中的音效與循環背景音樂。 */
public final class GameAudio {
    private final AudioClip bubbleSound = loadClip("/sounds/bubble-sound.mp3");
    private final AudioClip damageSound = loadClip("/sounds/damage-sound.mp3");
    private final AudioClip popBubbleSound = loadClip("/sounds/pop-bubble.mp3");
    private final MediaPlayer battleMusic = loadMusic("/sounds/battle-music.mp3");

    public void playBubbleSound() {
        if (bubbleSound != null) bubbleSound.play();
    }

    public void playDamageSound() {
        if (damageSound != null) damageSound.play();
    }

    public void playPopBubbleSound() {
        if (popBubbleSound != null) popBubbleSound.play();
    }

    public void startBattleMusic() {
        if (battleMusic != null) battleMusic.play();
    }

    public void stop() {
        if (bubbleSound != null) bubbleSound.stop();
        if (damageSound != null) damageSound.stop();
        if (popBubbleSound != null) popBubbleSound.stop();
        if (battleMusic != null) {
            battleMusic.stop();
            battleMusic.dispose();
        }
    }

    private AudioClip loadClip(String path) {
        URL resource = GameAudio.class.getResource(path);
        if (resource == null) {
            System.err.println("Sound resource not found: " + path);
            return null;
        }
        return new AudioClip(resource.toExternalForm());
    }

    private MediaPlayer loadMusic(String path) {
        URL resource = GameAudio.class.getResource(path);
        if (resource == null) {
            System.err.println("Music resource not found: " + path);
            return null;
        }
        MediaPlayer player = new MediaPlayer(new Media(resource.toExternalForm()));
        player.setCycleCount(MediaPlayer.INDEFINITE);
        return player;
    }
}
