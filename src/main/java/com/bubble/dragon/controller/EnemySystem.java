package com.bubble.dragon.controller;

import java.util.List;

import com.bubble.dragon.entity.GameObject;
import com.bubble.dragon.entity.enemy.Enemy;
import com.bubble.dragon.entity.enemy.EnemyState;
import com.bubble.dragon.entity.player.Player;
import com.bubble.dragon.physics.OverlapChecker;
import com.bubble.dragon.util.Constants;

// 敵人平時在平台巡邏，玩家靠近時追擊，但不會走出平台邊緣
public final class EnemySystem {
    private final List<Enemy> enemies;
    private final CollisionSystem collisions;
    private final BubbleSystem bubbles;

    public EnemySystem(List<Enemy> enemies, CollisionSystem collisions, BubbleSystem bubbles) {
        this.enemies = enemies;
        this.collisions = collisions;
        this.bubbles = bubbles;
    }

    public void update(double dt, Player player) {
        for (Enemy enemy : enemies) {
            if (enemy.getState() == EnemyState.DEFEATED)
                continue;
            if (enemy.getState() == EnemyState.TRAPPED) {
                updateTrapped(enemy, dt);
                continue;
            }

            updateAwareness(enemy, player); // 更新敵人是否注意到玩家
            updateActionState(enemy, dt); // 更新敵人行動狀態，決定是巡邏、跳躍、飛行還是停止

            if (enemy.getState() == EnemyState.STOP) {
                enemy.setVelocityX(0);
                enemy.setVelocityY(0);
            } else {
                moveOnPlatform(enemy, dt);
            }
        }
    }

    private void updateAwareness(Enemy enemy, Player player) {
        double horizontalDistance = Math.abs(centerX(player) - centerX(enemy));
        double verticalDistance = Math.abs(centerY(player) - centerY(enemy));

        if (!enemy.isChasing()
                && horizontalDistance <= Constants.ENEMY_NOTICE_DISTANCE
                && verticalDistance <= Constants.ENEMY_NOTICE_HEIGHT) {
            enemy.setChasing(true);
            // 玩家進入同一水平視線時，敵人面向玩家並開始加速追擊
            enemy.face(centerX(player));
        } else if (enemy.isChasing()
                && (horizontalDistance > Constants.ENEMY_FORGET_DISTANCE
                        || verticalDistance > Constants.ENEMY_FORGET_HEIGHT)) {
            enemy.setChasing(false);
        }
    }

    private void updateActionState(Enemy enemy, double dt) {
        enemy.updateStateTime(dt);
        if (enemy.getState() == EnemyState.JUMP
                && enemy.getStateElapsed() >= Constants.ENEMY_JUMP_SECONDS) {
            enemy.startFly();
        } else if (enemy.getState() == EnemyState.FLY
                && enemy.getStateElapsed() >= Constants.ENEMY_FLY_SECONDS) {
            enemy.startStop();
        } else if (enemy.getState() == EnemyState.STOP
                && enemy.getStateElapsed() >= Constants.ENEMY_STOP_SECONDS) {
            enemy.startJump();
        }
    }

    // 檢查敵人是否站在平台上，若站在平台上則依照巡邏或追擊的狀態移動
    private void moveOnPlatform(Enemy enemy, double dt) {
        double speed = enemy.isChasing()
                ? Constants.ENEMY_CHASE_SPEED
                : Constants.ENEMY_PATROL_SPEED;
        enemy.setVelocityX(enemy.getDirection() * speed);
        enemy.setVelocityY(enemy.getVelocityY() + Constants.GRAVITY * dt);

        // 敵人目前的左右腳下方，是否仍有實心磚塊支撐
        boolean currentlySupported = collisions.hasSupportAt(enemy, enemy.getX());
        double nextX = enemy.getX() + enemy.getVelocityX() * dt;

        // 巡邏時走到平台末端會轉身；追擊時則允許衝出平台繼續追玩家
        if (!enemy.isChasing()
                && currentlySupported
                && !collisions.hasSupportAt(enemy, nextX)) { // 檢查下一個 x 發現可能會掉落
            enemy.reverse(); // 轉身
            enemy.setVelocityX(enemy.getDirection() * speed); // 立即改用轉身後的方向移動
        }

        double intendedVelocityX = enemy.getVelocityX();
        collisions.moveWithTiles(enemy, dt);

        // 撞到實心地磚時，CollisionSystem 會把水平速度歸零
        boolean hitTileWall = intendedVelocityX != 0 && enemy.getVelocityX() == 0;

        // CollisionSystem 會限制物件留在畫面內，因此另外檢查是否正朝左右邊界外移動
        boolean hitScreenEdge = (enemy.getX() <= 0 && intendedVelocityX < 0)
                || (enemy.getRight() >= Constants.WINDOW_WIDTH && intendedVelocityX > 0);

        // 只有巡邏時會因牆壁或畫面邊界轉身；追擊時維持面向玩家的方向
        if (!enemy.isChasing()
                && (hitTileWall || hitScreenEdge))
            enemy.reverse();
    }

    private void updateTrapped(Enemy enemy, double dt) {
        enemy.getTrapTimer().update(dt);
        if (enemy.getTrapTimer().isFinished()) {
            bubbles.deactivateBubblesContaining(enemy);
            enemy.escape();
        }
    }

    private double centerX(GameObject object) {
        return object.getX() + object.getWidth() / 2;
    }

    private double centerY(GameObject object) {
        return object.getY() + object.getHeight() / 2;
    }

    // 檢查玩家是否碰到具有傷害性的敵人，若碰到就讓玩家受傷並產生擊退效果
    public void checkPlayerContact(Player player) {
        for (Enemy enemy : enemies) {
            if (enemy.getState() == EnemyState.TRAPPED
                    || enemy.getState() == EnemyState.DEFEATED
                    || !OverlapChecker.overlaps(player, enemy))
                continue;

            player.damage();
            player.setVelocityY(-Constants.PLAYER_DAMAGE_BOUNCE_SPEED); // 向上彈
            double direction = player.getX() < enemy.getX() ? -1 : 1;
            double hitX = player.getX() + direction * Constants.PLAYER_HIT_KNOCKBACK_DISTANCE; // 向反方向退
            double minX = Constants.PLAYER_HIT_SCREEN_MARGIN;
            double maxX = Constants.WINDOW_WIDTH
                    - player.getWidth()
                    - Constants.PLAYER_HIT_SCREEN_MARGIN;
            player.setX(Math.max(minX, Math.min(maxX, hitX)));
        }
    }
}
