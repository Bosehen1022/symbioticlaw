package com.symbioticlaw.network;

import com.symbioticlaw.capability.PlayerDataCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundRequestDailyQuotaPacket {

    public ServerBoundRequestDailyQuotaPacket() {
    }

    public ServerBoundRequestDailyQuotaPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                    NetworkHandler.sendToPlayer(player, new ClientBoundDailyQuotaPacket(
                            playerData.getDailyQuotaItem(),
                            playerData.getDailyQuotaRequiredAmount(),
                            playerData.getDailyQuotaProgress(),
                            playerData.getDailyQuotaReward()
                    ));
                });
            }
        });
        return true;
    }
}
