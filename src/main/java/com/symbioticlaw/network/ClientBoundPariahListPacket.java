package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 遗民列表数据包
 * 服务器 -> 客户端
 */
public class ClientBoundPariahListPacket {
    
    public static class PariahInfo {
        private final UUID uuid;
        private final String name;
        private final double balance;
        private final boolean isWelfareRecipient;
        
        public PariahInfo(UUID uuid, String name, double balance, boolean isWelfareRecipient) {
            this.uuid = uuid;
            this.name = name;
            this.balance = balance;
            this.isWelfareRecipient = isWelfareRecipient;
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public String getName() {
            return name;
        }
        
        public double getBalance() {
            return balance;
        }

        public boolean isWelfareRecipient() {
            return isWelfareRecipient;
        }
        
        public static void toBytes(FriendlyByteBuf buf, PariahInfo info) {
            buf.writeUUID(info.uuid);
            buf.writeUtf(info.name);
            buf.writeDouble(info.balance);
            buf.writeBoolean(info.isWelfareRecipient);
        }
        
        public static PariahInfo fromBytes(FriendlyByteBuf buf) {
            return new PariahInfo(
                buf.readUUID(),
                buf.readUtf(),
                buf.readDouble(),
                buf.readBoolean()
            );
        }
    }
    
    private final List<PariahInfo> pariahs;
    
    public ClientBoundPariahListPacket(List<PariahInfo> pariahs) {
        this.pariahs = pariahs;
    }
    
    public ClientBoundPariahListPacket(FriendlyByteBuf buf) {
        int count = buf.readInt();
        this.pariahs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            this.pariahs.add(PariahInfo.fromBytes(buf));
        }
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.pariahs.size());
        for (PariahInfo pariah : this.pariahs) {
            PariahInfo.toBytes(buf, pariah);
        }
    }
    
    public List<PariahInfo> getPariahs() {
        return pariahs;
    }
    
    public static void handle(ClientBoundPariahListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 存储到客户端数据
            com.symbioticlaw.client.ClientPlayerData.updatePariahList(msg.getPariahs());
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void encode(ClientBoundPariahListPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ClientBoundPariahListPacket decode(FriendlyByteBuf buffer) {
        return new ClientBoundPariahListPacket(buffer);
    }
}
