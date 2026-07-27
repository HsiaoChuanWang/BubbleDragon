package com.bubble.dragon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CountdownTimerTest {

    // 驗證正常倒數流程：經過指定時間後，計時器應標記為 Finished 並停止執行
    @Test
    void finishesAfterDuration() {
        CountdownTimer timer = new CountdownTimer(3);
        timer.start();
        timer.update(2);
        assertFalse(timer.isFinished()); // 經過 2 秒，尚未完成
        timer.update(1);
        assertTrue(timer.isFinished()); // 總共經過 3 秒，應該完成
        assertFalse(timer.isRunning()); // 完成後應該停止執行
    }

    // 驗證重設功能：呼叫 reset 後，剩餘時間應恢復為初始值，並且狀態為停止執行
    @Test
    void resetRestoresDuration() {
        CountdownTimer timer = new CountdownTimer(3);
        timer.start();
        timer.update(1); // 消耗 1 秒
        timer.reset(); // 觸發重設
        assertEquals(3, timer.getRemainingSeconds()); // 確認時間恢復為 3 秒
        assertFalse(timer.isRunning()); // 確認重設後計時器已停止
    }
}