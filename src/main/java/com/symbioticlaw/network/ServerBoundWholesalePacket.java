package com.symbioticlaw.network;

import com.symbioticlaw.system.ChefWholesaleSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 厨师批发购买数据包
 * 客户端 -> 服务器
 */
public class ServerBoundWholesalePacket {
    
    private final String itemId;
    private final int amount;
    
    public ServerBoundWholesalePacket(String itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }
    
    public ServerBoundWholesalePacket(FriendlyByteBuf buf) {
        this.itemId = buf.readUtf();
        this.amount = buf.readInt();
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.itemId);
        buf.writeInt(this.amount);
    }
    
    public static void handle(ServerBoundWholesalePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ChefWholesaleSystem.purchaseWholesale(player, msg.itemId, msg.amount);
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void encode(ServerBoundWholesalePacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ServerBoundWholesalePacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundWholesalePacket(buffer);
    }
}
