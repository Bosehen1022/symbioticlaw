package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 释放奴隶数据包
 * 客户端 -> 服务器
 */
public class ServerBoundReleaseSlavePacket {
    
    private final UUID slaveUUID;
    
    public ServerBoundReleaseSlavePacket(UUID slaveUUID) {
        this.slaveUUID = slaveUUID;
    }
    
    public ServerBoundReleaseSlavePacket(FriendlyByteBuf buf) {
        this.slaveUUID = buf.readUUID();
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.slaveUUID);
    }
    
    public UUID getSlaveUUID() {
        return slaveUUID;
    }
    
    public static void handle(ServerBoundReleaseSlavePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                com.symbioticlaw.system.SlaverySystem.releaseSlave(player, msg.getSlaveUUID());
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void encode(ServerBoundReleaseSlavePacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ServerBoundReleaseSlavePacket decode(FriendlyByteBuf buffer) {
        return new ServerBoundReleaseSlavePacket(buffer);
    }
}
