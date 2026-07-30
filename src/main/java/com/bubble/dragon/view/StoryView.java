package com.bubble.dragon.view;

import com.bubble.dragon.BubbleDragonApp;
import com.bubble.dragon.util.Constants;

import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Affine;
import javafx.util.Duration;

// 開始遊戲前的漫畫故事畫面
// 以漫畫黑框邊線的兩端定位，計算出寬度後，等比例縮放整格漫畫
public final class StoryView {
    private static final ComicPlacement COMIC_01 = new ComicPlacement(
            "/images/comic01.png",
            new Point2D(115, 139),
            new Point2D(811, 139),
            new Point2D(22, 118),
            new Point2D(261, 118));
    private static final ComicPlacement COMIC_02 = new ComicPlacement(
            "/images/comic02.png",
            new Point2D(37, 79),
            new Point2D(1_023, 79),
            new Point2D(272, 118),
            new Point2D(524, 118));
    private static final ComicPlacement COMIC_03 = new ComicPlacement(
            "/images/comic03.png",
            new Point2D(0, 1_024),
            new Point2D(1_536, 1_024),
            new Point2D(290, 601),
            new Point2D(800, 601));
    private static final ComicPlacement COMIC_04 = new ComicPlacement(
            "/images/comic04.png",
            new Point2D(46, 8),
            new Point2D(1_016, 8),
            new Point2D(535, 118),
            new Point2D(939.54, 118));

    private final BubbleDragonApp app;
    private SequentialTransition storyAnimation;

    public StoryView(BubbleDragonApp app) {
        this.app = app;
    }

    public Scene createScene() {
        Pane root = new Pane();
        root.setPrefSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        root.getStyleClass().add("story-screen");

        ImageView comic1 = createPlacedComic(COMIC_01);
        ImageView comic2 = createPlacedComic(COMIC_02);
        ImageView comic3 = createPlacedComic(COMIC_03);
        ImageView comic4 = createPlacedComic(COMIC_04);

        // JavaFX 後加入的節點會顯示在上方：comic04 最底、comic02 有翅膀凸出要最上
        Group comics = new Group(comic4, comic1, comic3, comic2);
        root.getChildren().add(comics);

        // 讓 comic01 的右緣只在視窗左側外 12px，進入 StoryView 後會立刻出現。
        double comic1OffscreenX = comic1.getBoundsInParent().getMaxX() + 12;
        double offscreenX = Constants.WINDOW_WIDTH + 550;
        double offscreenY = Constants.WINDOW_HEIGHT + 620;
        comic1.setTranslateX(-comic1OffscreenX);
        comic2.setTranslateY(-offscreenY);
        comic3.setTranslateY(offscreenY);
        comic4.setTranslateX(offscreenX);

        storyAnimation = new SequentialTransition(
                slide(comic1, comic1OffscreenX, true),
                slide(comic2, offscreenY, false),
                slide(comic3, -offscreenY, false),
                slide(comic4, -offscreenX, true),
                new PauseTransition(Duration.seconds(Constants.COMIC_FINAL_HOLD_SECONDS)));
        storyAnimation.setOnFinished(event -> app.startGame());

        return new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }

    public void play() {
        storyAnimation.playFromStart();
    }

    public void stop() {
        if (storyAnimation != null) {
            storyAnimation.stop();
        }
    }

    private ImageView createPlacedComic(ComicPlacement placement) {
        Image image = new Image(getClass().getResource(placement.resourcePath()).toExternalForm());
        ImageView comic = new ImageView(image);
        applyEdgeTransform(comic, placement);
        return comic;
    }

    // 所有圖片共用：左右兩點決定單一縮放倍率，左點負責最終定位。
    private void applyEdgeTransform(ImageView comic, ComicPlacement placement) {
        applyEdgeTransform(
                comic,
                placement.sourceLeft(),
                placement.sourceRight(),
                placement.targetLeft(),
                placement.targetRight());
    }

    private void applyEdgeTransform(
            ImageView comic,
            Point2D sourceStart,
            Point2D sourceEnd,
            Point2D targetStart,
            Point2D targetEnd) {
        double sourceX = sourceEnd.getX() - sourceStart.getX();
        double sourceY = sourceEnd.getY() - sourceStart.getY();
        double targetX = targetEnd.getX() - targetStart.getX();
        double targetY = targetEnd.getY() - targetStart.getY();
        double sourceLengthSquared = sourceX * sourceX + sourceY * sourceY;

        // a 代表「縮放倍率 × cos(旋轉角度)」，負責同方向的縮放分量
        double a = (targetX * sourceX + targetY * sourceY) / sourceLengthSquared;

        // b 代表「縮放倍率 × sin(旋轉角度)」，負責旋轉所需的垂直分量
        double b = (targetY * sourceX - targetX * sourceY) / sourceLengthSquared;

        // 算出 X 軸平移量，讓轉換後的原圖左錨點落在目標左錨點的 X 座標
        double translateX = targetStart.getX() - a * sourceStart.getX() + b * sourceStart.getY();

        // 算出 Y 軸平移量，讓轉換後的原圖左錨點落在目標左錨點的 Y 座標
        double translateY = targetStart.getY() - b * sourceStart.getX() - a * sourceStart.getY();

        /*
         * 建立矩陣 [a -b translateX; b a translateY]，同時完成等比例縮放、旋轉和平移
         * - a：主要負責等比例縮放
         * - b：負責旋轉，此次沒有旋轉，故為 0
         * - translateX：左右移動
         * - translateY：上下移動
         * Affine transform = new Affine(a, -b, translateX, b, a, translateY);
         * 因為 X、Y 共用相同的 a 和 b，所以圖片不會被壓扁或拉長
         * 清除圖片原有的座標轉換，並套用剛建立的唯一一組轉換。
         */
        Affine transform = new Affine(a, -b, translateX, b, a, translateY);

        // 清除圖片原有的座標轉換，並套用剛建立的唯一一組轉換。
        comic.getTransforms().setAll(transform);
    }

    private TranslateTransition slide(ImageView comic, double distance, boolean horizontal) {
        double seconds = Math.abs(distance) / Constants.COMIC_SLIDE_SPEED;
        TranslateTransition transition = new TranslateTransition(Duration.seconds(seconds), comic);
        if (horizontal) {
            transition.setByX(distance);
        } else {
            transition.setByY(distance);
        }

        // 使用線性插值，讓圖片從開始到結束都以固定速度移動，不會加速或減速
        transition.setInterpolator(Interpolator.LINEAR);
        return transition;
    }

    private record ComicPlacement(
            String resourcePath,
            Point2D sourceLeft,
            Point2D sourceRight,
            Point2D targetLeft,
            Point2D targetRight) {
    }

}
