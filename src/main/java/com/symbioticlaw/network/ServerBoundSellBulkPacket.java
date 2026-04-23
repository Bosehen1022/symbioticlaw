package com.symbioticlaw.network;

import com.symbioticlaw.data.MarketData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundSellBulkPacket {
    private final String itemId;
    private final int quantity;

    public ServerBoundSellBulkPacket(String itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public ServerBoundSellBulkPacket(FriendlyByteBuf buf) {
        this.itemId = buf.readUtf();
        this.quantity = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(itemId);
        buf.writeInt(quantity);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                MarketData marketData = MarketData.get(player.serverLevel());
                marketData.sellItem(player, itemId, quantity);

                // Sync player data back to the client
                player.getCapability(com.symbioticlaw.capability.PlayerDataCapability.INSTANCE).ifPresent(playerData -> {
                    NetworkHandler.sendToPlayer(player, new PlayerDataSyncPacket(playerData));
                });
            }
        });
        context.setPacketHandled(true);
    }
}
