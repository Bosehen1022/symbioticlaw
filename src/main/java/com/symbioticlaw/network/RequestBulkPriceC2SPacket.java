package com.symbioticlaw.network;

import com.symbioticlaw.data.MarketData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import com.symbioticlaw.network.NetworkHandler;

import java.util.function.Supplier;

public class RequestBulkPriceC2SPacket {

    private final String itemId;
    private final int quantity;
    private final boolean isBuy;

    public RequestBulkPriceC2SPacket(String itemId, int quantity, boolean isBuy) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.isBuy = isBuy;
    }

    public RequestBulkPriceC2SPacket(FriendlyByteBuf buf) {
        this.itemId = buf.readUtf();
        this.quantity = buf.readInt();
        this.isBuy = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(itemId);
        buf.writeInt(quantity);
        buf.writeBoolean(isBuy);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                MarketData marketData = MarketData.get(player.serverLevel());
                var marketItem = marketData.getMarketItem(itemId);
                if (marketItem != null) {
                    double bulkPrice;
                    double singlePrice;
                    if (isBuy) {
                        bulkPrice = MarketData.calculateBulkBuyPrice(player, marketItem, quantity);
                        singlePrice = MarketData.getBuyPrice(player, marketItem);
                    } else {
                        bulkPrice = MarketData.calculateBulkSellPrice(player, marketItem, quantity);
                        singlePrice = MarketData.getSellPrice(player, marketItem);
                    }
                    NetworkHandler.sendToPlayer(player, new UpdateBulkPriceS2CPacket(bulkPrice, singlePrice));
                }
            }
        });
        context.setPacketHandled(true);
    }
}
