package com.symbioticlaw.system;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.network.ClientBoundSlaveListPacket;
import com.symbioticlaw.network.NetworkHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 奴隶系统 - 处理奴隶相关操作
 * 
 * Chapter 5: Slavery System
 */
public class SlaverySystem {
    
    /**
     * 发送奴隶列表给玩家
     */
    public static void sendSlaveListToPlayer(ServerPlayer master) {
        master.getCapability(PlayerDataCapability.INSTANCE).ifPresent(masterData -> {
            List<UUID> slaveUUIDs = masterData.getSlaves();
            List<ClientBoundSlaveListPacket.SlaveInfo> slaveInfos = new ArrayList<>();
            double[] totalPendingTax = {0.0};
            
            for (UUID slaveUUID : slaveUUIDs) {
                ServerPlayer slave = master.getServer().getPlayerList().getPlayer(slaveUUID);
                if (slave != null) {
                    slave.getCapability(PlayerDataCapability.INSTANCE).ifPresent(slaveData -> {
                        double pendingTax = slaveData.getPendingTaxForMaster(master.getUUID());
                        double totalExtracted = slaveData.getTotalExtractedTax(master.getUUID());
                        
                        slaveInfos.add(new ClientBoundSlaveListPacket.SlaveInfo(
                            slaveUUID,
                            slave.getGameProfile().getName(),
                            pendingTax,
                            totalExtracted
                        ));
                        
                        totalPendingTax[0] += pendingTax;
                    });
                }
            }
            
            // 发送给客户端
            NetworkHandler.sendToPlayer(master, new ClientBoundSlaveListPacket(slaveInfos, totalPendingTax[0]));
        });
    }
    
    /**
     * 释放奴隶
     */
    public static void releaseSlave(ServerPlayer master, UUID slaveUUID) {
        master.getCapability(PlayerDataCapability.INSTANCE).ifPresent(masterData -> {
            if (!masterData.getSlaves().contains(slaveUUID)) {
                master.sendSystemMessage(Component.literal("§c[系统] 该玩家不是您的奴隶。"));
                return;
            }
            
            ServerPlayer slave = master.getServer().getPlayerList().getPlayer(slaveUUID);
            if (slave == null) {
                master.sendSystemMessage(Component.literal("§c[系统] 奴隶当前不在线。"));
                return;
            }
            
            // 执行释放
            masterData.removeSlave(slaveUUID);
            
            slave.getCapability(PlayerDataCapability.INSTANCE).ifPresent(slaveData -> {
                // 将奴隶转为遗民
                slaveData.setClassTier(0);
                slaveData.setMasterUUID(null);
                
                // 通知双方
                master.sendSystemMessage(Component.literal("§a[系统] 您已释放奴隶 " + slave.getGameProfile().getName() + 
                    "，其阶级已转为遗民。"));
                slave.sendSystemMessage(Component.literal("§a[系统] 您已被释放，现在您是遗民。"));
                slave.sendSystemMessage(Component.literal("§e您现在可以领取每日低保，或支付$500信用修复费恢复为受限公民。"));
            });
            
            // 刷新列表
            sendSlaveListToPlayer(master);
        });
    }
    
    /**
     * 提取所有奴隶的税金
     */
    public static void claimAllTax(ServerPlayer master) {
        master.getCapability(PlayerDataCapability.INSTANCE).ifPresent(masterData -> {
            List<UUID> slaveUUIDs = masterData.getSlaves();
            double totalClaimed = 0.0;
            int claimedCount = 0;
            
            for (UUID slaveUUID : slaveUUIDs) {
                ServerPlayer slave = master.getServer().getPlayerList().getPlayer(slaveUUID);
                if (slave != null) {
                    slave.getCapability(PlayerDataCapability.INSTANCE).ifPresent(slaveData -> {
                        double pendingTax = slaveData.claimPendingTax(master.getUUID());
                        if (pendingTax > 0) {
                            masterData.setBalance(masterData.getBalance() + pendingTax);
                        }
                    });
                }
            }
            
            if (totalClaimed > 0) {
                master.sendSystemMessage(Component.literal("§a[系统] 已提取所有奴隶税金，共计 $" + 
                    String.format("%.2f", totalClaimed)));
            } else {
                master.sendSystemMessage(Component.literal("§e[系统] 没有待提取的税金。"));
            }
            
            // 刷新列表
            sendSlaveListToPlayer(master);
        });
    }
    
    /**
     * 检查玩家是否可以拥有奴隶
     */
    public static boolean canOwnSlaves(IPlayerData playerData) {
        return playerData.getClassTier() >= 2 || playerData.hasSlaves();
    }
    
    /**
     * 获取奴隶数量限制
     */
    public static int getMaxSlaves(IPlayerData playerData) {
        return switch (playerData.getClassTier()) {
            case 2 -> 5;  // 自由公民最多5个奴隶
            case 3 -> 10; // 更高阶级（如果有）
            default -> 0;
        };
    }
}
