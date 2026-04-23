package com.symbioticlaw.network;

import com.symbioticlaw.system.AffinitySystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerBoundBondCreatePacket {
    private final String targetPlayerName;

    public ServerBoundBondCreatePacket(String targetPlayerName) {
        this.targetPlayerName = targetPlayerName;
    }

    public ServerBoundBondCreatePacket(FriendlyByteBuf buffer) {
        this.targetPlayerName = buffer.readUtf();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(targetPlayerName);
    }

    public static ServerBoundBondCreatePacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundBondCreatePacket(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null || sender.getServer() == null) return;

            // 通过名称查找玩家
            ServerPlayer target = sender.getServer().getPlayerList().getPlayerByName(targetPlayerName);
            if (target != null) {
                new AffinitySystem().initiateBond(sender, target);
            }
        });
        context.setPacketHandled(true);
    }
}
