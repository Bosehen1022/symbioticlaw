package com.symbioticlaw.network;

import com.symbioticlaw.data.WorldData;
import com.symbioticlaw.system.ChefWholesaleSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端请求经济数据同步
 * 客户端 -> 服务器
 */
public class ServerBoundRequestEconomySyncPacket {
    
    public ServerBoundRequestEconomySyncPacket() {}
    
    public ServerBoundRequestEconomySyncPacket(FriendlyByteBuf buf) {
        // 无数据需要读取
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        // 无数据需要写入
    }
    
    public static void handle(ServerBoundRequestEconomySyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // 发送完整经济数据
                EconomySyncManager.sendEconomyData(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void encode(ServerBoundRequestEconomySyncPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ServerBoundRequestEconomySyncPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundRequestEconomySyncPacket(buffer);
    }
}
