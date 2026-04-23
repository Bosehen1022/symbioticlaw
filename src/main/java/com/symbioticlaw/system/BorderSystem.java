package com.symbioticlaw.system;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import com.symbioticlaw.data.WorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import com.symbioticlaw.network.ClientBoundWarningPacket;
import com.symbioticlaw.network.NetworkHandler;
import net.minecraft.world.item.ItemStack;

import com.symbioticlaw.system.TaxSystem;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BorderSystem {

    private final Map<UUID, Long> violationStartTimes = new HashMap<>();
    private static final long WARNING_DURATION_TICKS = 5 * 20; // 5 seconds

    public void tick(MinecraftServer server) {
        WorldData worldData = WorldData.get(server.overworld());
        BlockPos corePos = worldData.getCorePosition();
        if (corePos.equals(BlockPos.ZERO)) {
            return; // Core is not placed, system is inactive
        }

        if (server.overworld() == null || server.getPlayerList() == null) return;
        long currentTime = server.overworld().getGameTime();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> checkBorder(player, playerData, corePos, currentTime));
        }
    }

    private void checkBorder(ServerPlayer player, IPlayerData playerData, BlockPos corePos, long currentTime) {
        int classTier = playerData.getClassTier();
        if (classTier == 2) return; // Free citizens have no border

        double distance = Math.sqrt(player.distanceToSqr(corePos.getX(), player.getY(), corePos.getZ())); // Use 2D distance
        int allowedRadius = (classTier == 0) ? 100 : 200;

        boolean hasVisa = playerData.getVisaExpireTime() > currentTime;
        boolean hasPass = playerData.getSurvivalPassExpireTime() > currentTime;
        boolean isAllowed = (classTier == 1 && hasVisa) || (classTier == 0 && hasPass);

        if (playerData.getSlaveOwnerUUID() != null) {
            if (player.getServer() != null) {
                ServerPlayer owner = player.getServer().getPlayerList().getPlayer(playerData.getSlaveOwnerUUID());
                if (owner != null && player.distanceTo(owner) < 30) {
                    isAllowed = true;
                }
            }
        }

        if (distance > allowedRadius && !isAllowed) {
            // Player is in violation
            if (!violationStartTimes.containsKey(player.getUUID())) {
                violationStartTimes.put(player.getUUID(), currentTime);
            }

            long startTime = violationStartTimes.get(player.getUUID());
            long remainingTicks = WARNING_DURATION_TICKS - (currentTime - startTime);
            int remainingSeconds = (int) Math.max(0, remainingTicks / 20);

            String title = "§c⚠ 警告：非法越界 ⚠";
            String subtitle = "§c制裁倒计时: " + remainingSeconds;
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundWarningPacket(title + "\n" + subtitle, ClientBoundWarningPacket.WarningType.BORDER_WARNING, remainingSeconds));

            if (currentTime - startTime >= WARNING_DURATION_TICKS) {
                punish(player, playerData, corePos);
                violationStartTimes.remove(player.getUUID());
            }
        } else {
            // Player is safe, remove warning
            if (violationStartTimes.remove(player.getUUID()) != null) {
                player.sendSystemMessage(Component.literal("§a你已返回许可区域。"));
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBoundWarningPacket("", ClientBoundWarningPacket.WarningType.BORDER_WARNING, 0));
            }
        }
    }

    private void punish(ServerPlayer player, IPlayerData playerData, BlockPos corePos) {
        if (player.getServer() == null || player.getServer().getPlayerList() == null) return;
        if (playerData.getClassTier() == 0) {
            // Severe punishment for Pariah
            player.sendSystemMessage(Component.literal("§c[☠] 严重警报：检测到非法越界行为！ §c执行裁决：资产腰斩 (-50%) | 违禁品没收 (清空背包) | 强制遣返。"));
            player.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§c[警报] 遗民 " + player.getGameProfile().getName() + " 试图逃离管控区，已被核心强制执行回收程序。"), false);
            double penalty = playerData.getBalance() * 0.5;
            playerData.setBalance(playerData.getBalance() - penalty);
            playerData.addTaxPaidThisCycle(penalty);
            WorldData.get(player.serverLevel()).addTotalTaxCollected(penalty);
            player.getInventory().clearContent();
            player.teleportTo(corePos.getX() + 0.5, corePos.getY() + 0.5, corePos.getZ() + 0.5);
        } else if (playerData.getClassTier() == 1) {
            // Standard punishment for Restricted Citizen
            // Mileage tax * 2.5 + teleport
            TaxSystem taxSystem = new TaxSystem();
            double mileageTax = taxSystem.calculateMileageTax(player, player.blockPosition(), corePos);
            double penalty = mileageTax * 2.5;
            playerData.setBalance(playerData.getBalance() - penalty);
            playerData.addTaxPaidThisCycle(penalty);
            WorldData.get(player.serverLevel()).addTotalTaxCollected(penalty);

            player.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§e[通报] 限制公民 " + player.getGameProfile().getName() + " 签证过期仍滞留境外，已被强制遣返并处以罚款。"), false);
            player.sendSystemMessage(Component.literal(String.format("§c因非法越界，罚款 $%.2f", penalty)));
            player.teleportTo(corePos.getX() + 0.5, corePos.getY() + 0.5, corePos.getZ() + 0.5);
        }
    }
}
