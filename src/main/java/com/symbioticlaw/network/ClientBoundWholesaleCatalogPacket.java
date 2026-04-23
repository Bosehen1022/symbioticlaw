package com.symbioticlaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 服务端发送批发目录数据包
 * 服务器 -> 客户端
 */
public class ClientBoundWholesaleCatalogPacket {
    
    private final Map<String, Double> catalog;
    
    public ClientBoundWholesaleCatalogPacket(Map<String, Double> catalog) {
        this.catalog = catalog;
    }
    
    public ClientBoundWholesaleCatalogPacket(FriendlyByteBuf buf) {
        this.catalog = new HashMap<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            String itemId = buf.readUtf();
            double price = buf.readDouble();
            this.catalog.put(itemId, price);
        }
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(catalog.size());
        for (Map.Entry<String, Double> entry : catalog.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeDouble(entry.getValue());
        }
    }
    
    public static void handle(ClientBoundWholesaleCatalogPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 客户端接收数据并更新本地缓存
            com.symbioticlaw.client.ClientWholesaleData.updateCatalog(msg.catalog);
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static void encode(ClientBoundWholesaleCatalogPacket packet, FriendlyByteBuf buffer) {
        packet.toBytes(buffer);
    }

    public static ClientBoundWholesaleCatalogPacket decode(FriendlyByteBuf buffer) {
        return new ClientBoundWholesaleCatalogPacket(buffer);
    }
    
    public Map<String, Double> getCatalog() {
        return catalog;
    }
}
