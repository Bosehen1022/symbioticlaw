package com.symbioticlaw.system;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class IncomeManager {

    public static void processIncome(ServerPlayer player, double amount, String source) {
        player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
            UUID ownerUUID = playerData.getSlaveOwnerUUID();

            if (ownerUUID == null) {
                // Not a slave, receive full amount
                playerData.addBalance(amount);
                player.sendSystemMessage(Component.literal(String.format("§a[进账] +$%.2f. §7来源: %s.", amount, source)));
            } else {
                // Is a slave, split the income
                double ownerShare = amount * 0.40;
                double slaveShare = amount * 0.60;

                playerData.addBalance(slaveShare);
                player.sendSystemMessage(Component.literal(String.format("§e[分成] 总收入: $%.2f. §c上缴40%%: $%.2f. §a实得: $%.2f. §7来源: %s.", amount, ownerShare, slaveShare, source)));

                if (player.getServer() == null) return;
                ServerPlayer owner = player.getServer().getPlayerList().getPlayer(ownerUUID);

                if (owner != null && owner.connection != null) {
                    // Owner is online, give them their share directly
                    owner.getCapability(PlayerDataCapability.INSTANCE).ifPresent(ownerData -> {
                        ownerData.addBalance(ownerShare);
                        owner.sendSystemMessage(Component.literal(String.format("§b[税金] 您的奴隶 %s 为您贡献了 $%.2f. §7来源: %s.", player.getGameProfile().getName(), ownerShare, source)));
                    });
                } else {
                    // Owner is offline, handle offline income (to be implemented)
                }
            }
        });
    }
}
