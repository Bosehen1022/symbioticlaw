package com.symbioticlaw.system;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ClassSystem {

    public void tick(MinecraftServer server) {
        if (server.getPlayerList() == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> updatePlayerClass(player, playerData));
        }
    }

    private void updatePlayerClass(ServerPlayer player, IPlayerData playerData) {
        double balance = playerData.getBalance();
        int currentTier = playerData.getClassTier();
        int newTier;

        // Pariahs cannot be upgraded automatically through this tick.
        // They must pay a fee.
        if (currentTier == 0) {
            return;
        }

        if (balance > 1000) {
            newTier = 2; // Free Citizen
        } else if (balance >= 200) {
            newTier = 1; // Restricted Citizen
        } else {
            newTier = 0; // Pariah - this is The Fall event trigger
        }

        if (currentTier != newTier) {
            playerData.setClassTier(newTier);
            
            // Announce class change
            String newClassName = getClassName(newTier);
            player.sendSystemMessage(Component.literal("§e[系统] 您的阶级已变更为: " + newClassName));

            if (newTier == 0) {
                // The Fall Event is handled by the LivingDeathEvent handler, which sets the isFallen flag.
                // This tick-based downgrade will also trigger the 'Pariah' status, but without the death penalties.
                playerData.setFallen(true); // Mark them for respawn logic if they die and respawn.
            }
        }
    }

    private String getClassName(int classTier) {
        return switch (classTier) {
            case 0 -> "§7遗民";
            case 1 -> "§e限制公民";
            case 2 -> "§b自由公民";
            default -> "";
        };
    }

    public void buyBusinessVisa(ServerPlayer player) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            if (playerData.getClassTier() != 1) {
                player.sendSystemMessage(Component.literal("§c只有限制公民才能购买商务签证。"));
                return;
            }

            if (playerData.getBalance() < 250) {
                player.sendSystemMessage(Component.literal("§c余额不足 $250，无法购买商务签证。"));
                return;
            }

            double cost = 50.0;
            playerData.setBalance(playerData.getBalance() - cost);

            long oneHourInTicks = 60 * 60 * 20;
            long currentVisaTime = playerData.getVisaExpireTime();
            long currentTime = player.level().getGameTime();

            long newExpireTime;
            if (currentVisaTime > currentTime) {
                newExpireTime = currentVisaTime + oneHourInTicks;
            } else {
                newExpireTime = currentTime + oneHourInTicks;
            }

            playerData.setVisaExpireTime(newExpireTime);

            player.sendSystemMessage(Component.literal("§b[许可] 交易成功。获得 [商务签证] (有效期: 1小时)。 §7您现在拥有 200格 外的合法通行权。"));
        });
    }

    public void claimWelfare(ServerPlayer player) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            if (playerData.getClassTier() != 0) {
                player.sendSystemMessage(Component.literal("§c只有遗民才能领取低保。"));
                return;
            }

            long currentTime = player.level().getGameTime();
            if (playerData.getWelfareCooldown() > currentTime) {
                long remainingTicks = playerData.getWelfareCooldown() - currentTime;
                player.sendSystemMessage(Component.literal("§c你今天已经领过低保了。剩余时间: " + formatTicks(remainingTicks)));
                return;
            }

            playerData.addBalance(100.0);
            playerData.setWelfareRecipient(true);
            playerData.addWelfareClaimsThisCycle(1);

            long twentyFourHoursInTicks = 24 * 60 * 60 * 20;
            playerData.setWelfareCooldown(currentTime + twentyFourHoursInTicks);

            player.sendSystemMessage(Component.literal("§7[配给] 已领取今日低保 $100.0。 §7[系统] 身份标记已更新：[低保户]。请谨记：不劳动者不得食。"));

            Component broadcast = Component.literal("§7§o玩家 " + player.getGameProfile().getName() + " 饥寒交迫，跪在权力核心前领取了今日的 $100 救济金。可悲的东西。！");
            if (player.getServer() != null && player.getServer().getPlayerList() != null) {
                player.getServer().getPlayerList().broadcastSystemMessage(broadcast, false);
            }
        });
    }

    public void payCreditRecoveryFee(ServerPlayer player) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            if (playerData.getClassTier() != 0) {
                player.sendSystemMessage(Component.literal("§c只有遗民才能支付信用恢复费。"));
                return;
            }

            double fee = 500.0;
            // Spec says player must have >= $700, to ensure they have at least $200 (Restricted Citizen threshold) after payment.
            if (playerData.getBalance() < fee + 200) {
                player.sendSystemMessage(Component.literal("§c余额不足 $700，无法支付信用恢复费。"));
                return;
            }

            playerData.setBalance(playerData.getBalance() - fee);
            playerData.setWelfareRecipient(false); // Remove the welfare tag

            // Manually update class tier, bypassing the tick system's restriction on pariahs
            double newBalance = playerData.getBalance();
            int newTier;
            if (newBalance > 1000) {
                newTier = 2; // Free Citizen
            } else {
                newTier = 1; // Restricted Citizen
            }
            playerData.setClassTier(newTier);

            player.sendSystemMessage(Component.literal("§e[系统] 赎身费用 $500.0 已扣除。档案重置中... §a[✔] [低保户] / [遗民] 标签已移除。请保持努力工作。"));
        });
    }

    public void requestSurvivalPass(ServerPlayer player) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            if (playerData.getClassTier() != 0) {
                player.sendSystemMessage(Component.literal("§c只有遗民才能申请生存假票。"));
                return;
            }

            long currentTime = player.level().getGameTime();
            if (playerData.getSurvivalPassCooldown() > currentTime) {
                long remainingTicks = playerData.getSurvivalPassCooldown() - currentTime;
                player.sendSystemMessage(Component.literal("§c生存假票仍在冷却中。剩余时间: " + formatTicks(remainingTicks)));
                return;
            }

            long tenMinutesInTicks = 10 * 60 * 20;
            long twentyFourHoursInTicks = 24 * 60 * 60 * 20;

            playerData.setSurvivalPassExpireTime(currentTime + tenMinutesInTicks);
            playerData.setSurvivalPassCooldown(currentTime + twentyFourHoursInTicks);

            player.sendSystemMessage(Component.literal("§b[许可] 交易成功。获得 [生存假票] (有效期: 10分钟)。"));
        });
    }

    private String formatTicks(long ticks) {
        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes %= 60;
        seconds %= 60;
        return String.format("%d时 %d分 %d秒", hours, minutes, seconds);
    }
}
