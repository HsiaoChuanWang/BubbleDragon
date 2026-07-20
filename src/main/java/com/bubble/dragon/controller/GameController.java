package com.bubble.dragon.controller;

import com.bubble.dragon.entity.GameObject;
import com.bubble.dragon.entity.enemy.Enemy;
import com.bubble.dragon.entity.enemy.EnemyState;
import com.bubble.dragon.entity.player.Player;
import com.bubble.dragon.entity.player.PlayerState;
import com.bubble.dragon.entity.weapon.Bubble;
import com.bubble.dragon.map.GameMap;
import com.bubble.dragon.map.LevelLoader;
import com.bubble.dragon.map.Tile;
import com.bubble.dragon.physics.OverlapChecker;
import com.bubble.dragon.util.Constants;
import javafx.scene.input.KeyCode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class GameController {
    private final Player player;
    private final List<Tile> tiles = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bubble> bubbles = new ArrayList<>();
    private final Set<KeyCode> keys = new HashSet<>();
    private final Consumer<Boolean> resultHandler;
    private double shootCooldown;
    private boolean jumpHeld;
    private boolean finished;
    private boolean doorVisible;
    private final double doorX = 850, doorY = 430;

    public GameController(Consumer<Boolean> resultHandler) {
        this.resultHandler = resultHandler;
        GameMap map = new LevelLoader().load("/maps/level1.json");
        player = new Player(map.playerX, map.playerY);
        map.tiles.forEach(t -> tiles.add(new Tile(t.x, t.y, t.width, t.height, t.solid)));
        map.enemies.forEach(e -> enemies.add(new Enemy(e.x, e.y, e.direction)));
    }

    public void press(KeyCode key) { keys.add(key); }
    public void release(KeyCode key) { keys.remove(key); }

    public void update(double dt) {
        if (finished) return;
        shootCooldown = Math.max(0, shootCooldown - dt);
        player.updateInvulnerability(dt);
        updatePlayer(dt); updateEnemies(dt); updateBubbles(dt); checkContacts();
        bubbles.removeIf(b -> !b.isActive());
        doorVisible = enemies.stream().allMatch(e -> e.getState() == EnemyState.DEFEATED);
        if (player.getHp() <= 0) finish(false);
        if (doorVisible && OverlapChecker.overlaps(player.getX(), player.getY(), player.getWidth(), player.getHeight(), doorX, doorY, 48, 70)) finish(true);
    }

    private void updatePlayer(double dt) {
        double direction = (keys.contains(KeyCode.RIGHT) ? 1 : 0) - (keys.contains(KeyCode.LEFT) ? 1 : 0);
        player.setVelocityX(direction * Constants.PLAYER_SPEED);
        if (direction != 0) player.setFacingRight(direction > 0);
        boolean jump = keys.contains(KeyCode.SPACE);
        if (jump && !jumpHeld && player.isOnGround()) { player.setVelocityY(-Constants.JUMP_SPEED); player.setOnGround(false); }
        jumpHeld = jump;
        if (keys.contains(KeyCode.Z) && shootCooldown == 0) shoot();
        player.setVelocityY(player.getVelocityY() + Constants.GRAVITY * dt);
        moveWithTiles(player, dt);
        player.setState(player.getHp() == 0 ? PlayerState.DEAD : !player.isOnGround() ? PlayerState.JUMPING : direction == 0 ? PlayerState.IDLE : PlayerState.MOVING);
    }

    private void updateEnemies(double dt) {
        for (Enemy enemy : enemies) {
            if (enemy.getState() == EnemyState.DEFEATED) continue;
            if (enemy.getState() == EnemyState.TRAPPED) {
                enemy.getTrapTimer().update(dt);
                if (enemy.getTrapTimer().isFinished()) {
                    bubbles.stream().filter(b -> b.getTrappedEnemy() == enemy).forEach(Bubble::deactivate);
                    enemy.escape();
                }
                continue;
            }
            enemy.setVelocityX(enemy.getDirection() * Constants.ENEMY_SPEED);
            enemy.setVelocityY(enemy.getVelocityY() + Constants.GRAVITY * dt);
            double before = enemy.getX(); moveWithTiles(enemy, dt);
            if (Math.abs(enemy.getX() - before) < .1) enemy.reverse();
        }
    }

    private void updateBubbles(double dt) {
        for (Bubble bubble : bubbles) {
            bubble.updateAge(dt);
            if (bubble.hasTrappedEnemy()) {
                Enemy e = bubble.getTrappedEnemy();
                bubble.setY(bubble.getY() + bubble.getVelocityY() * dt);
                e.setX(bubble.getX() - 5); e.setY(bubble.getY() - 5);
            } else {
                bubble.setX(bubble.getX() + bubble.getVelocityX() * dt);
                bubble.setY(bubble.getY() - 12 * dt);
                for (Enemy e : enemies) if (e.getState() == EnemyState.MOVING && OverlapChecker.overlaps(bubble, e)) { e.trap(); bubble.trap(e); break; }
            }
            if (bubble.getAge() > 5 || bubble.getX() < -40 || bubble.getX() > Constants.WIDTH + 40 || bubble.getY() < -50) {
                if (bubble.hasTrappedEnemy() && bubble.getTrappedEnemy().getState() == EnemyState.TRAPPED) bubble.getTrappedEnemy().escape();
                bubble.deactivate();
            }
        }
    }

    private void checkContacts() {
        for (Enemy enemy : enemies) {
            if (enemy.getState() == EnemyState.MOVING && OverlapChecker.overlaps(player, enemy)) {
                player.damage(); player.setVelocityY(-360); player.setX(Math.max(5, player.getX() + (player.getX() < enemy.getX() ? -35 : 35)));
            }
        }
        for (Bubble bubble : bubbles) if (bubble.isActive() && bubble.hasTrappedEnemy() && OverlapChecker.overlaps(player, bubble)) {
            bubble.getTrappedEnemy().defeat(); bubble.deactivate();
        }
    }

    private void shoot() {
        double vx = player.isFacingRight() ? Constants.BUBBLE_SPEED : -Constants.BUBBLE_SPEED;
        double x = player.isFacingRight() ? player.getRight() : player.getX() - 30;
        bubbles.add(new Bubble(x, player.getY() + 10, vx)); shootCooldown = .32;
    }

    private void moveWithTiles(GameObject object, double dt) {
        object.setX(object.getX() + object.getVelocityX() * dt);
        for (Tile tile : tiles) if (tile.isSolid() && OverlapChecker.overlaps(object, tile)) {
            if (object.getVelocityX() > 0) object.setX(tile.getX() - object.getWidth()); else if (object.getVelocityX() < 0) object.setX(tile.getRight());
            object.setVelocityX(0);
        }
        object.setX(Math.max(0, Math.min(Constants.WIDTH - object.getWidth(), object.getX())));
        object.setY(object.getY() + object.getVelocityY() * dt);
        boolean grounded = false;
        for (Tile tile : tiles) if (tile.isSolid() && OverlapChecker.overlaps(object, tile)) {
            if (object.getVelocityY() > 0) { object.setY(tile.getY() - object.getHeight()); grounded = true; }
            else if (object.getVelocityY() < 0) object.setY(tile.getBottom());
            object.setVelocityY(0);
        }
        if (object instanceof Player p) p.setOnGround(grounded);
        if (object.getY() > Constants.HEIGHT) { object.setY(100); object.setVelocityY(0); if (object instanceof Player p) p.damage(); }
    }

    private void finish(boolean victory) { if (!finished) { finished = true; resultHandler.accept(victory); } }
    public Player getPlayer() { return player; }
    public List<Tile> getTiles() { return tiles; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Bubble> getBubbles() { return bubbles; }
    public int getActiveEnemyCount() { return (int) enemies.stream().filter(e -> e.getState() != EnemyState.DEFEATED).count(); }
    public boolean isDoorVisible() { return doorVisible; }
    public double getDoorX() { return doorX; }
    public double getDoorY() { return doorY; }
}
