package com.symbioticlaw.system;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.WorldData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class P2PSystem {

    private static final double TRANSFER_TAX_RATE = 0.25;

    public static double calculateTransferTax(double amount) {
        return amount * TRANSFER_TAX_RATE;
    }

    public static double calculateReceivedAmount(double sentAmount) {
        return sentAmount * (1.0 - TRANSFER_TAX_RATE);
    }

    public void transfer(ServerPlayer sender, ServerPlayer target, double amount) {
        sender.getCapability(PlayerDataCapability.INSTANCE).ifPresent(senderData -> {
            if (senderData.getMasterUUID() != null) {
                sender.sendSystemMessage(Component.literal("§c[🚫 驳回] 奴隶无权支配资产。"));
                return;
            }

            if (senderData.getBalance() < amount) {
                sender.sendSystemMessage(Component.literal("§c余额不足。"));
                return;
            }

            target.getCapability(PlayerDataCapability.INSTANCE).ifPresent(targetData -> {
                double tax = calculateTransferTax(amount);
                double receivedAmount = calculateReceivedAmount(amount);

                senderData.setBalance(senderData.getBalance() - amount);
                senderData.addTaxPaidThisCycle(tax);
                WorldData.get(sender.serverLevel()).addTotalTaxCollected(tax);

                if (targetData.getMasterUUID() != null) {
                    double garnishedAmount = receivedAmount * 0.4;
                    double finalAmount = receivedAmount - garnishedAmount;
                    targetData.addBalance(finalAmount);

                    if (sender.getServer() != null) {
                        ServerPlayer owner = sender.getServer().getPlayerList().getPlayer(targetData.getMasterUUID());
                        if (owner != null) {
                            owner.getCapability(PlayerDataCapability.INSTANCE).ifPresent(ownerData -> {
                                ownerData.addBalance(garnishedAmount);
                                owner.sendSystemMessage(Component.literal(String.format("§a[税金] 您的奴隶 " + target.getGameProfile().getName() + " 上缴收益 +$%.2f", garnishedAmount)));
                            });
                        } else {
                            WorldData.get(sender.serverLevel()).getUnclaimedIncomeData().addIncome(targetData.getMasterUUID(), garnishedAmount);
                            WorldData.get(sender.serverLevel()).setDirty();
                        }
                    }

                    target.sendSystemMessage(Component.literal(String.format("§7[收入] 收到汇款 $%.2f (扣除 $%.2f 上缴债主)", finalAmount, garnishedAmount)));

                } else {
                    targetData.addBalance(receivedAmount);
                }

                sender.sendSystemMessage(Component.literal(String.format("§a[✔] 成功向 " + target.getGameProfile().getName() + " 传输 $%.2f。 (已代扣 25%% 网络流转税 $%.2f)", receivedAmount, tax)));
                target.sendSystemMessage(Component.literal(String.format("§a[收入] 收到来自 " + sender.getGameProfile().getName() + " 的汇款 $%.2f (税后)。", receivedAmount)));
            });
        });
    }
}