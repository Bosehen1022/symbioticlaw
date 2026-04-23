package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * P2P转账数据包
 * 客户端 -> 服务器
 */
public class ServerBoundTransferPacket {
    private final String targetPlayerName;
    private final double amount;

    public ServerBoundTransferPacket(String targetPlayerName, double amount) {
        this.targetPlayerName = targetPlayerName;
        this.amount = amount;
    }

    public ServerBoundTransferPacket(FriendlyByteBuf buf) {
        this.targetPlayerName = buf.readUtf();
        this.amount = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.targetPlayerName);
        buf.writeDouble(this.amount);
    }

    public static void handle(ServerBoundTransferPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                // 调用转账系统处理
                com.symbioticlaw.system.TransferSystem.handleTransfer(sender, msg.targetPlayerName, msg.amount);
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void encode(ServerBoundTransferPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ServerBoundTransferPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundTransferPacket(buffer);
    }
}
