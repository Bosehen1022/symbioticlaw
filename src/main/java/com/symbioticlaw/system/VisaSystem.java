package com.symbioticlaw.system;

import com.symbioticlaw.capability.IPlayerData;
import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class VisaSystem {
    public void tick(MinecraftServer server) {
        if (server.overworld() == null) return;
        long now = server.overworld().getGameTime();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> normalize(playerData, now));
        }
    }

    private void normalize(IPlayerData playerData, long now) {
        if (playerData.getVisaExpireTime() > 0 && playerData.getVisaExpireTime() <= now) {
            playerData.setVisaExpireTime(0);
        }

        if (playerData.getSurvivalPassExpireTime() > 0 && playerData.getSurvivalPassExpireTime() <= now) {
            playerData.setSurvivalPassExpireTime(0);
        }
    }
}

