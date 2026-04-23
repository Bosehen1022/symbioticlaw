package com.symbioticlaw.system;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.JobType;
import com.symbioticlaw.network.ClientBoundCareerPenaltyPacket;
import com.symbioticlaw.network.NetworkHandler;
import com.symbioticlaw.network.ServerBoundCareerActionPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 职业系统 - 处理职业变更、入职、辞职等操作
 * 
 * Chapter 5: Career Binding & Class Solidification
 */
public class CareerSystem {
    
    // 违约金基础值
    private static final double PENALTY_BASE = 3000.0;
    // 每级额外违约金
    private static final double PENALTY_PER_LEVEL = 300.0;
    
    /**
     * 计算职业变更违约金
     * 公式：$3,000 + (当前等级 × $300)
     */
    public static double calculatePenalty(int currentLevel) {
        return PENALTY_BASE + (currentLevel * PENALTY_PER_LEVEL);
    }
    
    /**
     * 处理职业操作请求
     */
    public static void handleCareerAction(ServerPlayer player, 
                                          ServerBoundCareerActionPacket.ActionType actionType,
                                          int targetProfessionId,
                                          boolean confirmed) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            switch (actionType) {
                case REQUEST_CAREER_CHANGE -> handleCareerChangeRequest(player, playerData, targetProfessionId);
                case CONFIRM_CAREER_CHANGE -> handleConfirmCareerChange(player, playerData, targetProfessionId);
                case APPLY_FOR_JOB -> handleJobApplication(player, playerData, targetProfessionId);
                case RESIGN -> handleResignation(player, playerData);
            }
        });
    }
    
    /**
     * 处理转职申请（有职业 -> 其他职业）
     */
    private static void handleCareerChangeRequest(ServerPlayer player, IPlayerData playerData, int targetProfessionId) {
        // 检查是否为遗民/奴隶 - 无法转职
        if (playerData.getClassTier() <= 0) {
            player.sendSystemMessage(Component.literal("§c[系统] 遗民和奴隶无法自主申请职业变更。"));
            return;
        }
        
        // 获取当前职业
        int currentProfessionId = playerData.getProfessionId();
        JobType currentJob = JobType.fromId(currentProfessionId);
        JobType targetJob = JobType.fromId(targetProfessionId);
        
        if (currentProfessionId == targetProfessionId) {
            player.sendSystemMessage(Component.literal("§c[系统] 您已经是该职业。"));
            return;
        }
        
        // 获取当前等级
        int currentLevel = playerData.getProfessionLevel(currentProfessionId);
        double currentXp = playerData.getProfessionXp(currentProfessionId);
        
        // 计算违约金
        double penalty = calculatePenalty(currentLevel);
        boolean canAfford = playerData.getBalance() >= penalty;
        
        // 发送惩罚信息给客户端
        ClientBoundCareerPenaltyPacket.CareerPenaltyInfo info = 
            new ClientBoundCareerPenaltyPacket.CareerPenaltyInfo(
                currentProfessionId,
                targetProfessionId,
                penalty,
                currentLevel,
                currentXp,
                canAfford,
                targetJob.getDisplayName()
            );
        
        NetworkHandler.sendToPlayer(player, new ClientBoundCareerPenaltyPacket(info));
        
        // 显示提示信息
        player.sendSystemMessage(Component.literal("§e═══════════════════════════════════"));
        player.sendSystemMessage(Component.literal("§e[⚠ 转职警告]"));
        player.sendSystemMessage(Component.literal("§7您当前: §f" + currentJob.getDisplayName() + " §7Lv." + currentLevel));
        player.sendSystemMessage(Component.literal("§7目标: §a" + targetJob.getDisplayName()));
        player.sendSystemMessage(Component.literal("§7违约金: §c$" + String.format("%.2f", penalty)));
        player.sendSystemMessage(Component.literal("§7将被清零: §c" + String.format("%.0f", currentXp) + " XP"));
        
        if (canAfford) {
            player.sendSystemMessage(Component.literal("§a您有足够余额支付违约金。"));
            player.sendSystemMessage(Component.literal("§e请在菜单中确认转职契约。"));
        } else {
            player.sendSystemMessage(Component.literal("§c余额不足！需要 $" + String.format("%.2f", penalty) + 
                "，您只有 $" + String.format("%.2f", playerData.getBalance())));
        }
        player.sendSystemMessage(Component.literal("§e═══════════════════════════════════"));
    }
    
    /**
     * 确认职业变更
     */
    private static void handleConfirmCareerChange(ServerPlayer player, IPlayerData playerData, int targetProfessionId) {
        int currentProfessionId = playerData.getProfessionId();
        JobType currentJob = JobType.fromId(currentProfessionId);
        JobType targetJob = JobType.fromId(targetProfessionId);
        
        int currentLevel = playerData.getProfessionLevel(currentProfessionId);
        double penalty = calculatePenalty(currentLevel);
        
        // 检查余额
        if (playerData.getBalance() < penalty) {
            player.sendSystemMessage(Component.literal("§c[系统] 余额不足，无法支付违约金。"));
            return;
        }
        
        // 扣除违约金
        playerData.setBalance(playerData.getBalance() - penalty);
        
        // 清空原职业数据
        playerData.setProfessionLevel(currentProfessionId, 0);
        playerData.setProfessionXp(currentProfessionId, 0.0);
        
        // 设置新职业
        playerData.setProfessionId(targetProfessionId);
        
        // 播放音效
        player.level().playSound(
            null,
            player.blockPosition(),
            net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE,
            net.minecraft.sounds.SoundSource.PLAYERS,
            1.0f,
            0.5f
        );
        player.level().playSound(
            null,
            player.blockPosition(),
            net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER,
            net.minecraft.sounds.SoundSource.WEATHER,
            1.0f,
            1.0f
        );
        
        // 通知玩家
        player.sendSystemMessage(Component.literal("§c[财务] 天价违约金扣除 -$" + String.format("%.2f", penalty)));
        player.sendSystemMessage(Component.literal("§4[☠ 档案清洗] 您的过往已被核心抹除。"));
        player.sendSystemMessage(Component.literal(String.format("§7[系统] %s 的 [%s] 权限已被吊销，相关技能记忆已格式化。", player.getGameProfile().getName(), currentJob.getDisplayName())));
        player.sendSystemMessage(Component.literal(String.format("§a[系统] 档案重建完毕。欢迎加入【%s】序列，请从底层重新证明您的价值。", targetJob.getDisplayName())));
        
        // 全服广播档案清洗消息
        if (player.getServer() != null) {
            Component broadcast = Component.literal(String.format(
                "§4[☠ 档案清洗] §7%s 的 [%s] 权限已被吊销，相关技能记忆已格式化。欢迎加入【%s】序列，请从底层重新证明您的价值。",
                player.getGameProfile().getName(),
                currentJob.getDisplayName(),
                targetJob.getDisplayName()
            ));
            player.getServer().getPlayerList().broadcastSystemMessage(broadcast, false);
        }
        
        // 清除客户端的惩罚信息
        NetworkHandler.sendToPlayer(player, new ClientBoundCareerPenaltyPacket((ClientBoundCareerPenaltyPacket.CareerPenaltyInfo) null));
    }
    
    /**
     * 处理入职申请（无业 -> 有职业）
     * 无业玩家（职业ID为0）申请成为某个职业
     */
    private static void handleJobApplication(ServerPlayer player, IPlayerData playerData, int targetProfessionId) {
        int currentProfessionId = playerData.getProfessionId();
        
        // 检查是否已有职业（职业ID > 0）
        if (currentProfessionId > 0) {
            player.sendSystemMessage(Component.literal("§c[系统] 您已有职业，请使用转职功能。"));
            return;
        }
        
        // 检查是否为遗民/奴隶
        if (playerData.getClassTier() <= 0) {
            player.sendSystemMessage(Component.literal("§c[系统] 遗民和奴隶无法申请职业。"));
            return;
        }
        
        JobType targetJob = JobType.fromId(targetProfessionId);
        
        // 设置职业
        playerData.setProfessionId(targetProfessionId);
        playerData.setProfessionLevel(targetProfessionId, 1); // 初始等级为1
        playerData.setProfessionXp(targetProfessionId, 0.0);
        
        player.sendSystemMessage(Component.literal("§a═══════════════════════════════════"));
        player.sendSystemMessage(Component.literal("§a[✓ 入职成功]"));
        player.sendSystemMessage(Component.literal("§7欢迎加入 §a" + targetJob.getDisplayName()));
        player.sendSystemMessage(Component.literal("§7初始等级: §aLv.1"));
        player.sendSystemMessage(Component.literal("§7请在交易终端查看您的职业信息。"));
        player.sendSystemMessage(Component.literal("§a═══════════════════════════════════"));
    }
    
    /**
     * 处理辞职（有职业 -> 无业）
     */
    private static void handleResignation(ServerPlayer player, IPlayerData playerData) {
        int currentProfessionId = playerData.getProfessionId();
        
        if (currentProfessionId <= 0) {
            player.sendSystemMessage(Component.literal("§c[系统] 您当前没有职业。"));
            return;
        }
        
        // 计算违约金
        int currentLevel = playerData.getProfessionLevel(currentProfessionId);
        double penalty = calculatePenalty(currentLevel);
        
        if (playerData.getBalance() < penalty) {
            player.sendSystemMessage(Component.literal("§c[系统] 余额不足，无法支付违约金。需要 $" + String.format("%.2f", penalty)));
            return;
        }
        
        JobType oldJob = JobType.fromId(currentProfessionId);
        
        // 扣除违约金
        playerData.setBalance(playerData.getBalance() - penalty);
        
        // 清空职业数据
        playerData.setProfessionLevel(currentProfessionId, 0);
        playerData.setProfessionXp(currentProfessionId, 0.0);
        playerData.setProfessionId(0); // 设为无业
        
        // Spec V51: Resignation Feedback
        player.sendSystemMessage(Component.literal("§c[财务] 天价违约金扣除 -$" + String.format("%.2f", penalty)));
        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.5f);
        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER, net.minecraft.sounds.SoundSource.WEATHER, 1.0f, 1.0f);

        player.sendSystemMessage(Component.literal("§4[☠ 档案清洗] 您的过往已被核心抹除。"));
        player.sendSystemMessage(Component.literal(String.format("§7[系统] %s 的 [%s] 权限已被吊销，相关技能记忆已格式化。", player.getGameProfile().getName(), oldJob.getDisplayName())));
        player.sendSystemMessage(Component.literal("§a[系统] 档案重建完毕。您现在是无业游民。"));
    }
    
    
    
    // ============ Getter methods ============    
    public static double getPenaltyBase() {
        return PENALTY_BASE;
    }
    
    public static double getPenaltyPerLevel() {
        return PENALTY_PER_LEVEL;
    }
}
