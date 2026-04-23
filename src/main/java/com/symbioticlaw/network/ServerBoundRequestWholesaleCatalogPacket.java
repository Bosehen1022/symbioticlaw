package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端请求批发目录数据包
 * 客户端 -> 服务器
 */
public class ServerBoundRequestWholesaleCatalogPacket {
    
    public ServerBoundRequestWholesaleCatalogPacket() {}
    
    public ServerBoundRequestWholesaleCatalogPacket(FriendlyByteBuf buf) {
        // 无数据需要读取
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        // 无数据需要写入
    }
    
    public static void handle(ServerBoundRequestWholesaleCatalogPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // 服务端获取批发目录并发送给客户端
                var catalog = com.symbioticlaw.system.ChefWholesaleSystem.getWholesaleCatalog(player);
                if (catalog != null) {
                    NetworkHandler.sendToPlayer(player, new ClientBoundWholesaleCatalogPacket(catalog));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void encode(ServerBoundRequestWholesaleCatalogPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ServerBoundRequestWholesaleCatalogPacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundRequestWholesaleCatalogPacket(buffer);
    }
}
