package com.symbioticlaw.system;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.SlaveContract;
import com.symbioticlaw.data.WorldData;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

public class SlaveSystem {

    public void initiateContract(ServerPlayer owner, ServerPlayer slave) {
        owner.getCapability(PlayerDataCapability.INSTANCE).ifPresent(ownerData -> {
            slave.getCapability(PlayerDataCapability.INSTANCE).ifPresent(slaveData -> {

                if (ownerData.getClassTier() != 2) {
                    owner.sendSystemMessage(Component.literal("§c只有自由公民才能拥有奴隶。"));
                    return;
                }
                if (slaveData.getClassTier() != 0) {
                    owner.sendSystemMessage(Component.literal("§c目标不是遗民。"));
                    return;
                }
                if (slaveData.getSlaveOwnerUUID() != null) {
                    owner.sendSystemMessage(Component.literal("§c目标已经是别人的奴隶。"));
                    return;
                }

                double buyoutCost = 500.0 - slaveData.getBalance();
                double penalty = slaveData.isWelfareRecipient() ? 500.0 : 0.0;
                double totalCost = buyoutCost + penalty;

                if (ownerData.getBalance() < totalCost) {
                    owner.sendSystemMessage(Component.literal(String.format("§c你的余额不足以支付总费用 $%.2f", totalCost)));
                    return;
                }

                Component message = Component.literal("§e[契约] 玩家 " + owner.getGameProfile().getName() + " 欲为您支付 §6$" + String.format("%.2f", totalCost) + "§e 赎身。§7您作为奴隶期间获得的所有收入将自动截留 40% 转给债主。")
                        .append(Component.literal(" [§a✔ 点击自愿卖身§r]")
                                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sy contract accept " + owner.getUUID()))))
                        .append(" ")
                        .append(Component.literal("[§c✖ 拒绝§r]")
                                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sy contract deny " + owner.getUUID()))));
                slave.sendSystemMessage(message);

                owner.sendSystemMessage(Component.literal("§e[系统] 请求已发送。正在等待目标签署卖身契..."));
            });
        });
    }

    public void finalizeContract(ServerPlayer owner, ServerPlayer slave) {
        owner.getCapability(PlayerDataCapability.INSTANCE).ifPresent(ownerData -> {
            slave.getCapability(PlayerDataCapability.INSTANCE).ifPresent(slaveData -> {

                double buyoutCost = 500.0 - slaveData.getBalance();
                double penalty = slaveData.isWelfareRecipient() ? 500.0 : 0.0;
                double totalCost = buyoutCost + penalty;

                if (ownerData.getBalance() < totalCost) {
                    return; // Should have been checked already, but as a safeguard.
                }

                // 1. Transaction
                ownerData.setBalance(ownerData.getBalance() - totalCost);
                slaveData.setBalance(500.0);

                // 2. Update slave data
                slaveData.setSlaveOwnerUUID(owner.getUUID());
                slaveData.setWelfareRecipient(false); // Clear this tag upon being bought
                slaveData.setClassTier(1); // Spec says slave is now equivalent to restricted citizen in terms of radius

                // 3. Create and save contract
                WorldData worldData = WorldData.get(owner.serverLevel());
                SlaveContract contract = new SlaveContract(owner.getUUID(), slave.getUUID(), owner.level().getGameTime());
                worldData.addContract(contract);
                ownerData.setHasSlaves(true);

                // 4. Send messages
                owner.sendSystemMessage(Component.literal("§a[资产] 交易成功。已支付 §6$" + String.format("%.2f", totalCost) + "§a。 §7所有权转移完成。目标身份已变更为：[" + owner.getGameProfile().getName() + "的奴隶]。"));
                slave.sendSystemMessage(Component.literal("§b[通知] 您的债务已被买断。当前余额重置为 $500.0。 §c[!] 身份强制变更：[" + owner.getGameProfile().getName() + "的奴隶]。即刻起您必须为债主效力。"));

                // 5. Sync data
                PlayerDataCapability.sync(owner);
                PlayerDataCapability.sync(slave);
            });
        });
    }

    public void denyContract(ServerPlayer owner, ServerPlayer slave) {
        owner.sendSystemMessage(Component.literal("§c[✖] 交易失败：目标拒绝了赎身提议。资金未扣除。"));
        slave.sendSystemMessage(Component.literal("§7[系统] 您拒绝了卖身契约。"));
    }

    public void releaseSlave(ServerPlayer owner, ServerPlayer slave) {
        owner.getCapability(PlayerDataCapability.INSTANCE).ifPresent(ownerData -> {
            slave.getCapability(PlayerDataCapability.INSTANCE).ifPresent(slaveData -> {
                if (slaveData.getSlaveOwnerUUID() == null || !slaveData.getSlaveOwnerUUID().equals(owner.getUUID())) {
                    owner.sendSystemMessage(Component.literal("§c[✖] 这不是你的奴隶。"));
                    return;
                }

                // Remove contract
                WorldData.get(owner.serverLevel()).removeContract(slave.getUUID());
                slaveData.setSlaveOwnerUUID(null);

                // Update class tier
                if (WorldData.get(owner.serverLevel()).getSlavesOf(owner.getUUID()).isEmpty()) {
                    ownerData.setHasSlaves(false);
                }
                double balance = slaveData.getBalance();
                int newTier;
                if (balance > 1000) {
                    newTier = 2; // Free Citizen
                } else if (balance >= 200) {
                    newTier = 1; // Restricted Citizen
                } else {
                    newTier = 0; // Pariah
                }
                slaveData.setClassTier(newTier);

                owner.sendSystemMessage(Component.literal("§e[系统] 契约编号 #" + slave.getUUID().toString().substring(0, 8) + " 已终止。您已放弃对 " + slave.getGameProfile().getName() + " 的所有权。"));
                slave.sendSystemMessage(Component.literal("§b[!] 自由！债主解除了您的契约。 §7身份变更：恢复自由身 (需重新判定阶级)。"));

                // Sync data
                PlayerDataCapability.sync(owner);
                PlayerDataCapability.sync(slave);
            });
        });
    }
}
