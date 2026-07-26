package com.bubble.dragon;

public final class Main {
    // 將建構子設為 private，禁止任何人在程式碼的其他地方使用 new Main() 來建立這個類別的實體物件
    private Main() {}

    public static void main(String[] args) {
        BubbleDragonApp.launchApp(args);
    }
}
