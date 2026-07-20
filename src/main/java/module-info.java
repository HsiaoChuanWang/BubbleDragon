module com.bubble.dragon {
    requires javafx.controls;
    requires javafx.media;
    requires com.fasterxml.jackson.databind;

    exports com.bubble.dragon;
    exports com.bubble.dragon.entity;
    exports com.bubble.dragon.entity.player;
    exports com.bubble.dragon.entity.enemy;
    exports com.bubble.dragon.entity.weapon;
    exports com.bubble.dragon.map;
    exports com.bubble.dragon.physics;
    exports com.bubble.dragon.util;

    opens com.bubble.dragon.map to com.fasterxml.jackson.databind;
}
