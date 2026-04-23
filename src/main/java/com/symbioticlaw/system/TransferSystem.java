package com.symbioticlaw.system;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * 转账系统 - 处理P2P转账
 * 
 * Chapter 5: P2P Transfer System
 * - 25% 交易税
 * - 奴隶额外40%扣押
 */
public class TransferSystem {
    
    // 转账税率
    private static final double TRANSFER_TAX_RATE = 0.25;
    // 奴隶扣押率
    private static final double SLAVE_GARNISH_RATE = 0.40;
    
    /**
     * 处理转账
     * @param sender 发送者
     * @param targetName 目标玩家名称
     * @param amount 转账金额
     */
    public static void handleTransfer(ServerPlayer sender, String targetName, double amount) {
        if (amount <= 0) {
            sender.sendSystemMessage(Component.literal("§c[系统] 转账金额必须大于0。"));
            return;
        }
        
        // 检查发送者余额
        sender.getCapability(PlayerDataCapability.INSTANCE).ifPresent(senderData -> {
            if (senderData.getBalance() < amount) {
                sender.sendSystemMessage(Component.literal("§c[系统] 余额不足。需要 $" + String.format("%.2f", amount) + 
                    "，您只有 $" + String.format("%.2f", senderData.getBalance())));
                return;
            }
            
            // 查找目标玩家
            ServerPlayer target = findPlayer(sender.getServer(), targetName);
            if (target == null) {
                sender.sendSystemMessage(Component.literal("§c[系统] 找不到玩家: " + targetName));
                return;
            }
            
            if (target.getUUID().equals(sender.getUUID())) {
                sender.sendSystemMessage(Component.literal("§c[系统] 不能给自己转账。"));
                return;
            }
            
            // 执行转账
            performTransfer(sender, target, amount, senderData);
        });
    }
    
    /**
     * 执行转账逻辑
     */
    private static void performTransfer(ServerPlayer sender, ServerPlayer target, double amount, IPlayerData senderData) {
        target.getCapability(PlayerDataCapability.INSTANCE).ifPresent(targetData -> {
            // 计算税金
            double tax = amount * TRANSFER_TAX_RATE;
            double amountAfterTax = amount - tax;
            
            // 检查目标是否为奴隶
            double finalAmount = amountAfterTax;
            double slaveGarnish = 0.0;
            String garnishMessage = "";
            
            if (targetData.getClassTier() == -1) { // 奴隶
                slaveGarnish = amountAfterTax * SLAVE_GARNISH_RATE;
                finalAmount = amountAfterTax - slaveGarnish;
                garnishMessage = " (奴隶扣押 $" + String.format("%.2f", slaveGarnish) + ")";
            }
            
            // 扣除发送者金额
            senderData.setBalance(senderData.getBalance() - amount);
            
            // 给目标实际到账金额
            targetData.setBalance(targetData.getBalance() + finalAmount);
            
            // 发送消息给双方
            // 发送者消息
            sender.sendSystemMessage(Component.literal("§a═══════════════════════════════════"));
            sender.sendSystemMessage(Component.literal("§a[✓ 转账成功]"));
            sender.sendSystemMessage(Component.literal("§7转给: §f" + target.getGameProfile().getName()));
            sender.sendSystemMessage(Component.literal("§7转出: §c$" + String.format("%.2f", amount)));
            sender.sendSystemMessage(Component.literal("§7交易税: §e$" + String.format("%.2f", tax) + " (25%)"));
            if (slaveGarnish > 0) {
                sender.sendSystemMessage(Component.literal("§7对方为奴隶，额外扣押: §c$" + String.format("%.2f", slaveGarnish)));
            }
            sender.sendSystemMessage(Component.literal("§7对方实际收到: §a$" + String.format("%.2f", finalAmount)));
            sender.sendSystemMessage(Component.literal("§a═══════════════════════════════════"));
            
            // 接收者消息
            target.sendSystemMessage(Component.literal("§a═══════════════════════════════════"));
            target.sendSystemMessage(Component.literal("§a[✓ 收到转账]"));
            target.sendSystemMessage(Component.literal("§7来自: §f" + sender.getGameProfile().getName()));
            target.sendSystemMessage(Component.literal("§7原金额: §7$" + String.format("%.2f", amount)));
            target.sendSystemMessage(Component.literal("§7交易税: §e$" + String.format("%.2f", tax)));
            if (slaveGarnish > 0) {
                target.sendSystemMessage(Component.literal("§7奴隶扣押: §c$" + String.format("%.2f", slaveGarnish)));
            }
            target.sendSystemMessage(Component.literal("§7实际到账: §a$" + String.format("%.2f", finalAmount)));
            target.sendSystemMessage(Component.literal("§a═══════════════════════════════════"));
        });
    }
    
    /**
     * 查找玩家（支持在线玩家）
     */
    private static ServerPlayer findPlayer(MinecraftServer server, String name) {
        if (server == null) return null;
        
        // 尝试精确匹配
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.getGameProfile().getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        
        return null;
    }
    
    /**
     * 获取转账税率
     */
    public static double getTransferTaxRate() {
        return TRANSFER_TAX_RATE;
    }
    
    /**
     * 获取奴隶扣押率
     */
    public static double getSlaveGarnishRate() {
        return SLAVE_GARNISH_RATE;
    }
    
    /**
     * 计算实际到账金额
     */
    public static double calculateActualReceived(double amount, boolean isSlave) {
        double afterTax = amount * (1 - TRANSFER_TAX_RATE);
        if (isSlave) {
            return afterTax * (1 - SLAVE_GARNISH_RATE);
        }
        return afterTax;
    }
    
    /**
     * 获取税费信息字符串
     */
    public static String getTaxInfoString(double amount, boolean isSlave) {
        double tax = amount * TRANSFER_TAX_RATE;
        double afterTax = amount - tax;
        
        if (isSlave) {
            double garnish = afterTax * SLAVE_GARNISH_RATE;
            double finalAmount = afterTax - garnish;
            return String.format("交易税: $%.2f (25%%) | 奴隶扣押: $%.2f (40%%) | 实际到账: $%.2f", 
                tax, garnish, finalAmount);
        } else {
            return String.format("交易税: $%.2f (25%%) | 实际到账: $%.2f", tax, afterTax);
        }
    }
}
