package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 奴隶列表数据包
 * 服务器 -> 客户端
 */
public class ClientBoundSlaveListPacket {
    
    public static class SlaveInfo {
        private final UUID uuid;
        private final String name;
        private final double pendingTax;      // 待提取税金
        private final double totalExtracted;  // 累计提取
        
        public SlaveInfo(UUID uuid, String name, double pendingTax, double totalExtracted) {
            this.uuid = uuid;
            this.name = name;
            this.pendingTax = pendingTax;
            this.totalExtracted = totalExtracted;
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public String getName() {
            return name;
        }
        
        public double getPendingTax() {
            return pendingTax;
        }
        
        public double getTotalExtracted() {
            return totalExtracted;
        }
        
        public static void toBytes(FriendlyByteBuf buf, SlaveInfo info) {
            buf.writeUUID(info.uuid);
            buf.writeUtf(info.name);
            buf.writeDouble(info.pendingTax);
            buf.writeDouble(info.totalExtracted);
        }
        
        public static SlaveInfo fromBytes(FriendlyByteBuf buf) {
            return new SlaveInfo(
                buf.readUUID(),
                buf.readUtf(),
                buf.readDouble(),
                buf.readDouble()
            );
        }
    }
    
    private final List<SlaveInfo> slaves;
    private final double totalPendingTax;
    
    public ClientBoundSlaveListPacket(List<SlaveInfo> slaves, double totalPendingTax) {
        this.slaves = slaves;
        this.totalPendingTax = totalPendingTax;
    }
    
    public ClientBoundSlaveListPacket(FriendlyByteBuf buf) {
        int count = buf.readInt();
        this.slaves = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            this.slaves.add(SlaveInfo.fromBytes(buf));
        }
        this.totalPendingTax = buf.readDouble();
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.slaves.size());
        for (SlaveInfo slave : this.slaves) {
            SlaveInfo.toBytes(buf, slave);
        }
        buf.writeDouble(this.totalPendingTax);
    }
    
    public List<SlaveInfo> getSlaves() {
        return slaves;
    }
    
    public double getTotalPendingTax() {
        return totalPendingTax;
    }
    
    public static void handle(ClientBoundSlaveListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            com.symbioticlaw.client.ClientPlayerData.updateSlaveList(msg.getSlaves(), msg.getTotalPendingTax());
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void encode(ClientBoundSlaveListPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ClientBoundSlaveListPacket decode(FriendlyByteBuf buffer) {
        return new ClientBoundSlaveListPacket(buffer);
    }
}
