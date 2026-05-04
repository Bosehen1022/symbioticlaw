package com.symbioticlaw.system;

import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ExileSystem {
    public void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                double debt = playerData.getExileDebt();
                double balance = playerData.getBalance();

                if (balance < 0) {
                    playerData.setExile(true);
                    playerData.setExileDebt(Math.max(debt, -balance));
                } else {
                    playerData.setExile(false);
                    if (debt > 0) {
                        playerData.setExileDebt(0);
                    }
                }
            });
        }
    }
}

