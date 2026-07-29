package com.bubble.dragon.entity.boss;

public enum BossState {
    INACTIVE, // 第二關轉場與登場延遲尚未結束，不更新也不繪製
    SCREAM, // 登場展翅叫聲階段，持續朝四角發射泡泡
    FOLD_WINGS, // 登場收翅階段，只等待、不發射
    PATROL, // 正式戰鬥：巡邏、追逐並接受玩家泡泡傷害
    DIVING_ATTACK, // 週期性三段俯衝，忽略平台碰撞
    DEFEATED // HP 歸零，停止更新與繪製
}
