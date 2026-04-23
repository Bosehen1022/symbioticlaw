package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 请求奴隶列表数据包
 * 客户端 -> 服务器
 */
public class ServerBoundRequestSlaveListPacket {
    
    public ServerBoundRequestSlaveListPacket() {
    }
    
    public ServerBoundRequestSlaveListPacket(FriendlyByteBuf buf) {
        // 无数据需要读取
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        // 无数据需要写入
    }
    
    public static void handle(ServerBoundRequestSlaveListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                com.symbioticlaw.system.SlaverySystem.sendSlaveListToPlayer(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void encode(ServerBoundRequestSlaveListPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ServerBoundRequestSlaveListPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundRequestSlaveListPacket(buffer);
    }
}
