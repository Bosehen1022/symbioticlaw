package com.symbioticlaw.system;

import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.JobType;
import com.symbioticlaw.professions.Profession;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * 职业变更系统
 * 处理转职/离职的所有逻辑，包括违约金计算和经验清零
 * 
 * 规格书要求：
 * - 总违约金 = $3,000 (基础惩罚) + (当前职业等级 × $300)
 * - 转职后原职业经验和等级清零
 * - 需要二次确认
 */
public class ProfessionChangeSystem {
    
    private static final double BASE_BREACH_FEE = 3000.0;
    private static final double LEVEL_MULTIPLIER = 300.0;
    
    /**
     * 计算转职违约金
     * 公式：$3,000 + (当前等级 × $300)
     */
    public static double calculateBreachmentFee(int currentLevel) {
        return BASE_BREACH_FEE + (currentLevel * LEVEL_MULTIPLIER);
    }
    
    /**
     * 检查玩家是否可以转职
     */
    public static boolean canChangeProfession(ServerPlayer player, Profession newProfession) {
        var playerData = player.getCapability(PlayerDataCapability.INSTANCE).orElse(null);
        if (playerData == null) return false;
        
        Profession currentProfession = playerData.getProfession();
        
        // 检查是否已经是该职业
        if (currentProfession == newProfession) {
            player.sendSystemMessage(Component.literal("§c你已经是这个职业了。"));
            return false;
        }
        
        // 检查是否是无业游民（无业游民转职不需要费用）
        if (currentProfession == Profession.UNEMPLOYED) {
            return true;
        }
        
        // 检查阶级 - 遗民不能转职
        if (playerData.getClassTier() == 0) {
            player.sendSystemMessage(Component.literal("§c[🚫] 遗民无法申请转职。请先脱贫成为限制公民。"));
            return false;
        }
        
        // 检查是否持有低保户标签
        if (playerData.isWelfareRecipient()) {
            player.sendSystemMessage(Component.literal("§c[🚫] 领取低保期间禁止转职。请先支付 $500 移除低保标签。"));
            return false;
        }
        
        // 检查余额是否足够支付违约金
        int currentLevel = playerData.getProfessionLevel(currentProfession);
        double fee = calculateBreachmentFee(currentLevel);
        
        if (playerData.getBalance() < fee) {
            player.sendSystemMessage(Component.literal(
                String.format("§c[🚫 驳回] 您的资产 ($%.2f) 甚至不足以支付天价违约金 ($%.2f)。回到你的流水线上去，系统不需要穷光蛋的朝三暮四。", 
                    playerData.getBalance(), fee)));
            return false;
        }
        
        return true;
    }
    
    /**
     * 执行职业变更
     * 扣除违约金，清零原职业数据，设置新职业
     */
    public static boolean executeProfessionChange(ServerPlayer player, Profession newProfession) {
        var playerData = player.getCapability(PlayerDataCapability.INSTANCE).orElse(null);
        if (playerData == null) return false;
        
        Profession oldProfession = playerData.getProfession();
        
        // 无业游民转职不需要费用和清零
        if (oldProfession != Profession.UNEMPLOYED) {
            int oldLevel = playerData.getProfessionLevel(oldProfession);
            double fee = calculateBreachmentFee(oldLevel);
            
            // 扣除违约金
            playerData.setBalance(playerData.getBalance() - fee);
            playerData.addTaxPaidThisCycle(fee); // 记录为已缴税款
            
            // 清零原职业数据
            playerData.setProfessionLevel(oldProfession, 0);
            playerData.setProfessionXp(oldProfession, 0.0);
            
            // 发送扣款通知
            player.sendSystemMessage(Component.literal(
                String.format("§c[财务] 天价违约金扣除 -$%.2f。", fee)));
            
            // 播放音效 - 信标消散声 + 雷声
            player.level().playSound(null, player.blockPosition(), 
                SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);
            player.level().playSound(null, player.blockPosition(), 
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5f, 1.0f);
            
            // 全服广播
            String broadcastMsg = String.format("§4[☠ 档案清洗] §7[系统] %s 的 [%s] 权限已被吊销，相关技能记忆已格式化。", 
                player.getGameProfile().getName(), 
                oldProfession.getDisplayName().getString());
            if (player.getServer() != null) {
                player.getServer().getPlayerList().broadcastSystemMessage(Component.literal(broadcastMsg), false);
            }
        }
        
        // 设置新职业
        playerData.setProfession(newProfession);
        playerData.setProfessionLevel(newProfession, 1);
        playerData.setProfessionXp(newProfession, 0.0);
        playerData.setLastProfessionChangeTime(player.level().getGameTime());
        
        // 发送欢迎消息
        player.sendSystemMessage(Component.literal(
            String.format("§a[系统] 档案重建完毕。欢迎加入【%s】序列，请从底层重新证明您的价值。", 
                newProfession.getDisplayName().getString())));
        
        return true;
    }
    
    /**
     * 获取转职费用详情文本
     */
    public static String getFeeBreakdown(ServerPlayer player) {
        var playerData = player.getCapability(PlayerDataCapability.INSTANCE).orElse(null);
        if (playerData == null) return "无法获取数据";
        
        Profession currentProfession = playerData.getProfession();
        if (currentProfession == Profession.UNEMPLOYED) {
            return "无业游民首次入职免费";
        }
        
        int currentLevel = playerData.getProfessionLevel(currentProfession);
        double fee = calculateBreachmentFee(currentLevel);
        
        return String.format("基础违约金: $%.0f + 等级惩罚: $%.0f (Lv.%d × $300) = 总计: $%.0f",
            BASE_BREACH_FEE, currentLevel * LEVEL_MULTIPLIER, currentLevel, fee);
    }
    
    /**
     * 显示转职契约信息
     */
    public static void displayContractInfo(ServerPlayer player, Profession targetProfession) {
        var playerData = player.getCapability(PlayerDataCapability.INSTANCE).orElse(null);
        if (playerData == null) return;
        
        Profession currentProfession = playerData.getProfession();
        
        player.sendSystemMessage(Component.literal("§e═══════════════════════════════════════"));
        player.sendSystemMessage(Component.literal("§e[至高权力核心] 身份档案更新请求..."));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
            String.format("§b您正在申请核心授权职业：【%s】", targetProfession.getDisplayName().getString())));
        player.sendSystemMessage(Component.literal("§7--------------------------------------------------"));
        
        // 显示当前状态
        if (currentProfession != Profession.UNEMPLOYED) {
            int currentLevel = playerData.getProfessionLevel(currentProfession);
            double fee = calculateBreachmentFee(currentLevel);
            player.sendSystemMessage(Component.literal(
                String.format("§c当前职业: %s (Lv.%d)", currentProfession.getDisplayName().getString(), currentLevel)));
            player.sendSystemMessage(Component.literal(
                String.format("§c转职违约金: $%.2f", fee)));
            player.sendSystemMessage(Component.literal("§c⚠ 终身契约警告 (FATAL WARNING)："));
            player.sendSystemMessage(Component.literal("§c核心厌恶不稳定的齿轮。您的社会分工一旦确立将被锁死。"));
            player.sendSystemMessage(Component.literal("§c转职后原职业等级与经验将被§4永久物理抹除(归零)§c。"));
        } else {
            player.sendSystemMessage(Component.literal("§a当前身份: 无业游民 (首次入职免费)"));
        }
        
        player.sendSystemMessage(Component.literal("§7--------------------------------------------------"));
        player.sendSystemMessage(Component.literal("§e═══════════════════════════════════════"));
    }
}
