package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundTurnInDailyQuotaPacket {

    public ServerBoundTurnInDailyQuotaPacket() {
    }

    public ServerBoundTurnInDailyQuotaPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            com.symbioticlaw.system.DailyQuotaSystem.turnInQuota(player);
        });
        return true;
    }
}
