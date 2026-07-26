package com.bubble.dragon.ui;

import com.bubble.dragon.controller.GameController;
import com.bubble.dragon.entity.enemy.Enemy;
import com.bubble.dragon.entity.enemy.EnemyState;
import com.bubble.dragon.entity.player.Player;
import com.bubble.dragon.entity.weapon.Bubble;
import com.bubble.dragon.map.Tile;
import com.bubble.dragon.util.Constants;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public final class GameCanvas extends Canvas {
    // 畫布大小
    public GameCanvas() { 
        super(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT - Constants.HUD_HEIGHT);
     }

    public void render(GameController game) {
        // 取得 Canvas 的繪圖工具（GraphicsContext） 
        GraphicsContext g = getGraphicsContext2D();

        double w = getWidth();  
        double h = getHeight();

        // 畫面重置，覆蓋上一幀的畫面，避免角色移動產生殘影 (Ghosting)
        g.setFill(Color.web("#102447")); 
        g.fillRect(0, 0, w, h);
        g.setFill(Color.web("#17365f"));
        for (int i = 0; i < 18; i++) g.fillOval(i * 67 % (int) w, 40 + i * 83 % (int) h, 4, 4);

        // 繪製順序：背景 → 地圖 → 泡泡 → 敵人 → 玩家 → 出口。
        for (Tile tile : game.getTiles()) {
            g.setFill(Color.web("#3c7a57"));
            g.fillRoundRect(tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight(), 10, 10);
            g.setStroke(Color.web("#79c267")); g.strokeRoundRect(tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight(), 10, 10);
        }

        for (Bubble bubble : game.getBubbles()) {
            g.setFill(bubble.hasTrappedEnemy() ? Color.web("#a86df2", .42) : Color.web("#7de7ff", .30));
            g.fillOval(bubble.getX(), bubble.getY(), bubble.getWidth(), bubble.getHeight());
            g.setStroke(Color.web("#d8fbff")); g.setLineWidth(2); g.strokeOval(bubble.getX(), bubble.getY(), bubble.getWidth(), bubble.getHeight());
        }

        for (Enemy enemy : game.getEnemies()) {
            if (enemy.getState() == EnemyState.DEFEATED || enemy.getState() == EnemyState.TRAPPED) continue;
            g.setFill(Color.web("#ff6b6b")); g.fillRoundRect(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight(), 14, 14);
            drawEyes(g, enemy.getX(), enemy.getY(), enemy.getWidth());
        }

        Player p = game.getPlayer();
        g.setGlobalAlpha(p.isInvulnerable() ? .48 : 1);
        g.setFill(Color.web("#55dc70")); g.fillRoundRect(p.getX(), p.getY(), p.getWidth(), p.getHeight(), 18, 18);
        drawEyes(g, p.getX(), p.getY(), p.getWidth()); g.setGlobalAlpha(1);

        if (game.isDoorVisible()) {
            g.setFill(Color.web("#ffd166"));
            g.fillRoundRect(
                    game.getDoorX(),
                    game.getDoorY(),
                    Constants.DOOR_WIDTH,
                    Constants.DOOR_HEIGHT,
                    16,
                    16
            );
            g.setFill(Color.web("#604b2d")); g.fillOval(game.getDoorX() + 35, game.getDoorY() + 36, 6, 6);
            g.setFill(Color.WHITE); g.setFont(Font.font(18)); g.fillText("出口", game.getDoorX() + 3, game.getDoorY() - 8);
        }
    }

    private void drawEyes(GraphicsContext g, double x, double y, double width) {
        g.setFill(Color.WHITE); g.fillOval(x + 9, y + 10, 8, 11); g.fillOval(x + width - 17, y + 10, 8, 11);
        g.setFill(Color.web("#17223b")); g.fillOval(x + 12, y + 14, 4, 5); g.fillOval(x + width - 14, y + 14, 4, 5);
    }
}
