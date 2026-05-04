package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 客户端请求经济数据同步
 * 客户端 -> 服务器
 */
public class ServerBoundRequestEconomySyncPacket {

    private static final long MIN_REQUEST_INTERVAL_MS = 1500;
    private static final Map<java.util.UUID, Long> LAST_REQUEST_TIME = new HashMap<>();
    
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
                long now = System.currentTimeMillis();
                Long last = LAST_REQUEST_TIME.get(player.getUUID());
                if (last != null && now - last < MIN_REQUEST_INTERVAL_MS) {
                    return;
                }
                LAST_REQUEST_TIME.put(player.getUUID(), now);
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
